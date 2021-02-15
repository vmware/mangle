from unittest import TestCase
from unittest.mock import patch, Mock, MagicMock ,create_autospec,mock_open
import psutil
import subprocess
from Faults.FaultStatus import FaultStatus
from Faults.NetworkPartitionFault import NetworkPartitionFault
import time
from subprocess import CalledProcessError

'''
Unit test cases for NetworkPartitionFault.
@author: jayasankarr
'''


class NetworkPartitionFaultTest(TestCase):

    @classmethod
    def setUp(cls):
        cls.fault_args = {'--operation': 'inject', '--faultname': "networkPartitionFault","--timeout": "3000",
                          "--hosts": "10.134.89.81,10.134.89.82", "--faultId": "networkPartitionFault"}
        cls.fault = NetworkPartitionFault(cls.fault_args)

    def test_get_status(self):
        self.assertEquals(FaultStatus.NOT_STARTED.name, self.fault.get_status(self.fault_args.get("--faultId")))

    def test_trigger_injection(self):
        subprocess.call = mocked_call
        fault_obj = NetworkPartitionFault(self.fault_args)
        fault_obj.trigger_injection()
        time.sleep(float(self.fault_args.get("--timeout"))/1000)
        fault_obj.remediate()
        self.assertEqual(FaultStatus.COMPLETED.name, fault_obj.get_status(self.fault_args.get("--faultId")))

    def test_trigger_injection_fail(self):
        subprocess.call = mocked_call_fail
        fault_obj = NetworkPartitionFault(self.fault_args)
        fault_obj.trigger_injection()
        self.assertEqual(FaultStatus.INJECTION_FAILED.name, fault_obj.get_status(self.fault_args.get("--faultId")))


def mocked_call(*a, **kw):
    return 0


def mocked_call_fail(*a, **kw):
    return 2
