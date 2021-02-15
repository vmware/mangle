import unittest
from unittest import TestCase
from unittest.mock import patch, Mock, MagicMock

from collections import namedtuple
import os
import psutil
from Faults.FaultStatus import FaultStatus
from Faults.DiskFillFault import DiskFillFault
import subprocess

'''
Unit test cases for DiskFillFault.
@author: jayasankarr
'''


class DiskFillFaultTest(TestCase):

    @classmethod
    def setUp(cls):
        cls.fault_args ={'--operation': 'inject', '--faultname': "diskSpaceFault", "--directoryPath": "mangletestDir",
                        "--timeout": "15000","--diskFillSize":"75", "--faultId": "diskSpaceFault"}
        cls.fault = DiskFillFault(cls.fault_args)

    @patch.object(psutil, "disk_usage")
    def test_get_status(self,mock_disk_usage):
        sdiskusage = namedtuple('sdiskusage', ['total', 'used', 'free', 'percent'])
        mock_disk_usage.return_value = sdiskusage(100, 50, 50, 50.0)
        self.assertTrue(FaultStatus.NOT_STARTED.name in self.fault.get_status(self.fault_args.get("--faultId")))

    @patch('os.remove')
    @patch('os.path.isfile')
    #@patch.object('Faults.DiskFillFault.psutil', "disk_usage")
    def test_trigger_injection(self,mock_is_file,mock_remove):
        psutil.disk_usage = mocked_call_disk
        # sdiskusage = namedtuple('sdiskusage', ['total', 'used', 'free', 'percent'])   mock_disk_usage,
        # mock_disk_usage.return_value = sdiskusage(100,50,50,50.0)
        mock_is_file.return_value = True
        mock_remove.return_value = 1
        subprocess.run = mocked_call
        fault_obj = DiskFillFault(self.fault_args)
        fault_obj.trigger_injection()
        fault_obj.remediate()
        self.assertTrue(mock_remove.called)
        self.assertTrue(mock_is_file.called)
        self.assertEqual(FaultStatus.COMPLETED.name, fault_obj.get_status(self.fault_args.get("--faultId")))


def mocked_call(*a, **kw):
    return ""

def mocked_call_disk(*a, **kw):
    sdiskusage = namedtuple('sdiskusage', ['total', 'used', 'free', 'percent'])
    return sdiskusage(100,50,50,50.0)