from unittest import TestCase
from unittest.mock import patch, Mock, MagicMock ,create_autospec,mock_open
import psutil
import subprocess
from Faults.FaultStatus import FaultStatus
from Faults.KernelPanicFault import KernelPanicFault
import time

'''
Unit test cases for KernelPanicFault.
@author: jayasankarr
'''


class KernelPanicFaultTest(TestCase):

    @classmethod
    def setUp(cls):
        cls.fault_args = {'--operation': 'inject', '--faultname': "kernelPanicFault","--faultId": "kernelPanicFault" }
        cls.fault = KernelPanicFault(cls.fault_args)

    def test_get_status(self):
        self.assertEquals(FaultStatus.NOT_STARTED.name , self.fault.get_status(self.fault_args.get("--faultId")))

    def test_trigger_injection(self):
        subprocess.call = mocked_call
        fault_obj = KernelPanicFault(self.fault_args)
        fault_obj.trigger_injection()
        time.sleep(5)
        self.assertEqual(FaultStatus.COMPLETED.name, fault_obj.get_status(self.fault_args.get("--faultId")))


def mocked_call(*a, **kw):
    return 0
