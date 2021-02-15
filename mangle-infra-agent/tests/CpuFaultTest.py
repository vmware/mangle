from unittest import TestCase
from unittest.mock import patch, Mock, MagicMock
import psutil
from Faults.FaultStatus import FaultStatus
from Faults.CpuFault import CpuFault

'''
Unit test cases for CpuFault.
@author: jayasankarr
'''


class CpuFaultTest(TestCase):

    @classmethod
    def setUp(cls):
        cls.fault_args = {'--operation': 'inject', '--faultname': "cpuFault",
                      "--load": "60","--timeout": 10000,"--faultId": "1234"}
        cls.fault = CpuFault(cls.fault_args)

    def test_get_status(self):
        self.assertEqual(FaultStatus.NOT_STARTED.name, self.fault.get_status(self.fault_args.get("--faultId")))

    def test_trigger_injection(self):
        psutil =Mock()
        fault_obj=CpuFault(self.fault_args)
        psutil.cpu_count.return_value = 4
        psutil.cpu_percent.return_value = 50
        fault_obj.trigger_injection()
        fault_obj.remediate()
        self.assertEqual(FaultStatus.COMPLETED.name, fault_obj.get_status(self.fault_args.get("--faultId")))
