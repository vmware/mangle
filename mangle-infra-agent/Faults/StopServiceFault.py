from Faults import InfraFault
from Faults import FaultStatus
import os
import subprocess
import logging

log = logging.getLogger("python_agent")


class StopServiceFault(InfraFault.InfraFault):

    def __init__(self, fault_args):
        super().__init__(fault_args)
        self.processes = []
        self.stop_cmd = ''
        self.remediate_cmd = ''
        self.status_cmd = ''
        self.sudo_command = ''

    def is_sudo_present(self):
        res = subprocess.call('sudo -nv >/dev/null 2>&1', shell=True)
        if res == 0:
            self.sudo_command = 'sudo'
            log.info("sudo available")

    def is_systemctl_present(self):
        system_ctl_present= False
        res = subprocess.call('systemctl --help >/dev/null 2>&1', shell=True)
        if res == 0:
            system_ctl_present = True
            log.info("systemctl found")
        return system_ctl_present

    def prereq_check(self):
        pre_req_error_msg = ''
        self.is_sudo_present()
        if os.path.isdir("/lib/systemd") and self.is_systemctl_present() :
            self.stop_cmd = 'systemctl stop {}'.format(self.fault_args.get("--serviceName"))
            self.remediate_cmd = 'systemctl start {}'.format(self.fault_args.get("--serviceName"))
            self.status_cmd = 'systemctl status {} --no-pager'.format(self.fault_args.get("--serviceName"))
        elif os.path.isdir("/etc/init.d"):
            self.stop_cmd = 'service {} stop'.format(self.fault_args.get("--serviceName"))
            self.remediate_cmd = 'service {} restart'.format(self.fault_args.get("--serviceName"))
            self.status_cmd = "service {} status".format(self.fault_args.get("--serviceName"))
        else:
            pre_req_error_msg += "Mangle doesn't support Stop service fault."
        #checking service is available
        if len(self.status_cmd) != 0:
            res = subprocess.call(self.status_cmd, shell=True)
            if res != 0:
                pre_req_error_msg += "Provided service:{} is not present in the System or Inactive".\
                    format(self.fault_args.get("--serviceName"))

        if len(pre_req_error_msg) > 0:
            log.info(pre_req_error_msg)
            return pre_req_error_msg

    def get_status(self, fault_id):
        log.info("status of {} is {}".format(fault_id, self.faultinfo.status))
        return self.faultinfo.status + " " + " ".join(str(x) for x in self.faultinfo.activity)

    def remediate(self):
        res = subprocess.call(self.remediate_cmd, shell=True)
        if res == 0:
            self.faultinfo.status = FaultStatus.FaultStatus.COMPLETED.name
        else:
            self.faultinfo.status = FaultStatus.FaultStatus.REMEDIATION_FAILED.name

    def trigger_injection(self):
        log.info("Injecting Service stop fault")
        #res = subprocess.run(self.stop_cmd, shell=True)
        try:
            res = subprocess.check_output(self.stop_cmd, stderr=subprocess.STDOUT, shell=True)
            log.info("Service stopped")
        except subprocess.CalledProcessError as err:
            log.info(err.output.decode("utf-8").strip())
            self.faultinfo.activity.append(err.output.decode("utf-8").strip())
            self.faultinfo.status = FaultStatus.FaultStatus.INJECTION_FAILED.name


if __name__ == '__main__':
    fault_args = {'--operation': 'inject', '--faultname': "stopServiceFault",
                  "--serviceName": "xyz","--faultId": "abcdefgDiskio", "--timeout":"1000" }
    service_stopfaut= StopServiceFault
    service_stopfaut.trigger_injection()
    print("fault triggered")
