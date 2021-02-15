from unittest import TestCase
from unittest.mock import patch, Mock, MagicMock ,create_autospec,mock_open
import psutil
import subprocess
from Faults.FaultStatus import FaultStatus
from Faults.FileHandlerFault import FileHandlerFault
import builtins
import time
import platform

'''
Unit test cases for FileHandlerFault.
@author: jayasankarr
'''


class FileHandlerFaultTest(TestCase):

    @classmethod
    def setUp(cls):
        cls.fault_args = {'--operation': 'inject', '--faultname': "fileHandlerFault",
                          "--timeout": 10000,"--faultId": "1234"}
        cls.fault = FileHandlerFault(cls.fault_args)

    def test_get_status(self):
        subprocess.check_output = create_autospec(subprocess.check_output, return_value=b'832\t0\t193350\n')
        result = subprocess.check_output('cat /proc/sys/fs/file-nr')
        self.assertTrue(FaultStatus.NOT_STARTED.name in self.fault.get_status(self.fault_args.get("--faultId")))

    #@patch("builtins.open", new_callable=mock_open())    ,mock_openfile,write_date=""
    def test_trigger_injection(self):
        subprocess.check_output=mocked_call
        #mock_file = MagicMock(spec=io.IOBase)
        #mock_openfile.return_value.__enter__.return_value=mock_file
        if platform.system() == "Linux":
            fault_obj=FileHandlerFault(self.fault_args)
            fault_obj.trigger_injection()
            time.sleep(5)
            fault_obj.remediate()
            self.assertEqual(FaultStatus.COMPLETED.name, fault_obj.get_status(self.fault_args.get("--faultId")))

def mocked_call(*a, **kw):
    return b'832\t0\t193350\n'