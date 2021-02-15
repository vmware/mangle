import unittest

from unittest import TestCase
from unittest.mock import patch, Mock, MagicMock
import os

from pymongo import MongoClient

from Faults.FaultStatus import FaultStatus
from Faults.MongodbConnectionLeakFault import MongodbConnectionLeakFault

'''
Unit test cases for MongodbConnectionLeakFault.
@author: kumargautam
'''


class MongodbConnectionLeakFaultTest(TestCase):

    @classmethod
    def setUp(cls):
        print("Called setUp() function")
        cls.fault_args = {"--operation": "inject", "--faultname": "mongodb_connection_leak_fault",
                          "--userName": "test",
                          "--password": "test", "--port": 27017, "--dbName": "admin", "--sslEnabled": True,
                          "--timeout": 1000,
                          "--faultId": "1234"}
        cls.fault = MongodbConnectionLeakFault(cls.fault_args)

    def test_get_status(self):
        self.assertEqual(FaultStatus.NOT_STARTED.name, self.fault.get_status(self.fault_args.get("--faultId")))

    @patch('Faults.MongodbConnectionLeakFault.MongoClient', autospec=True)
    def test_get_connection(self, mongo_client_mock):
        mongo_client_instance = mongo_client_mock.return_value
        client = self.fault.get_connection()
        self.assertIsNotNone(client)
        self.assertEqual(mongo_client_instance, client)
        self.assertIsNotNone(mongo_client_instance)

    @patch('Faults.MongodbConnectionLeakFault.MongoClient', autospec=True,
           side_effect=Exception('Password is not correct'))
    def test_get_connection_for_error(self, mongo_client_mock):
        mongo_client_instance = mongo_client_mock.return_value
        client = self.fault.get_connection()
        self.assertIsNone(client)
        self.assertIsNotNone(mongo_client_instance)

    @patch('Faults.MongodbConnectionLeakFault.MongoClient', autospec=True)
    def test_test_connection(self, mongo_client_mock):
        mongo_client_instance = mongo_client_mock.return_value
        mongo_client_instance.close.return_value = True
        self.assertTrue(self.fault.test_connection())
        self.assertIsNotNone(mongo_client_instance)
        mongo_client_instance.close.assert_called_once()

    @patch('Faults.MongodbConnectionLeakFault.MongoClient', autospec=True,
           side_effect=Exception('Password is not correct'))
    def test_test_connection_for_error(self, mongo_client_mock):
        mongo_client_instance = mongo_client_mock.return_value
        self.assertFalse(self.fault.test_connection())
        self.assertIsNotNone(mongo_client_instance)

    @patch("Faults.MongodbConnectionLeakFault.MongoClient", autospec=True)
    def test_get_active_connections(self, mongo_client_mock):
        mongo_client_instance = mongo_client_mock.return_value
        mongo_client_instance.close.return_value = True
        db_mock = Mock(name='db_mock')
        mongo_client_instance.__getitem__.return_value = db_mock
        db_mock.command.return_value = {'connections': {'current': 5}}
        self.assertEqual(4, self.fault.get_active_connections())
        self.assertIsNotNone(mongo_client_instance)
        mongo_client_instance.close.assert_called_once()
        mongo_client_instance.__getitem__.assert_called_once()
        db_mock.command.assert_called_once()

    @patch('Faults.MongodbConnectionLeakFault.MongoClient', autospec=True)
    def test_get_active_connections_for_error(self, mongo_client_mock):
        mongo_client_instance = mongo_client_mock.return_value
        mongo_client_instance.close.return_value = True
        db_mock = Mock(name='db_mock')
        mongo_client_instance.__getitem__.return_value = db_mock
        db_mock.command.side_effect = Exception('command invalid')
        self.assertEqual(0, self.fault.get_active_connections())
        self.assertIsNotNone(mongo_client_instance)
        mongo_client_instance.close.assert_called_once()
        mongo_client_instance.__getitem__.assert_called_once()
        db_mock.command.assert_called_once()

    @patch("Faults.MongodbConnectionLeakFault.MongoClient", autospec=True)
    def test_trigger_injection(self, mongo_client_mock):
        mongo_client_instance = mongo_client_mock.return_value
        mongo_client_instance.close.return_value = True
        db_mock = Mock(name='db_mock')
        mongo_client_instance.__getitem__.return_value = db_mock
        db_mock.command.return_value = {'connections': {'current': 5}}
        self.fault.trigger_injection()
        os._exit = MagicMock()
        self.fault.remediate()
        self.assertIsNotNone(mongo_client_instance)
        mongo_client_instance.close.assert_called()
        mongo_client_instance.__getitem__.assert_called_once()
        db_mock.command.assert_called_once()
        # assert os._exit.called

    def test_close_connection(self):
        mongo_client_mock = Mock(name='mongo_client_mock', spec=MongoClient)
        mongo_client_mock.close.return_value = True
        self.fault.close_connection(mongo_client_mock)
        mongo_client_mock.close.assert_called_once()

    def test_close_connection_for_error(self):
        mongo_client_mock = Mock(name='mongo_client_mock', spec=MongoClient)
        mongo_client_mock.close.side_effect = Exception('Not able to close connection from db')
        self.fault.close_connection(mongo_client_mock)
        mongo_client_mock.close.assert_called_once()

    @classmethod
    def tearDown(cls):
        print("Called tearDown() function")
        cls.fault = None


if __name__ == '__main__':
    unittest.main()
