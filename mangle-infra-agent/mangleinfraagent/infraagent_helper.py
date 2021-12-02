'''
Created on Nov 25, 2019

@author: jayasankarr
'''
import argparse
from Faults import FaultStatus
import logging
from infrautils import Infrafultfactory
import threading
import re
import subprocess
import psutil
from Faults.CassandraConnectionLeakFault import log

fault_map = {}
completed_fault_map = {}
server_port = -1
log = logging.getLogger("python_agent")
lock = threading.Lock()


def parse_arguements(command):
    print("Parsing arguemnts")
    print("list", command)
    args = list(command.split(" "))
    print(str(args))
    ap = argparse.ArgumentParser(description="""infra fault""")
    subparser = ap.add_subparsers(dest='fault_name', help='Types of faults')
    fault_parser_mem = subparser.add_parser("memory_fault")
    fault_parser_cpu = subparser.add_parser("cpu_fault")
    ap.add_argument("-o", "--operation", choices=['inject', 'remediate', 'status'], required=True,
                    help="provide inject/remediate")
    fault_parser_mem.add_argument("-l", "--load", type=float, required=False, help="memoryload")
    fault_parser_mem.add_argument("-t", "--timeout", type=int, required=False, help="timeout")
    fault_parser_cpu.add_argument("-l", "--load", type=float, required=False, help="cpuload")
    fault_parser_cpu.add_argument("-t", "--timeout", type=int, required=False, help="timeout")
    return ap


def parse_args(command):
    fault_args = {}
    args_list = command.split("::")
    for i in range(len(args_list)):
        if args_list[i].startswith("--") and (i + 1) < len(args_list) and not args_list[i+1].startswith("--"):
            fault_args[args_list[i]] = args_list[i + 1]
    logargs(str(fault_args))
    return fault_args

def logargs(args):
    args = re.sub("(?<='(--password|--userName)':\\s)[^\\s]*", "'*****',", args) 
    log.info(args)

def injectfault(fault_args):
    infrafault = Infrafultfactory.get_fault(fault_args)
    for faultinstance in fault_map.values():
        if type(faultinstance) is type(infrafault) and \
                faultinstance.faultinfo.status != FaultStatus.FaultStatus.COMPLETED.name and \
                faultinstance.faultinfo.status != FaultStatus.FaultStatus.INJECTION_FAILED.name:
            log.info("Found existing fault {} with status {}".format(faultinstance.faultinfo.fault_name,
                                                            faultinstance.faultinfo.status))    
            raise Exception('Same fault is already running.Wait for completion')
    lock.acquire()
    print("Lock aquired by", threading.current_thread().name)
    log.info("Lock aquired by {}".format(str(threading.current_thread().name)))
    fault_map[fault_args.get('--faultId')] = infrafault
    if fault_args.get('--faultId') in completed_fault_map:
        del completed_fault_map[fault_args.get('--faultId')]
    lock.release()

    pre_req_output = infrafault.prereq_check()
    if pre_req_output is not None and pre_req_output != '':
        log.info('Prerequisite error: {}'.format(pre_req_output))
        infrafault.faultinfo.status = FaultStatus.FaultStatus.INJECTION_FAILED.name
        completed_fault_map[fault_args.get('--faultId')] = infrafault
        del fault_map[fault_args.get('--faultId')]
        raise Exception("Prerequisite failed:" + pre_req_output)
    infrafault.start()
    log.info("Fault Injection Triggered")
    return "Fault Injection Triggered"


def remediatefault(fault_id):
    infrafault = fault_map.get(fault_id)
    if infrafault is None:
        infrafault = completed_fault_map.get(fault_id)
    if infrafault is not None:
        log.info("Fault Status before remediating:{}".format(infrafault.faultinfo.status))
        if infrafault.faultinfo.status == FaultStatus.FaultStatus.IN_PROGRESS.name:
            infrafault.remediate()
            if infrafault.faultinfo.status == FaultStatus.FaultStatus.COMPLETED.name or \
                        infrafault.faultinfo.status == FaultStatus.FaultStatus.INJECTION_FAILED.name or \
                        infrafault.faultinfo.status == FaultStatus.FaultStatus.REMEDIATION_FAILED.name:
                completed_fault_map[fault_id] = infrafault
                del fault_map[fault_id]
            return "Received Remediation Request Successfully"
        elif infrafault.faultinfo.status == FaultStatus.FaultStatus.COMPLETED.name:
            return "Requested Fault is already Remediated"
        else:
            return "Requested Fault cannot be remediated.Either the injection is failed or remediation is failed"
    else:
        return "No fault found with provided ID"


def checkstatus(fault_id):
    completed_fault = completed_fault_map.get(fault_id)
    if completed_fault is not None:
        return "Status is {}".format(completed_fault.faultinfo.status)
    infra_fault = fault_map.get(fault_id)
    output = ''
    if infra_fault is not None:
        output = 'Status is ' + infra_fault.get_status(fault_id)
        log.info(output)
        if infra_fault.faultinfo.status == FaultStatus.FaultStatus.COMPLETED.name or \
                        infra_fault.faultinfo.status == FaultStatus.FaultStatus.INJECTION_FAILED.name or \
                        infra_fault.faultinfo.status == FaultStatus.FaultStatus.REMEDIATION_FAILED.name:
            completed_fault_map[fault_id] = infra_fault
            del fault_map[fault_id]
    else:
        output = 'There is no fault running with given fault ID: {}'.format(fault_id)
    return output


def list_faults():
    return list(fault_map.keys()) + list(completed_fault_map.keys())


def is_faults_running():
    global fault_map
    if fault_map:
        for fault in fault_map.values():
            if fault.faultinfo.status == FaultStatus.FaultStatus.NOT_STARTED.name or \
                    fault.faultinfo.status == FaultStatus.FaultStatus.IN_PROGRESS.name or \
                    fault.faultinfo.status == FaultStatus.FaultStatus.INITIALIZING.name:
                log.info("Found fault running:{}".format(fault.faultinfo.fault_name))
                return True
    log.info("No Faults running currently")
    return False


def has_completed_faults():
    global fault_map
    if fault_map:
        for fault in fault_map.values():
            if fault.faultinfo.status == FaultStatus.FaultStatus.COMPLETED.name or \
                            fault.faultinfo.status == FaultStatus.FaultStatus.INJECTION_FAILED.name or \
                            fault.faultinfo.status == FaultStatus.FaultStatus.REMEDIATION_FAILED.name:
                log.info("Found completed fault in faultmap:{}".format(fault.faultinfo.fault_name))
                return True
        log.info("No completed faults in the faultMap")
    return False


def get_available_port(server_sock):
    global server_port
    if server_port == -1:
        ip_add = get_host_ip_address()
        server_sock.bind((ip_add, 0))
        server_port = server_sock.getsockname()[1]
    return server_port


def get_host_ip_address():
    ip_add = '127.0.0.1'
    log.info("IP: %s", ip_add)
    print("IP", ip_add)
    return ip_add


def is_sudo_available():
    res = subprocess.call('sudo -nv >/dev/null 2>&1', shell = True)
    sudo_command = ''
    if res == 0:
        sudo_command = 'sudo '
        log.info("sudo available")
    return sudo_command

   