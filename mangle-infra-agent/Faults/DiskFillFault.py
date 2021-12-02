from Faults import InfraFault
from Faults import FaultStatus
import time
import psutil
import subprocess
import os
import logging
import threading
import math
from Faults.helper import FaultHelper
log = logging.getLogger("python_agent")


class DiskFillFault(InfraFault.InfraFault):

    def __init__(self, fault_args):
        super().__init__(fault_args)
        self.threads = []
        self.sudo_command = ''

    def prereq_check(self):
        self.sudo_command = FaultHelper.is_sudo_available()
        pre_req_error_msg = ''
        dd_res = subprocess.call(self.sudo_command + ' dd --version >/dev/null 2>&1', shell=True)
        if dd_res != 0:
            pre_req_error_msg += "dd command required"
        if os.path.isdir(self.fault_args.get("--directoryPath")):
            if os.access(self.fault_args.get("--directoryPath"), os.W_OK):
                _, _, _, use_percentage = psutil.disk_usage(self.fault_args.get("--directoryPath"))
                if float(self.fault_args.get("--diskFillSize")) <= use_percentage:
                    pre_req_error_msg += "The Provided diskFill percentage should be greater than used disk percentage"
            else:
                pre_req_error_msg += "The Provided user does not have permission on given directory"
        else:
            pre_req_error_msg += "The Provided directory path not found:" + self.fault_args.get("--directoryPath")
        if len(pre_req_error_msg) > 0:
            return pre_req_error_msg

    def get_status(self, faultId):
        log.info("status of {} is {}".format(faultId, self.faultinfo.status))
        if self.faultinfo.status == FaultStatus.FaultStatus.COMPLETED.name:
            return self.faultinfo.status
        _, used, _, percentage = psutil.disk_usage(self.fault_args.get("--directoryPath"))
        current_disk_status = "Current  disk usage is {} and Current disk used Percentage is {}. "\
            .format(used, percentage)
        return self.faultinfo.status +" " +" ".join(str(x) for x in self.faultinfo.activity) + current_disk_status

    def remediate(self):
        log.info("remediation of disk fill called")
        if len(self.threads) > 0:
            for th in self.threads:
                if th:
                    th.stop = True
            print("Close threads")
            self.threads.clear()
        for proc in psutil.process_iter(['pid', 'name', 'username', 'cmdline']):
            if 'mangleDumpFile.txt' in proc.info['cmdline']:
                proc.kill()
        file_path = '{}/mangleDumpFile.txt'.format(self.fault_args.get("--directoryPath"))
        if os.path.isfile(file_path):
            log.info("removing dummyfile")
            remove_dummyfile_cmd= self.sudo_command + " rm -rf {}".format(file_path)
            subprocess.call(remove_dummyfile_cmd, shell=True)
        else:
            log.info("Error: %s file already deleted" % file_path)
        self.faultinfo.status = FaultStatus.FaultStatus.COMPLETED.name

    def trigger_injection(self):
        log.info("Filling Disk")
        thread = threading.Thread(target=self.fill_disk)
        self.threads.append(thread)
        thread.start()
        print("Thread creation done")

    def fill_disk(self):
        total, _, _, use_percentage = psutil.disk_usage(self.fault_args.get("--directoryPath"))
        if use_percentage < 100:
            disk_fill_percentage = float(self.fault_args.get("--diskFillSize"))
            of_cmd = 'of={}/mangleDumpFile.txt'.format(self.fault_args.get("--directoryPath"))
            log.info(of_cmd)
            if disk_fill_percentage != 0 and disk_fill_percentage != 100:
                percentage_to_fill = disk_fill_percentage - use_percentage
                bytes_to_fill = round(percentage_to_fill*total/100)
                log.info("Bytes to fill:{}".format(bytes_to_fill))
                count_cmd = 'count={}'.format(round(bytes_to_fill/(1024*1024)))
                log.info("Count: {}".format(count_cmd))
                dd_command = "{} dd if=/dev/zero {} oflag=append bs=1MB {} conv=notrunc".format(self.sudo_command,
                                                                                                of_cmd,count_cmd)
                cmd_return = subprocess.call(dd_command, shell=True)
                log.info(str(cmd_return))
                if cmd_return != 0:
                    self.faultinfo.status = FaultStatus.FaultStatus.INJECTION_FAILED.name
                    return
                time.sleep(round(float(self.fault_args.get("--timeout"))/1000))
            else:
                dd_command = "{} dd if=/dev/zero {} oflag=append bs=1GB conv=notrunc".format(self.sudo_command,of_cmd)
                cmd_return = subprocess.call(dd_command, shell=True)
                log.info(str(cmd_return))
                _, _, _, use_percentage = psutil.disk_usage(self.fault_args.get("--directoryPath"))
                log.info("Used:{}".format(str(use_percentage)))
                if cmd_return != 0:
                    if math.ceil(use_percentage) != 100 :
                        self.faultinfo.status = FaultStatus.FaultStatus.INJECTION_FAILED.name
                        return
                time.sleep(round(float(self.fault_args.get("--timeout")) / 1000))
        else:
            log.info("Disk is already full")


if __name__ == '__main__':
    print("Happy Filling")
    fault_args = {'--operation': 'inject', '--faultname': "diskSpaceFault", "--directoryPath": "mangletestDir",
                        "--timeout": "15000","--diskFillSize":"75", "--faultId": "abcdefgDiskio"}
    diskobj = DiskFillFault(fault_args)
    process = []
