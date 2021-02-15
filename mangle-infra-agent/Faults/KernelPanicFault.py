from Faults import InfraFault
from Faults import FaultStatus
from multiprocessing import  Process
import time
import threading
import os
import subprocess
import logging

log = logging.getLogger("python_agent")


class KernelPanicFault(InfraFault.InfraFault):

    def __init__(self, fault_args):
        super().__init__(fault_args)
        self.threadList = []

    def prereq_check(self):
        pre_req_error_msg = ''
        file_path = '/proc/sysrq-trigger'
        if os.path.isfile(file_path):
            if not os.access(file_path, os.W_OK):
                pre_req_error_msg += "Write permission on /proc/sysrq-trigger is required"
                log.info("Write permission on /proc/sysrq-trigger is required")
        else:
            log.info("Unsupported Kernel Configuration, sysrq is not configured")
            pre_req_error_msg += "Unsupported Kernel Configuration, sysrq is not configured"
        if len(pre_req_error_msg) > 0:
            return pre_req_error_msg

    def get_status(self, fault_id):
        log.info("status of {} is {}".format(fault_id, self.faultinfo.status))
        return self.faultinfo.status + " ".join(str(x) for x in self.faultinfo.activity)

    def remediate(self):
        log.info("Remediation is not supported.If kernel Panic is not triggered ,cancelling injection of the fault")
        self._remediation = True
        self.faultinfo.status == FaultStatus.FaultStatus.COMPLETED.name

    def trigger_injection(self):
        log.info("Injecting Kernel Panic")
        code = subprocess.call('c>/proc/sysrq-trigger',shell=True)
        print(code)
        if code == 0:
            self.faultinfo.status = FaultStatus.FaultStatus.COMPLETED.name


if __name__ == '__main__':
    fault_args = {'--operation': 'inject', '--faultname': "kernelPanicFault","--faultId": "kernelPanicFault" }
    kernelPanicFault= KernelPanicFault(fault_args)
    kernelPanicFault.trigger_injection()
    print("fault triggered")
