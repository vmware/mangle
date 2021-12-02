from Faults import InfraFault
from Faults import FaultStatus
from Faults.helper import FaultHelper
from multiprocessing import  Process
import time
import psutil
import sys
import subprocess
import distro
import datetime
import logging

log = logging.getLogger("python_agent")


class ClockSkewFault(InfraFault.InfraFault):

    def __init__(self, fault_args):
        super().__init__(fault_args)
        self.processes = []
        self.stop_cmd=""
        self.remediate_cmd = ""
        self.status_cmd = ""
        self.time_before_injection=''
        self.option=""
        self.sudo_command = ""

    def prereq_check(self):
        self.sudo_command = FaultHelper.is_sudo_available();
        pre_req_error_msg = ''
        dist = distro.linux_distribution(full_distribution_name=False)[0]
        log.info("distro:{}".format(str(distro.linux_distribution(full_distribution_name=False))))
        log.info("distro:{}".format(str(dist)))
        if 'ubuntu' in dist:
            self.stop_cmd = self.sudo_command + "service ntp stop"
            self.remediate_cmd = self.sudo_command + "service ntp restart"
            self.status_cmd = self.sudo_command + "service ntp status"
        elif 'centos' in dist or 'rhel' in dist:
            self.stop_cmd = self.sudo_command + "service ntpd stop"
            self.remediate_cmd = self.sudo_command + "service ntpd restart"
            self.status_cmd = self.sudo_command + "service ntpd status"
        elif 'fedora' in dist or 'sles' in dist:
            self.stop_cmd = self.sudo_command + "systemctl stop ntpd"
            self.remediate_cmd = self.sudo_command + "systemctl restart ntpd"
            self.status_cmd = self.sudo_command + "systemctl status ntpd"
        elif 'photon' in dist:
            self.stop_cmd = self.sudo_command + "systemctl stop systemd-timesyncd"
            self.remediate_cmd = self.sudo_command + "systemctl restart systemd-timesyncd"
            self.status_cmd = self.sudo_command + "systemctl status systemd-timesyncd --no-pager"
        else:
            log.info("Mangle doesn't support TimesSkew on the provided OS.")
            pre_req_error_msg = 'Mangle does not support TimesSkew on the provided OS.,'
        if len(self.status_cmd) != 0:
            status_res_code = subprocess.call(self.status_cmd,shell=True)
            if status_res_code != 0:
                pre_req_error_msg = pre_req_error_msg + "NTP is not configured on the system"
        if self.fault_args.get("--type") == "FUTURE":
            self.option = "+"
        elif self.fault_args.get("--type") == "PAST":
            self.option = "-"
        else:
            pre_req_error_msg += "Wrong type argument provided"
        if len(pre_req_error_msg) > 0:
            return pre_req_error_msg

    def get_status(self, fault_id):
        if self.faultinfo.status == FaultStatus.FaultStatus.COMPLETED.name:
            return self.faultinfo.status
        log.info("status of {} is {}".format(fault_id, self.faultinfo.status))
        current_time=''
        if len(self.status_cmd) != 0:
            try:
                subprocess.call(self.status_cmd,shell=True)
                log.info("Status check succesfull and fault is in progress")
                current_time = "time after injection of fault is {}".format(datetime.datetime.now())
            except subprocess.CalledProcessError as err:
                raise RuntimeError("Checking status failed.\n"
                               "\tGot exit code {err.returncode}. Msg: {err.output}") from err
        return self.faultinfo.status + " ".join(str(x) for x in self.faultinfo.activity) + \
               "Before injection: {}".format(self.time_before_injection) + "After Injection: " + current_time

    def remediate(self):
        log.info("Remediation is triggered")
        #kill child processes and terminate process
        for p in self.processes:
            log.info("Process id : {}".format(p.pid))
            parent = psutil.Process(p.pid)
            log.info("Number of child Process for {} is".format(parent.name(), len(parent.children(recursive=False))))
            for child in parent.children(recursive=True):
                child.kill()
            p.terminate()
        if len(self.remediate_cmd) != 0 :
            stop_cmd_return = subprocess.call(self.remediate_cmd,shell=True)
            if stop_cmd_return == 0:
                log.info("Remediation succesfull : ntp service restored")
                self.faultinfo.status = FaultStatus.FaultStatus.COMPLETED.name
            else:
                self.faultinfo.status = FaultStatus.FaultStatus.REMEDIATION_FAILED.name

    def trigger_injection(self):
        log.info("Injecting Clock skew")
        d = "{}{} days".format(self.option,self.fault_args.get("--days"))
        h = "{}{} hours".format(self.option,self.fault_args.get("--hours"))
        m = "{}{} minutes".format(self.option, self.fault_args.get("--minutes"))
        s = "{}{} seconds".format(self.option, self.fault_args.get("--seconds"))
        date_cmd = self.sudo_command + 'date -d "{} {} {} {}"'.format(d,h,m,s)
        self.time_before_injection = datetime.datetime.now()
        log.info("Creating process.")
        log.info("Date command : {}".format(date_cmd))
        log.info("stop_cmd command :".format(self.stop_cmd))
        log.info("remediate_cmd command.".format(self.remediate_cmd))
        p1 = Process(target=inject_clock_skew,args=(self.fault_args.get("--timeout"), date_cmd,
                                                    self.stop_cmd,self.sudo_command))
        self.processes.append(p1)
        p1.start()


def inject_clock_skew(time_out,date_cmd,stop_cmd,sudo_cmd):
    print(stop_cmd)
    stop_cmd_return = subprocess.run(stop_cmd,shell=True)
    log.info("Date will be changed according to input: {}".format(date_cmd))
    log.info("stop_cmd_return code:{}".format(stop_cmd_return))
    if stop_cmd_return.returncode == 0:
        try:
            date_value = subprocess.check_output(date_cmd, shell=True).decode(sys.stdout.encoding).strip()
            log.info("Date will be set to: {}".format(str(date_value)))
        except subprocess.CalledProcessError as err:
            raise RuntimeError("Date creation failed.\n"
                               "\tGot exit code {err.returncode}. Msg: {err.output}") from err
        date_cmd_return = subprocess.run(sudo_cmd + 'date -s "{}"'.format(date_value),shell=True )
        if date_cmd_return.returncode == 0:
            time.sleep(round(float(time_out)/1000))

if __name__ == '__main__':
    fault_args = {'--operation': 'inject', '--faultname': "clockSkewFault","--faultId": "abcdefgclock" ,"--timeout":"12000",
                  "--days": "1", "--hours":"1","--minutes":"1","--seconds":"10", "--type": "FUTURE"}
    clockSkewFault= ClockSkewFault(fault_args)
    clockSkewFault.trigger_injection()
    print("fault triggered")
