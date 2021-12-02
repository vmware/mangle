from Faults import InfraFault
from Faults import FaultStatus
import sys
import psutil
import os,signal
import subprocess
import getpass
import multiprocessing
import logging
import time

log = logging.getLogger("python_agent")


class KillProcessFault(InfraFault.InfraFault):

    def __init__(self, fault_args):
        super().__init__(fault_args)
        self.processes = []
        self.sudo_command = ""

    def prereq_check(self):
        pre_check_msg=''
        if not isKillPresent():
            pre_check_msg="kill command is required"
        log.info("Pre req check completed")
        if len(pre_check_msg) > 0:
            return pre_check_msg

    def get_status(self, faultId):
        log.info("status of {} is {}".format(faultId, self.faultinfo.status))
        return self.faultinfo.status + " " +" ".join(str(x) for x in self.faultinfo.activity)

    def remediate(self):
        if self.fault_args.get("--remediationCommand"):
            remediate_res = subprocess.run(self.fault_args.get("--remediationCommand"),shell=True)
            if remediate_res.returncode == 0:
                log.info("Remediation succesfull")
            else:
                log.info("Remediation command failed.The exit code was: {}" .format(remediate_res.returncode))
            self.faultinfo.status = FaultStatus.FaultStatus.COMPLETED.name


    def trigger_injection(self):
        all_process_ids = []
        sudo_command = get_sudo_command()
        euid = os.geteuid()
        if euid is None:
            euid = 1
        running_in_docker = is_running_in_docker()
        if self.fault_args.get("--processId"):
            pname_to_delete=""
            for proc in psutil.process_iter(['pid', 'name', 'username','cmdline']):
                if int(self.fault_args.get("--processId")) == proc.pid:
                    pname_to_delete = proc.name()
                    log.info("process name to delete for given processid is {}".format(pname_to_delete))
                    all_process_ids.append(int(self.fault_args.get("--processId")))
            if not pname_to_delete:
                self.faultinfo.status = FaultStatus.FaultStatus.INJECTION_FAILED.name
                self.faultinfo.activity.append("No Process found with given pid")
                return
            if running_in_docker:
                p1 = multiprocessing.Process(target=kill_process_docker,
                                             args=all_process_ids)
                self.processes.append(p1)
                p1.start()
                if not self.fault_args.get("--remediationCommand"):
                    self.faultinfo.status = FaultStatus.FaultStatus.COMPLETED.name
            else:
                kill_process(all_process_ids)
                if not self.fault_args.get("--remediationCommand"):
                    self.faultinfo.status = FaultStatus.FaultStatus.COMPLETED.name

        else:
            for proc in psutil.process_iter(['pid', 'name', 'username','cmdline']):
                if self.fault_args.get(
                        "--processIdentifier") in proc.cmdline() and os.getpid() != proc.pid and\
                                proc.name() != "infra_submit":
                    log.info("Process:{}".format(proc))
                    log.info("Process cmdLine:{}".format(proc.cmdline()))
                    log.info("Process:{}".format(proc.pid))
                    if euid == 0 or sudo_command == "sudo":
                        #os.kill(proc,signal.SIGKILL)
                        all_process_ids.append(int(proc.pid))
                        log.info("Found:{} , cmd: {}".format( proc.pid, proc.cmdline()))
                    elif proc.username() == getpass.getuser():
                        all_process_ids.append(int(proc.pid))
            if len(all_process_ids) == 0:
                log.info("No Process found with given identifier")
                self.faultinfo.activity.append("No Process found with given identifier")
                self.faultinfo.status = FaultStatus.FaultStatus.INJECTION_FAILED.name
                return
            else:
                log.info("Kill all is {}".format(self.fault_args.get("--killAll")))
                if len(all_process_ids) > 1 :
                    if self.fault_args.get("--killAll") == "true":
                        if not running_in_docker:
                            try:
                                for pid in all_process_ids:
                                    #os.kill(pid, signal.SIGKILL)
                                    subprocess.call(get_kill_command()+ str(pid),shell = True)
                                if not self.fault_args.get("--remediationCommand"):
                                    self.faultinfo.status = FaultStatus.FaultStatus.COMPLETED.name
                            except ProcessLookupError as err:
                                log.info('Process already killed:{}'.format(pid))
                            except:
                                log.info("Unexpected error:{}".format(str(sys.exc_info()[0])))
                                self.faultinfo.activity.append("Unexpected error:{}".format(str(sys.exc_info()[0])))
                                self.faultinfo.status = FaultStatus.FaultStatus.INJECTION_FAILED.name
                                return
                        else:
                            p1 = multiprocessing.Process(target=kill_process_docker,
                                         args=all_process_ids)
                            p1.start()
                            self.processes.append(p1)
                            if not self.fault_args.get("--remediationCommand"):
                                self.faultinfo.status = FaultStatus.FaultStatus.COMPLETED.name
                    else:
                        log.info('Found more than one process with same process identifier:{}'.
                                 format(str(all_process_ids)))
                        self.faultinfo.status = FaultStatus.FaultStatus.INJECTION_FAILED.name
                        self.faultinfo.activity.append("Found more than one process with same process identifier")
                        return 'Found more than one process with same process identifier:{}'.\
                            format(str(all_process_ids))
                else:
                    if not running_in_docker:
                        log.info('Found one process with same process identifier:{}'.format(all_process_ids[0]))
                        try:
                            #os.kill(all_process_ids[0], signal.SIGKILL)
                            kill_command = get_kill_command() + str(all_process_ids[0])
                            log.info("kill comand : {}" .format(kill_command))
                            subprocess.call(kill_command,shell = True)
                            if not self.fault_args.get("--remediationCommand"):
                                log.info("Found no remediation command and status after killing process is completed")
                                self.faultinfo.status = FaultStatus.FaultStatus.COMPLETED.name
                        except ProcessLookupError as err:
                            log.info('Process already killed:{}'.format(all_process_ids[0]))
                        except:
                            log.info("Unexpected error:{}".format(str(sys.exc_info()[0])))
                            self.faultinfo.status = FaultStatus.FaultStatus.INJECTION_FAILED.name
                            self.faultinfo.activity.append("Unexpected error:{}".format(str(sys.exc_info()[0])))
                            return
                    else:
                        p1 = multiprocessing.Process(target=kill_process_docker,
                                     args=all_process_ids)
                        p1.start()
                        self.processes.append(p1)
                        if not self.fault_args.get("--remediationCommand"):
                            self.faultinfo.status = FaultStatus.FaultStatus.COMPLETED.name


def get_sudo_command():
    sudo_command = ""
    res = subprocess.call('sudo -nv >/dev/null 2>&1', shell=True)
    if res == 0:
        sudo_command = "sudo"
        log.info("sudo available")
    return sudo_command


def is_running_in_docker():
    running_in_docker = False
    with open('/proc/self/cgroup', 'rt') as fh:
        if 'docker' in fh.read():
            running_in_docker = True
            log.info("Fault is running on a container")
    return running_in_docker


def kill_process(all_process_ids):
    log.info("Number of processes to delete : {}".format(len(all_process_ids)))
    try:
        for pid in all_process_ids:
            kill_command = get_kill_command();
            log.info(kill_command)
            subprocess.call(kill_command+ str(pid),shell = True)
    except:
        log.info("Unexpected error:{}".format(str(sys.exc_info()[0])))


def kill_process_docker(*all_process_ids):
    log.info("Waiting for 10 secs")
    time.sleep(10)
    kill_process(all_process_ids)


def get_kill_command():
    sudo = ""
    res = subprocess.call('sudo kill', shell=True)
    if res in (0,1):
        sudo = "sudo "
    return sudo + "kill -9 "


def isKillPresent():
    res =  subprocess.call("kill > /dev/null 2>&1", shell=True)
    if res in (0,1,2):
        return True
    return False


if __name__ == '__main__':
    fault_args = {'--operation': 'inject', '--faultname': "killProcessFault","--faultId": "abcdefgkillProcess" ,"--processIdentifier":"12000",
                  "--killAll": "1", "--processId":"1"}
    killProcessFault= KillProcessFault(fault_args)
    killProcessFault.populate_thread_list()
    print("fault triggered")