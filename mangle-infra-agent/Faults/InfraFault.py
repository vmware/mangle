import abc
import threading
import time

from Faults import FaultInfo
from Faults import FaultStatus
import logging
log = logging.getLogger("python_agent")


class InfraFault(threading.Thread,metaclass=abc.ABCMeta):
    futurelist = []

    def __init__(self,fault_args):
        threading.Thread.__init__(self)
        self.faultinfo = FaultInfo.FaultInfo(fault_args,fault_args.get("--faultname"))
        self.fault_args = fault_args
        self.futurelist = []
        self._remediation = False
        print(fault_args)
        print(fault_args.get("--faultname"))

    @abc.abstractmethod
    def get_status(self,faultId):
        log.info("Checking status of fault:{}".format(faultId))
        pass

    @property
    def remediation(self):
        return self._remediation

    @remediation.setter
    def remediation(self, value):
        log.info("Remediation set to {} ".format(value) )
        self._remediation = value

    @abc.abstractmethod
    def trigger_injection(self):
        pass

    @abc.abstractmethod
    def remediate(self):
        pass
        # for f in self.futurelist:
            # if f.cancelled() != True and f.done() != True :
            #     f.cancel()
            #     print("cancelling future")
            #     if not f.cancelled() :
            #         print("couldnt cancel {}".format(f))

    @abc.abstractmethod
    def prereq_check(self):
        pass

    def wait_and_remediate(self):
        log.info("Thread waiting to remediate for :{}".format(self.fault_args.get("--timeout")))
        time.sleep(int(self.fault_args.get("--timeout")) / 1000)
        if self.faultinfo.status != FaultStatus.FaultStatus.COMPLETED.name:
            log.info("calling remediation from wait and remediate after timeout")
            self.remediate()
        else:
            log.info("Fault is already remediated")

    def start(self):
        self.faultinfo.status = FaultStatus.FaultStatus.IN_PROGRESS.name
        log.info("status changed to {}".format(self.faultinfo.status))
        self.trigger_injection()
        log.info("status changed to {}".format(self.faultinfo.status))
        if self.faultinfo.status != FaultStatus.FaultStatus.INJECTION_FAILED.name and \
                        self.fault_args.get("--timeout") is not None:
            print("Calling wait thread")
            thread1=threading.Thread(target=self.wait_and_remediate)
            thread1.start()
            print("wait_and_remediate called for remediation")