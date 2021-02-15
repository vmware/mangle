import unittest
from unittest import TestCase
from unittest.mock import patch, Mock, MagicMock

from collections import namedtuple
import os
import psutil
from Faults.FaultStatus import FaultStatus
from Faults.Memoryfault import MemoryFault

'''
Unit test cases for MemoryFault.
@author: jayasankarr
'''


class MemoryFaultTest(TestCase):

    @classmethod
    def setUp(cls):
        cls.fault_args = {'--operation': 'inject', '--faultname': "memoryFault",
                      "--load": "60","--timeout": 10000,"--faultId": "1234"}
        cls.fault = MemoryFault(cls.fault_args)

    def test_get_status(self):
        self.assertEqual(FaultStatus.NOT_STARTED.name, self.fault.get_status(self.fault_args.get("--faultId")))

    @patch.object(psutil, "virtual_memory")
    def test_trigger_injection(self,mock_virtual_memory):
        svmem = namedtuple(
            'svmem', ['total', 'available', 'percent', 'used', 'free'])
        mock_virtual_memory.return_value = svmem(1000,500,50,500,500)
        fault_obj=MemoryFault(self.fault_args)
        fault_obj.trigger_injection()
        fault_obj.remediate()
        psutil.virtual_memory.assert_called()
        self.assertEqual(FaultStatus.COMPLETED.name, fault_obj.get_status(self.fault_args.get("--faultId")))