from Faults import InfraFault
from Faults import FaultStatus
from multiprocessing import  Process ,Queue
import time
import psutil
import logging

log = logging.getLogger("python_agent")

class CpuFault(InfraFault.InfraFault):

    def __init__(self, fault_args):
        super().__init__(fault_args)
        self.processes = []

    def prereq_check(self):
        pass

    def get_status(self, fault_id):
        log.info("status of {} is {}".format(fault_id, self.faultinfo.status))
        if self.faultinfo.status == FaultStatus.FaultStatus.COMPLETED.name \
                or self.faultinfo.status == FaultStatus.FaultStatus.NOT_STARTED.name:
            return self.faultinfo.status
        log.info("Current cpu usage: {}".format(str(psutil.cpu_percent(interval=1))))
        log.info(self.faultinfo.status + " ".join(str(x) for x in self.faultinfo.activity) + " Current usage: " +\
               str(psutil.cpu_percent(interval=1)))
        return self.faultinfo.status + " ".join(str(x) for x in self.faultinfo.activity) + " Current usage: " +\
               str(psutil.cpu_percent(interval=1))

    def trigger_injection(self):
        log.info("CPU Injection started")
        log.info("cpu percent before injection is {} ".format(psutil.cpu_percent(interval=1)))
        cpu_count = psutil.cpu_count(logical=True)
        log.info("Cpu count : ".format(cpu_count))
        for i in range(1, cpu_count + 1):
            p1 = Process(target=cpu_load, args=(self.fault_args.get("--load"),self.fault_args.get("--timeout")))
            self.processes.append(p1)
            p1.start()

    def remediate(self):
        log.info("Remediation of cpu fault called")
        for p in self.processes:
            log.info("Process :{}".format(p.pid))
            p.terminate()
        self.faultinfo.status = FaultStatus.FaultStatus.COMPLETED.name


def cpu_load(load,duration):
    load = int(load)
    duration = round(float(duration))
    start_time = round(time.time() * 1000)
    current_time = round(time.time() * 1000)
    while (current_time - start_time) < duration :
        if current_time % 100 == 0:
            time.sleep((100 - load)/1000)
        current_time = round(time.time() * 1000)

if __name__ == '__main__':
    fault_args={'--operation' : 'inject', '--faultname':"cpuFault" ,"--load":"75","--timeout":"15000","--faultId":"abcdefg" }
    cpuFault= CpuFault(fault_args)
    cpuFault.populate_thread_list()
    print("Happy CPU Injection")



