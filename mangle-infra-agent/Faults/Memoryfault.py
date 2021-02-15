from concurrent.futures import ThreadPoolExecutor
from concurrent.futures import ProcessPoolExecutor
from Faults import InfraFault
from Faults import FaultStatus
import time
import psutil
import math
import threading
from multiprocessing import Process
import platform

import logging
log = logging.getLogger("python_agent")


class MemoryFault(InfraFault.InfraFault):
    #handle this using init.py
    def __init__(self, fault_args):
        super().__init__(fault_args)

    def prereq_check(self):
        pass

    def get_status(self,faultId):
        log.info("status of {} is {}".format(faultId,self.faultinfo.status))
        if self.faultinfo.status == FaultStatus.FaultStatus.COMPLETED.name \
                or self.faultinfo.status == FaultStatus.FaultStatus.NOT_STARTED.name:
            return self.faultinfo.status
        else:
            if platform.system() == "Windows":
                tot, avail, percent, used, free = psutil.virtual_memory()
            else:
                tot, avail, percent, used, free, _, _, _, _, _, _ = psutil.virtual_memory()

            current_memory_status = " Current Memory  is {} MB and Current memory used Percentage is {}. "\
                .format(used,percent)
            return self.faultinfo.status + " ".join(str(x) for x in self.faultinfo.activity) + current_memory_status

    def remediate(self):
        log.info("remediation called")
        if self.futurelist:
            self.remediation = True
            self.faultinfo.status = FaultStatus.FaultStatus.COMPLETED.name
        else:
            log.info("list is empty Nothing to remediate")

    def generate_memory_load(self,timeout,load):
        log.info("Generating load")
        char_a = 'a'
        if platform.system() == "Windows":
            tot, avail, percent, used, free = psutil.virtual_memory()
        else:
            tot, avail, percent, used, free, _, _, _, _, _, _ = psutil.virtual_memory()
        initial_memory_status = "Total memory is {} MB and Percentage consumed before injection is {}. ".format(str(tot), str(percent))
        self.faultinfo.activity.append(initial_memory_status)
        log.info("Total memory is {} MB and Percentage consumed is {}".format(tot,percent))
        percentage_mem_to_fill = float(load) - percent
        log.info("Percentage to be filled :{}".format(percentage_mem_to_fill))
        memtofill = percentage_mem_to_fill / 100 * tot
        log.info("Bytes To Fill: {} ".format(memtofill))
        a = []
        a.append(char_a * math.floor(memtofill))
        start_time = int(round(time.time() * 1000))
        log.info("start time:{}".format(start_time))
        current_time = int(round(time.time() * 1000))
        check = current_time - int(timeout)
        log.info("remediate -- {}".format(self.remediation))
        while start_time > check and not self.remediation :
            time.sleep(1)
            current_time = int(round(time.time() * 1000))
            check = current_time - int(timeout)
        log.info("current time:{}".format(current_time))
        log.info("Memory injection completed")

    def trigger_injection(self):
        log.info("Creating injection threads")
        future = threading.Thread(target=self.generate_memory_load,args=(self.fault_args.get("--timeout"),
                                                                         self.fault_args.get("--load")))
        future.start()
        log.info("Thread creation done")
        self.futurelist.append(future)
        print(self.futurelist)


if __name__ == '__main__':
    fault_args={'--operation' : 'inject', '--faultname':"memoryfault" ,"--load":"75","--timeout":"30000","--faultId":"abcdefg" }
    memfault= MemoryFault(fault_args)
    memfault.populate_thread_list()



