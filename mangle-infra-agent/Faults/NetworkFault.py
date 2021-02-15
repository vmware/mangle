from Faults import InfraFault
from Faults import FaultStatus
from Faults import NetworkFaults
from Faults.helper import FaultHelper
import time
import psutil
import sys
import subprocess
import logging

log = logging.getLogger("python_agent")


class NetworkFault(InfraFault.InfraFault):
    def __init__(self, fault_args):
        super().__init__(fault_args)
        self.sudo_command = ""

    def prereq_check(self):
        FaultHelper.add_standard_sub_directories_to_path()
        pre_check_msg=''
        if self.fault_args.get("--nicName") not in psutil.net_if_addrs().keys():
            log.info("Given Nic name not found")
            pre_check_msg=pre_check_msg + "Given Nic name not found"
        res = subprocess.call('tc >/dev/null 2>&1', shell=True)
        if res != 0:
            log.info("tc is required,")
            pre_check_msg=pre_check_msg + ",tc is required"
        res = subprocess.call('sudo tc >/dev/null 2>&1', shell=True)
        if res == 0:
            self.sudo_command = "sudo "
            log.info("sudo available")
        _,res_tc_permission_out = run_command_get_result(self.sudo_command + 'tc qdisc add')
        if 'Operation not permitted' in res_tc_permission_out :
            pre_check_msg += "Sudo Permission for TC command is required,"
        log.info("Pre req check completed")
        if len(pre_check_msg) > 0:
            return pre_check_msg

    def get_status(self, faultId):
        log.info("status of {} is {}".format(faultId, self.faultinfo.status))
        return self.faultinfo.status + " " +" ".join(str(x) for x in self.faultinfo.activity)

    def remediate(self):
        code, _ = run_command_get_result\
            ('{} tc qdisc del dev {} root netem'.format(self.sudo_command,self.fault_args.get("--nicName")))
        if code == 0:
            self.faultinfo.status = FaultStatus.FaultStatus.COMPLETED.name
        else:
            self.faultinfo.status = FaultStatus.FaultStatus.REMEDIATION_FAILED.name

    def run_packet_delay(self):
        command = '{} tc qdisc add dev {} root netem delay {}ms'.\
            format(self.sudo_command,self.fault_args.get("--nicName"),self.fault_args.get("--latency"))
        return run_command_get_result(command)

    def run_packet_corruption(self):
        command = '{} tc qdisc add dev {} root netem corrupt {}%'.\
            format(self.sudo_command,self.fault_args.get("--nicName"), self.fault_args.get("--percentage"))
        return run_command_get_result(command)

    def run_packet_duplication(self):
        command = '{} tc qdisc add dev {} root netem duplicate {}%'.\
            format(self.sudo_command, self.fault_args.get("--nicName"), self.fault_args.get("--percentage"))
        return run_command_get_result(command)

    def run_packet_drop(self):
        command = '{} tc qdisc add dev {} root netem loss {}%'.\
            format(self.sudo_command, self.fault_args.get("--nicName"), self.fault_args.get("--percentage"))
        return run_command_get_result(command)

    def trigger_injection(self):
        FaultHelper.add_standard_sub_directories_to_path()
        run_command_get_result('{} tc qdisc del dev {} root netem'.
                                    format(self.sudo_command, self.fault_args.get("--nicName")))
        fault_switcher = {
            NetworkFaults.NetworkFaults.NETWORK_DELAY_MILLISECONDS.name: self.run_packet_delay,
            NetworkFaults.NetworkFaults.PACKET_DUPLICATE_PERCENTAGE.name: self.run_packet_duplication,
            NetworkFaults.NetworkFaults.PACKET_CORRUPT_PERCENTAGE.name: self.run_packet_corruption,
            NetworkFaults.NetworkFaults.PACKET_LOSS_PERCENTAGE.name: self.run_packet_drop,
        }
        func = fault_switcher.get(self.fault_args.get("--faultOperation"), lambda: "Invalid Fault Operation")
        res_code, res_output = func()
        if res_code != 0:
            log.info("Injection failed:{}".format( res_output))
            self.faultinfo.status = FaultStatus.FaultStatus.INJECTION_FAILED.name
            self.faultinfo.activity.append(res_output)


def run_command_get_result(command):
    log.info("Running command :{}".format(command))
    try:
        res = subprocess.check_output(
            command, stderr=subprocess.STDOUT, shell=True, timeout=5, universal_newlines=True)
        if not isinstance(res,str):
            res= res.decode(sys.stdout.encoding).strip()
    except subprocess.CalledProcessError as exc:
        log.info("Status : FAIL {} {}".format( exc.returncode, exc.output))
        return exc.returncode, exc.output
    else:
        log.info("Output: \n{}\n".format(res))
        return 0, res


if __name__ == '__main__':
    print("Happy newtorkdelay")
    fault_args = {'--operation': 'inject', '--faultname': "networkFault", "--faultOperation": "NETWORK_DELAY_MILLISECONDS",
                        "--timeout": "15000","--latency":"75", "--percentage":"75","--nicName":"eth0","--faultId": "abcdefgDiskio"}
    #NETWORK_DELAY_MILLISECONDS or PACKET_DUPLICATE_PERCENTAGE or PACKET_CORRUPT_PERCENTAGE or PACKET_LOSS_PERCENTAGE
    networkFault = NetworkFault(fault_args)
    cmd = 'tc qdisc add dev eth0 root netem delay 1ms'
    code, output = run_command_get_result(cmd)
    print(code)
    print(output)
    time.sleep(10)
    code, output = run_command_get_result('tc qdisc del dev eth0 root netem')



