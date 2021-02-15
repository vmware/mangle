import unittest
from unittest import TestCase
from unittest.mock import patch, Mock, MagicMock

from collections import namedtuple
import os
import psutil
from Faults.FaultStatus import FaultStatus
from Faults.DiskIOFault import DiskIOFault
import subprocess
from subprocess import CompletedProcess

'''
Unit test cases for DiskIOFault.
@author: jayasankarr
'''


class DiskFillFaultTest(TestCase):

    @classmethod
    def setUp(cls):
        cls.fault_args ={'--operation' : 'inject', '--faultname':"diskFault" ,"--targetDir":"mangletestDir",
                         "--blockSize":"81920","--timeout":"15000","--faultId":"diskFault" }
        cls.fault = DiskIOFault(cls.fault_args)

    @patch.object(psutil, "disk_io_counters")
    def test_get_status(self,mock_disk_io_counters):
        sdiskio = namedtuple('sdiskio', ['read_count', 'write_count', 'read_bytes', 'write_bytes', 'read_time',
                                         'write_time', 'read_merged_count', 'write_merged_count','busy_time'])
        mock_disk_io_counters.return_value = sdiskio(read_count=2045896, write_count=56090998, read_bytes=28572900352, write_bytes=300174102528,
                read_time=1562936, write_time=158609576, read_merged_count=41481, write_merged_count=11897830,
                busy_time=45554996)
        self.assertTrue(FaultStatus.NOT_STARTED.name in self.fault.get_status(self.fault_args.get("--faultId")))

    @patch('os.remove')
    @patch('os.path.isfile')
    def test_trigger_injection(self,mock_is_file,mock_remove):
        psutil.disk_io_counters = mocked_call_disk
        psutil.disk_usage = mocked_call_disk_usage
        mock_is_file.return_value = True
        mock_remove.return_value = 1
        subprocess.run = mocked_call
        fault_obj = DiskIOFault(self.fault_args)
        fault_obj.trigger_injection()
        fault_obj.remediate()
        self.assertTrue(mock_remove.called)
        self.assertTrue(mock_is_file.called)
        self.assertEqual(FaultStatus.COMPLETED.name, fault_obj.get_status(self.fault_args.get("--faultId")))


def mocked_call(*a, **kw):
    return CompletedProcess("", 0, "", "")

def mocked_call_disk(*a, **kw):
    sdiskio = namedtuple('sdiskio', ['read_count', 'write_count', 'read_bytes', 'write_bytes', 'read_time',
                                     'write_time', 'read_merged_count', 'write_merged_count', 'busy_time'])
    return sdiskio(read_count=2045896, write_count=56090998, read_bytes=28572900352,
                                                 write_bytes=300174102528,
                                                 read_time=1562936, write_time=158609576, read_merged_count=41481,
                                                 write_merged_count=11897830,
                                                 busy_time=45554996)

def mocked_call_disk_usage(*a, **kw):
    sdiskusage = namedtuple('sdiskusage', ['total', 'used', 'free', 'percent'])
    return sdiskusage(100,50,50,50.0)
