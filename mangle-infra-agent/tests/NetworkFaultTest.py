from unittest import TestCase
from unittest.mock import patch, Mock, MagicMock ,create_autospec,mock_open
import psutil
import subprocess
from Faults.FaultStatus import FaultStatus
from Faults.NetworkFault import NetworkFault
import builtins
import time
import platform
import time
from subprocess import CalledProcessError

'''
Unit test cases for NetworkFault.
@author: jayasankarr
'''


class NetworkFaultTest(TestCase):

    @classmethod
    def setUp(cls):
        cls.fault_args = {'--operation': 'inject', '--faultname': "networkFault",
                          "--faultOperation": "NETWORK_DELAY_MILLISECONDS","--timeout": "3000", "--latency": "75",
                          "--nicName": "eth0", "--faultId": "fileHandlerFault"}
        cls.fault_args_duplicate = {'--operation': 'inject', '--faultname': "networkFault",
                                    "--faultOperation": "PACKET_DUPLICATE_PERCENTAGE","--timeout": "3000",
                                    "--percentage": "75", "--nicName": "eth0", "--faultId": "fileHandlerFault"}
        cls.fault = NetworkFault(cls.fault_args)

    def test_get_status(self):
        self.assertEquals(FaultStatus.NOT_STARTED.name , self.fault.get_status(self.fault_args.get("--faultId")))

    def test_trigger_injection(self):
        subprocess.check_output = mocked_call
        fault_obj = NetworkFault(self.fault_args)
        fault_obj.trigger_injection()
        time.sleep(float(self.fault_args.get("--timeout"))/1000)
        fault_obj.remediate()
        self.assertEqual(FaultStatus.COMPLETED.name, fault_obj.get_status(self.fault_args.get("--faultId")))

    @patch('Faults.NetworkFault.subprocess', autospec=True)
    def test_trigger_injection_fail(self, mock_subprocess):
        mock_subprocess.check_output.side_effect = CalledProcessError(returncode=2,cmd ="tc qdisc add dev eth0 root netem loss 1%")
        mock_subprocess.CalledProcessError= subprocess.CalledProcessError
        fault_obj = NetworkFault(self.fault_args)
        fault_obj.trigger_injection()
        self.assertEqual(FaultStatus.INJECTION_FAILED.name, fault_obj.get_status(self.fault_args.get("--faultId")))

def mocked_call(*a, **kw):
    return ""