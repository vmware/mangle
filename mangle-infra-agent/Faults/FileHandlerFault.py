from Faults import InfraFault
import time
from multiprocessing import  Process ,Queue
import glob
import os,signal
import psutil
from Faults import FaultStatus
import subprocess
import sys
import logging

log = logging.getLogger("python_agent")

"""
This is class used to inject the file handler leak fault.
@ author: jayasankarr

"""


class FileHandlerFault(InfraFault.InfraFault):
    def __init__(self, fault_args):
        super().__init__(fault_args)
        self.processes = []
        self.filecount=0
        self.remediation = False

    def prereq_check(self):
        pre_req_error_msg = ''
        if not os.access(os.getcwd(), os.W_OK):
            log.info("Write permission on {} is required".format(os.getcwd()))
            pre_req_error_msg += "Write permission on {} is required".format(os.getcwd())
        try:
            _ = int(self.fault_args.get("--timeout"))
        except ValueError:
            pre_req_error_msg += "timeout value should be an integer: {}".format(self.fault_args.get("--timeout"))
        if len(pre_req_error_msg) > 0:
            return pre_req_error_msg
        #check ulimit
        #checkoptimized shell

    def get_status(self,faultId):
        if self.faultinfo.status == FaultStatus.FaultStatus.COMPLETED.name:
            return self.faultinfo.status
        #add trycacth
        current_handlers, max_handlers = get_file_handler_status()
        log.info(current_handlers + max_handlers)
        return self.faultinfo.status + " " + " ".join(str(x) for x in self.faultinfo.activity) + current_handlers \
               + max_handlers

    def trigger_injection(self):
        log.info("Triggering filehandler leak")
        current_handlers, _ = get_file_handler_status()
        self.faultinfo.activity.append("Before Injection :" + current_handlers)
        timeout = int(self.fault_args.get("--timeout"))
        log.info(" Injecting file handler for timeout: {}".format(timeout))
        p = Process(target=self.process_file_handler, args=(timeout,))
        p.start()
        self.processes.append(p)
        log.info("Injection done")

    def remediate(self):
        self.remediation = True
        log.info("Remediation set to :{}".format(self.remediation))
        for p in self.processes:
            parent = psutil.Process(p.pid)
            stop_cmd_return = subprocess.call('kill -STOP {}'.format(p.pid), shell=True)
            log.info("No of children for  parent {} is :{}".format(parent,len(parent.children(recursive=False))))
            for child in parent.children(recursive=True):
                child.kill()
            os.kill(p.pid, signal.SIGKILL)
        time.sleep(5)
        for filename in glob.glob('{}/myfile*'.format(os.getcwd())):
            os.remove(filename)
        self.faultinfo.status = FaultStatus.FaultStatus.COMPLETED.name

    def process_file_handler(self,timeout):
        start_time = int(round(time.time() * 1000))
        log.info("start time:{}".format( start_time))
        current_time = int(round(time.time() * 1000))
        check = current_time - timeout
        while start_time > check and not self.remediation:
            p = Process(target=self.create_file_handlers, args=(self.filecount,))
            p.start()
            self.processes.append(p)
            self.filecount = self.filecount + 1
            current_time = int(round(time.time() * 1000))
            check = current_time - timeout
        log.info("Process filehandler exiting:Remediation value is changed to {}".format(self.remediation))

    def create_file_handlers(self,filecount):
        a = []
        while True:
            try:
                if self.remediation:
                    log.info("Remediation triggered manually for file--{}".format(filecount))
                    #print("File handlers created by file {} is {}".format(filecount,len(a)))
                    break
                else:
                    print("creating handlers")
                    f3 = open("myfile{}.txt".format(filecount), "w")
                    a.append(f3)
                    #print("descriptor {} for file filecount-{}".format(f3.fileno(), filecount))
                    time.sleep(5)
            except Exception as error:
                log.info("Exception :{}".format(repr(error)))
                while not self.remediation:
                    time.sleep(1)
                break
        log.info("Exiting from process: {}".format( filecount))


def get_file_handler_status():
    res = subprocess.check_output('cat /proc/sys/fs/file-nr', shell=True).decode(sys.stdout.encoding).strip()
    output = res.split('\t')
    current_handlers = " Current handlers:{} ".format(output[0])
    max_handlers = " Maximum handlers:{} ".format(output[2])
    return current_handlers, max_handlers

if __name__ == '__main__':
    print("Happy filehandler leaking")
    fault_args = {'--operation': 'inject', '--faultname': "fileHandlerFault", "--timeout": "15000",
                  "--faultId": "fileHandlerFault"}
    filehandlerobj= FileHandlerFault(fault_args)
    filehandlerobj.trigger_injection()