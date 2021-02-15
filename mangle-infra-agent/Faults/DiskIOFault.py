from Faults import InfraFault
from Faults import FaultStatus
import time
import psutil
import os
import subprocess
import logging
import threading

log = logging.getLogger("python_agent")


class DiskIOFault(InfraFault.InfraFault):

    def __init__(self, fault_args):
        super().__init__(fault_args)
        self.threads = []
        self.sudo_command = ""

    def prereq_check(self):
        res = subprocess.call('sudo -nv >/dev/null 2>&1', shell=True)
        if res == 0:
            self.sudo_command = 'sudo'
            log.info("sudo available")
        pre_req_error_msg = ''
        dd_res = subprocess.call(self.sudo_command + ' dd --version >/dev/null 2>&1', shell=True)
        if dd_res != 0:
            pre_req_error_msg += "dd command required"
        if not os.access(self.fault_args.get("--targetDir"), os.W_OK):
            pre_req_error_msg += "The Provided user does not have permission on given directory"
        if len(pre_req_error_msg) > 0:
            return pre_req_error_msg

    def get_status(self, fault_id):
        log.info("status of {} is {}".format(fault_id, self.faultinfo.status))
        if self.faultinfo.status == FaultStatus.FaultStatus.COMPLETED.name:
            return self.faultinfo.status
        log.info(psutil.disk_io_counters(perdisk=True))
        current_disk_status = " Current  Disk io of the system is {}. To monitor IO of each disk ,try install sysstat" \
                              " and use iostat command  ".format(psutil.disk_io_counters())
        return self.faultinfo.status + " " +" ".join(str(x) for x in self.faultinfo.activity) + current_disk_status

    def remediate(self):
        log.info("Remediation of diskio called")
        if len(self.threads) > 0:
            for th in self.threads:
                if th:
                    th.stop = True
            log.info("Close threads")
            self.threads.clear()
        for proc in psutil.process_iter(['pid', 'name', 'username', 'cmdline']):
            if 'mangleburn' in proc.info['cmdline']:
                proc.kill()
        file_path = '{}/mangleburn'.format(self.fault_args.get("--targetDir"))
        if os.path.isfile(file_path):
            remove_dummyfile_cmd= self.sudo_command + " rm -rf {}".format(file_path)
            subprocess.call(remove_dummyfile_cmd, shell=True)
        else:
            print("Error: %s file already deleted" % file_path)
        self.faultinfo.status = FaultStatus.FaultStatus.COMPLETED.name

    def trigger_injection(self):
        thread = threading.Thread(target=self.fill_disk)
        self.threads.append(thread)
        thread.start()

    def fill_disk(self):
        log.info("Starting Diskio Injection")
        mbs_to_fill = 1024
        count_cmd = 'count={}'.format(mbs_to_fill)
        bs_cmd = 'bs={}'.format(self.fault_args.get("--blockSize"))
        of_cmd = 'of={}/mangleburn'.format(self.fault_args.get("--targetDir"))
        duration = round(float(self.fault_args.get("--timeout")))
        start_time = round(time.time() * 1000)
        current_time = round(time.time() * 1000)
        percentage = 0
        dd_command = "{} dd if=/dev/zero {} oflag=append {} {} conv=notrunc".format\
            (self.sudo_command,of_cmd, bs_cmd, count_cmd)
        log.info("dd command {}".format(dd_command))
        while (current_time - start_time) < duration:
            cmd_return = subprocess.call(dd_command, shell=True)
            log.info(str(cmd_return))
            if cmd_return != 0:
                self.faultinfo.status = FaultStatus.FaultStatus.INJECTION_FAILED.name
                return
            log.info("The exit code was: %d" % cmd_return.returncode)
            current_time = round(time.time() * 1000)
            _, _, _, percentage = psutil.disk_usage(self.fault_args.get("--targetDir"))
            log.info("Used Percentage : {}".format(percentage))
            if percentage > 90:
                file_path = '{}/mangleburn'.format(self.fault_args.get("--targetDir"))
                if os.path.isfile(file_path):
                    remove_dummyfile_cmd= self.sudo_command + " rm -rf {}".format(file_path)
                    subprocess.call(remove_dummyfile_cmd, shell=True)
                else:
                    print("Error: %s file not found" % file_path)


if __name__ == '__main__':
    fault_args={'--operation' : 'inject', '--faultname':"diskFault" ,"--targetDir":"mangletestDir","--blockSize":"81920","--timeout":"15000","--faultId":"abcdefgDiskio" }
    diskIOFault= DiskIOFault(fault_args)
    diskIOFault.trigger_injection()
    process = []
    print("fault triggered")
