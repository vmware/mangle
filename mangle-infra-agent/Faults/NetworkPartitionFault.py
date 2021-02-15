from Faults import InfraFault
from Faults import FaultStatus
from Faults.helper import FaultHelper
import subprocess
import logging

log = logging.getLogger("python_agent")

class NetworkPartitionFault(InfraFault.InfraFault):
    def __init__(self, fault_args):
        super().__init__(fault_args)
        self.sudo_command = ""

    def prereq_check(self):
        FaultHelper.add_standard_sub_directories_to_path()
        pre_req_error_msg = ''
        res = subprocess.call('route >/dev/null 2>&1', shell=True)
        if res not in [0,1,2]:
            log.info("route command not available")
            pre_req_error_msg += "route command not available"
        res = subprocess.call('sudo route >/dev/null 2>&1', shell=True)
        if res == 0:
            self.sudo_command = "sudo "
            log.info("sudo available")
        ping_msg = "Host communication failed :{}"
        if self.fault_args.get("--hosts"):
            log.info("communication  to these {} hosts will be blocked".format(self.fault_args.get("--hosts")))
            host_lists = self.fault_args.get("--hosts").split(",")
            failed_ips = []
            for host in host_lists:
                ping_command = 'ping -q -c 1 -W 1 {} >/dev/null 2>&1'.format(host)
                log.info(ping_command)
                res = subprocess.call(ping_command , shell=True)
                if res != 0:
                    failed_ips.append(host)
            if len(failed_ips) != 0:
                log.info(ping_msg.format(str(failed_ips)))
                pre_req_error_msg += ping_msg.format(str(failed_ips))
        else:
            pre_req_error_msg += "Recieved a hostlist which is empty or does not have hosts other than this machine"
        if len(pre_req_error_msg) > 0:
            return pre_req_error_msg

    def get_status(self, faultId):
        log.info("status of {} is {}".format(faultId, self.faultinfo.status))
        return self.faultinfo.status + " " +" ".join(str(x) for x in self.faultinfo.activity)

    def remediate(self):
        for host in self.fault_args.get("--hosts").split(","):
            route_add_cmnd = self.sudo_command + 'route delete -host {} reject'.format(host)
            res = subprocess.call(route_add_cmnd, shell=True)
        if self.faultinfo.status != FaultStatus.FaultStatus.INJECTION_FAILED.name:
            self.faultinfo.status = FaultStatus.FaultStatus.COMPLETED.name

    def trigger_injection(self):
        FaultHelper.add_standard_sub_directories_to_path()        
        host_lists = self.fault_args.get("--hosts").split(",")
        log.info("Host list :{}".format(host_lists))
        for host in host_lists:
            route_add_cmnd = self.sudo_command + 'route add -host {} reject'.format(host)
            try:
                subprocess.check_output(route_add_cmnd, stderr=subprocess.STDOUT, shell=True)
                log.info("Route added for {}".format(host))
            except subprocess.CalledProcessError as err:
                log.info(err.output.decode("utf-8").strip())
                self.faultinfo.activity.append(err.output.decode("utf-8").strip())
                self.faultinfo.status = FaultStatus.FaultStatus.INJECTION_FAILED.name
                self.remediate()
            return
                

if __name__ == '__main__':
    print("Happy newtorkpartition")
    fault_args = {'--operation': 'inject', '--faultname': "networkPartitionFault", "--faultId": "abcdefgclock",
                  "--timeout": "12000",
                  "--hosts": "10.134.89.81,10.134.89.82"}
    networkFault = NetworkPartitionFault(fault_args)
    networkFault.populate_thread_list()
