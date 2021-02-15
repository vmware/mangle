from unittest import TestCase
from unittest.mock import patch, Mock, MagicMock ,create_autospec,mock_open
import subprocess
from Faults.FaultStatus import FaultStatus
from Faults.ClockSkewFault import ClockSkewFault
from subprocess import CompletedProcess
import time
import distro

'''
Unit test cases for ClockSkewFault.
@author: jayasankarr
'''


class NetworkFaultTest(TestCase):

    @classmethod
    def setUp(cls):
        cls.fault_args = {'--operation': 'inject', '--faultname': "clockSkewFault","--faultId": "abcdefgclock" ,"--timeout":"4000",
                  "--days": "1", "--hours":"1","--minutes":"1","--seconds":"10", "--type": "FUTURE"}
        cls.fault = ClockSkewFault(cls.fault_args)

    def test_get_status(self):
        self.assertTrue(FaultStatus.NOT_STARTED.name  in self.fault.get_status(self.fault_args.get("--faultId")))


    @patch('Faults.ClockSkewFault.subprocess', autospec=True)
    def test_trigger_injection_fail(self, mock_subprocess):
        mock_subprocess.check_output.return_value = b'Thu Dec 10 13:43:42 UTC 2020\n'
        mock_subprocess.run.return_value = CompletedProcess("Thu Dec 10 13:43:42", 0, "", "")
        mock_subprocess.call.return_value = 0
        distro.linux_distribution = mock_distro
        fault_obj = ClockSkewFault(self.fault_args)
        fault_obj.prereq_check()
        fault_obj.trigger_injection()
        fault_obj.remediate()
        self.assertEqual(FaultStatus.COMPLETED.name, fault_obj.get_status(self.fault_args.get("--faultId")))


def mock_distro(*a, **kw):
    return ('ubuntu', '16.04', 'xenial')
