import unittest
from unittest import TestCase
from unittest.mock import patch, MagicMock, Mock
import os

from Faults.CassandraConnectionLeakFault import CassandraConnectionLeakFault
from Faults.FaultStatus import FaultStatus

'''
Unit test cases for CassandraConnectionLeakFault.
@author: kumargautam
'''


class CassandraConnectionLeakFaultTest(TestCase):

    @classmethod
    def setUp(cls):
        print("Called setUp() function")
        cls.fault_args = {'--operation': 'inject', '--faultname': "cassandra_connection_leak_fault",
                          "--userName": "test",
                          "--password": "test", "--port": 9042, "--dbName": "test", "--sslEnabled": True,
                          "--timeout": 1000,
                          "--faultId": "1234"}
        cls.fault = CassandraConnectionLeakFault(cls.fault_args)

    def test_get_status(self):
        self.assertEqual(FaultStatus.NOT_STARTED.name, self.fault.get_status(self.fault_args.get("--faultId")))

    @patch('Faults.CassandraConnectionLeakFault.Cluster', autospec=True)
    def test_get_connection(self, cassandra_cluster_mock):
        cassandra_cluster_instance = cassandra_cluster_mock.return_value
        cassandra_session_mock = Mock(name='cassandra_session_mock')
        cassandra_cluster_instance.connect.return_value = cassandra_session_mock
        client = self.fault.get_connection()
        self.assertIsNotNone(client)
        self.assertIsNotNone(cassandra_cluster_instance)
        cassandra_cluster_instance.connect.assert_called_once()

    @patch('Faults.CassandraConnectionLeakFault.Cluster', autospec=True)
    def test_get_connection_for_error(self, cassandra_cluster_mock):
        cassandra_cluster_instance = cassandra_cluster_mock.return_value
        cassandra_cluster_instance.connect.side_effect = Exception('not able to connect')
        client = self.fault.get_connection()
        self.assertIsNone(client)
        self.assertIsNotNone(cassandra_cluster_instance)
        cassandra_cluster_instance.connect.assert_called_once()

    @patch('Faults.CassandraConnectionLeakFault.Cluster', autospec=True)
    def test_test_connection(self, cassandra_cluster_mock):
        cassandra_cluster_instance = cassandra_cluster_mock.return_value
        cassandra_session_mock = Mock(name='cassandra_session_mock')
        cassandra_cluster_instance.connect.return_value = cassandra_session_mock
        cassandra_session_mock.execute.return_value = 1
        cassandra_session_mock.shutdown.return_value = True
        status = self.fault.test_connection()
        self.assertTrue(status)
        self.assertIsNotNone(cassandra_cluster_instance)
        cassandra_cluster_instance.connect.assert_called_once()
        cassandra_session_mock.execute.assert_called_once()
        cassandra_session_mock.shutdown.assert_called_once()

    @patch('Faults.CassandraConnectionLeakFault.Cluster', autospec=True)
    def test_test_connection_for_error(self, cassandra_cluster_mock):
        cassandra_cluster_instance = cassandra_cluster_mock.return_value
        cassandra_session_mock = Mock(name='cassandra_session_mock')
        cassandra_cluster_instance.connect.return_value = cassandra_session_mock
        cassandra_session_mock.execute.side_effect = Exception('cql query invalid')
        status = self.fault.test_connection()
        self.assertFalse(status)
        self.assertIsNotNone(cassandra_cluster_instance)
        cassandra_cluster_instance.connect.assert_called_once()
        cassandra_session_mock.execute.assert_called_once()

    @patch('Faults.CassandraConnectionLeakFault.Cluster', autospec=True)
    def test_trigger_injection(self, cassandra_cluster_mock):
        cassandra_cluster_instance = cassandra_cluster_mock.return_value
        cassandra_session_mock = Mock(name='cassandra_session_mock')
        cassandra_cluster_instance.connect.return_value = cassandra_session_mock
        cassandra_session_mock.execute.return_value = 1
        cassandra_session_mock.shutdown.return_value = True
        self.fault.trigger_injection()
        os._exit = MagicMock()
        self.fault.remediate()
        self.assertIsNotNone(cassandra_cluster_instance)
        cassandra_session_mock.shutdown.assert_called()
        # assert os._exit.called

    def test_close_connection(self):
        cassandra_session_mock = Mock(name='cassandra_session_mock')
        cassandra_session_mock.shutdown.return_value = True
        self.fault.close_connection(cassandra_session_mock)
        cassandra_session_mock.shutdown.assert_called()

    def test_close_connection_for_error(self):
        cassandra_session_mock = Mock(name='cassandra_session_mock')
        cassandra_session_mock.shutdown.side_effect = Exception
        self.fault.close_connection(cassandra_session_mock)
        cassandra_session_mock.shutdown.assert_called()

    @classmethod
    def tearDown(cls):
        print("Called tearDown() function")
        cls.fault = None


if __name__ == '__main__':
    unittest.main()
