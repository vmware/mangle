from unittest import TestCase
from unittest.mock import Mock, patch, MagicMock
import psycopg2
import unittest
import os

from Faults.FaultStatus import FaultStatus
from Faults.PostgresTransactionLatencyFault import PostgresTransactionLatencyFault

'''
Unit test cases for PostgresTransactionLatencyFault.
@author: kumargautam
'''


class PostgresTransactionLatencyFaultTest(TestCase):

    @classmethod
    def setUp(cls):
        print("Called setUp() function")
        cls.fault_args = {'--operation': 'inject', '--faultname': "pg_transaction_error_fault",
                          "--userName": "test",
                          "--password": "test", "--port": 5432, "--dbName": "test", "--sslEnabled": True,
                          "--tableName": "customer", "--percentage": 75, "--latency": 1000,
                          "--timeout": 1000,
                          "--faultId": "1234"}
        cls.fault = PostgresTransactionLatencyFault(cls.fault_args)

    def test_get_status(self):
        self.assertEqual(FaultStatus.NOT_STARTED.name, self.fault.get_status(self.fault_args.get('--faultId')))

    @patch.object(psycopg2, 'connect')
    def test_get_connection(self, psycopg2_patch):
        connect_mock = Mock(name='connect_mock', spec=psycopg2)
        psycopg2_patch.return_value = connect_mock._connect()
        self.assertIsNotNone(self.fault.get_connection())
        self.assertEqual(1, psycopg2_patch.call_count)
        connect_mock._connect.assert_called_once()

    @patch.object(psycopg2, 'connect')
    def test_test_connection(self, psycopg2_patch):
        connect_mock = Mock(name='connect_mock')
        psycopg2_patch.return_value = connect_mock
        connect_mock.close.return_value = True
        self.assertTrue(self.fault.test_connection())
        self.assertEqual(1, psycopg2_patch.call_count)
        connect_mock.close.assert_called_once()

    @patch.object(psycopg2, 'connect')
    def test_test_connection_for_not_connected(self, psycopg2_patch):
        psycopg2_patch.return_value = None
        self.assertFalse(self.fault.test_connection())
        self.assertEqual(1, psycopg2_patch.call_count)

    @patch.object(psycopg2, 'connect')
    def test_test_connection_for_error(self, psycopg2_patch):
        psycopg2_patch.side_effect = Exception("user_name/password not matched")
        self.assertFalse(self.fault.test_connection())
        self.assertEqual(1, psycopg2_patch.call_count)

    def test_close_connection_for_error(self):
        connect_mock = Mock(name='connect_mock')
        connect_mock.close.side_effect = Exception("Close connection error")
        self.fault.close_connection(connect_mock)
        connect_mock.close.assert_called_once()

    @patch.object(psycopg2, 'connect')
    def test_is_table_exist(self, psycopg2_patch):
        connect_mock = Mock(name='connect_mock')
        psycopg2_patch.return_value = connect_mock
        cursor_mock = Mock(name='cursor_mock')
        connect_mock.cursor.return_value = cursor_mock
        connect_mock.close.return_value = True
        cursor_mock.execute.return_value = 1
        cursor_mock.fetchone.return_value = (True,)
        cursor_mock.close.return_value = True
        self.assertTrue(self.fault.is_table_exist())
        self.assertEqual(1, psycopg2_patch.call_count)
        connect_mock.cursor.assert_called_once()
        connect_mock.close.assert_called_once()
        cursor_mock.execute.assert_called_once()
        cursor_mock.fetchone.assert_called_once()
        cursor_mock.close.assert_called_once()

    @patch.object(psycopg2, 'connect')
    def test_is_table_exist_for_error(self, psycopg2_patch):
        connect_mock = Mock(name='connect_mock')
        psycopg2_patch.return_value = connect_mock
        cursor_mock = Mock(name='cursor_mock')
        connect_mock.cursor.return_value = cursor_mock
        connect_mock.close.return_value = True
        cursor_mock.execute.return_value = 1
        cursor_mock.fetchone.side_effect = Exception("Not able to fetch data from db")
        cursor_mock.close.return_value = True
        self.assertFalse(self.fault.is_table_exist())
        self.assertEqual(1, psycopg2_patch.call_count)
        connect_mock.cursor.assert_called_once()
        connect_mock.close.assert_called_once()
        cursor_mock.execute.assert_called_once()
        cursor_mock.fetchone.assert_called_once()
        cursor_mock.close.assert_called_once()

    @patch.object(psycopg2, 'connect')
    def test_is_trigger_exist(self, psycopg2_patch):
        connect_mock = Mock(name='connect_mock')
        psycopg2_patch.return_value = connect_mock
        cursor_mock = Mock(name='cursor_mock')
        connect_mock.cursor.return_value = cursor_mock
        connect_mock.close.return_value = True
        cursor_mock.execute.return_value = 1
        cursor_mock.fetchone.return_value = (1,)
        cursor_mock.close.return_value = True
        self.assertTrue(self.fault.is_trigger_exist())
        self.assertEqual(1, psycopg2_patch.call_count)
        connect_mock.cursor.assert_called_once()
        connect_mock.close.assert_called_once()
        cursor_mock.execute.assert_called_once()
        cursor_mock.fetchone.assert_called_once()
        cursor_mock.close.assert_called_once()

    @patch.object(psycopg2, 'connect')
    def test_is_trigger_exist_for_error(self, psycopg2_patch):
        connect_mock = Mock(name='connect_mock')
        psycopg2_patch.return_value = connect_mock
        cursor_mock = Mock(name='cursor_mock')
        connect_mock.cursor.return_value = cursor_mock
        connect_mock.close.return_value = True
        cursor_mock.execute.return_value = 1
        cursor_mock.fetchone.side_effect = Exception("Not able to fetch data from db")
        cursor_mock.close.return_value = True
        self.assertFalse(self.fault.is_trigger_exist())
        self.assertEqual(1, psycopg2_patch.call_count)
        connect_mock.cursor.assert_called_once()
        connect_mock.close.assert_called_once()
        cursor_mock.execute.assert_called_once()
        cursor_mock.fetchone.assert_called_once()
        cursor_mock.close.assert_called_once()

    @patch.object(psycopg2, 'connect')
    def test_trigger_injection(self, psycopg2_patch):
        connect_mock = Mock(name='connect_mock')
        psycopg2_patch.return_value = connect_mock
        cursor_mock = Mock(name='cursor_mock')
        connect_mock.cursor.return_value = cursor_mock
        connect_mock.close.return_value = True
        connect_mock.commit.return_value = True
        cursor_mock.execute.return_value = 1
        cursor_mock.fetchone.side_effect = [[True], [0]]
        cursor_mock.close.return_value = True
        self.fault.trigger_injection()
        self.assertEqual(2, psycopg2_patch.call_count)
        self.assertEqual(2, connect_mock.cursor.call_count)
        connect_mock.close.assert_called()
        self.assertEqual(2, cursor_mock.execute.call_count)
        cursor_mock.close.assert_called()
        connect_mock.commit.assert_called()

    @patch.object(psycopg2, 'connect')
    def test_remediate(self, psycopg2_patch):
        connect_mock = Mock(name='connect_mock')
        psycopg2_patch.return_value = connect_mock
        cursor_mock = Mock(name='cursor_mock')
        connect_mock.cursor.return_value = cursor_mock
        connect_mock.close.return_value = True
        connect_mock.commit.return_value = True
        cursor_mock.execute.return_value = 1
        cursor_mock.close.return_value = True
        os._exit = MagicMock()
        self.fault.remediate()
        self.assertEqual(1, psycopg2_patch.call_count)
        self.assertEqual(1, connect_mock.cursor.call_count)
        connect_mock.close.assert_called()
        self.assertEqual(1, cursor_mock.execute.call_count)
        cursor_mock.close.assert_called()
        connect_mock.commit.assert_called()
        # assert os._exit.called

    @patch.object(psycopg2, 'connect')
    def test_clean(self, psycopg2_patch):
        connect_mock = Mock(name='connect_mock')
        psycopg2_patch.return_value = connect_mock
        cursor_mock = Mock(name='cursor_mock')
        connect_mock.cursor.return_value = cursor_mock
        connect_mock.close.return_value = True
        connect_mock.commit.return_value = True
        cursor_mock.execute.return_value = 1
        cursor_mock.close.return_value = True
        self.assertTrue(self.fault.clean())
        self.assertEqual(1, psycopg2_patch.call_count)
        self.assertEqual(1, connect_mock.cursor.call_count)
        connect_mock.close.assert_called()
        self.assertEqual(1, cursor_mock.execute.call_count)
        cursor_mock.close.assert_called()
        connect_mock.commit.assert_called()

    @patch.object(psycopg2, 'connect')
    def test_clean_for_error(self, psycopg2_patch):
        connect_mock = Mock(name='connect_mock')
        psycopg2_patch.return_value = connect_mock
        cursor_mock = Mock(name='cursor_mock')
        connect_mock.cursor.return_value = cursor_mock
        connect_mock.close.return_value = True
        connect_mock.commit.side_effect = Exception("Error during commit")
        cursor_mock.execute.return_value = 1
        cursor_mock.close.return_value = True
        self.assertFalse(self.fault.clean())
        self.assertEqual(1, psycopg2_patch.call_count)
        self.assertEqual(1, connect_mock.cursor.call_count)
        connect_mock.close.assert_called()
        self.assertEqual(1, cursor_mock.execute.call_count)
        cursor_mock.close.assert_called()
        connect_mock.commit.assert_called()

    @classmethod
    def tearDown(cls):
        print("Called tearDown() function")
        cls.fault = None


if __name__ == '__main__':
    unittest.main()
