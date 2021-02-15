from Faults import InfraFault
from cassandra.cluster import Cluster
import ssl
from cassandra.auth import PlainTextAuthProvider
import threading
import time
import os

from Faults.FaultStatus import FaultStatus
from Faults.Constants.FaultConstants import FaultConstants
from infrautils import Infrafultfactory
import logging

log = logging.getLogger("python_agent")


class CassandraConnectionLeakFault(InfraFault.InfraFault):

    def __init__(self, fault_arg):
        super().__init__(fault_arg)
        self.threadList = []
        self.connectionList = []
        self.no_of_connection = 0

    def get_status(self, fault_id):
        print("Start get_status() method...")
        log.info("Status of faultId:{} is {}".format(fault_id, self.faultinfo.status))
        log.info("No of open connection: {}".format(self.no_of_connection + len(self.connectionList)))
        return self.faultinfo.status

    def prereq_check(self):
        pre_req_error_msg = ''
        status = self.test_connection()
        print("Test connection", "Passed" if status else "Failed")
        if not status:
            pre_req_error_msg = FaultConstants.DB_CONNECTION_PROPERTIES_NOT_VALID
        return pre_req_error_msg

    def trigger_injection(self):
        print("Start trigger_injection() method...")
        self.inject_fault()

    def remediate(self):
        print("Start remediate() method...")
        self.clean()
        self._remediation = True
        self.faultinfo.status = FaultStatus.COMPLETED.name
        # os._exit(0)

    def inject_fault(self):
        print("Start inject_fault() method...")
        try:
            t1 = WorkerThread(self)
            t1.daemon = True
            t1.start()
            # Adding thread to list
            self.threadList.append(t1)
        except Exception as error:
            print("Error while injecting Cassandra db connection leak fault", error)
            self.faultinfo.status = FaultStatus.INJECTION_FAILED.name
            self.clean()
        log.info("Triggered Cassandra db connection leak fault...")

    def test_connection(self) -> bool:
        print("Start test_connection() method...")
        status = False
        conn = None
        try:
            conn = self.get_connection()
            db_name = self.fault_args.get(FaultConstants.DB_NAME)
            if conn:
                conn.execute("use {keyspace};".format(keyspace=db_name))
                status = True

        except Exception as error:
            log.error("Error while test connection of Cassandra db %s", error)

        finally:
            self.close_connection(conn)

        return status

    def get_connection(self):
        print("Start get_connection() for", threading.current_thread().getName())
        ssl_context = None
        if self.fault_args.get(FaultConstants.DB_SSL_ENABLED):
            ssl_context = ssl.SSLContext(ssl.PROTOCOL_TLSv1)
            ssl_context.verify_mode = ssl.CERT_NONE
            ssl_context.check_hostname = False
        hosts = ['127.0.0.1']
        port = self.fault_args.get(FaultConstants.DB_PORT)
        user_name = self.fault_args.get(FaultConstants.USER_NAME)
        pass_word = self.fault_args.get(FaultConstants.PASSWORD_KEY)
        db_name = self.fault_args.get(FaultConstants.DB_NAME)  # database name to authenticate
        auth_pro = PlainTextAuthProvider(username=user_name, password=pass_word)
        session = None
        try:
            cluster = Cluster(hosts, port=port, auth_provider=auth_pro, ssl_options=None,
                              ssl_context=ssl_context, connect_timeout=10)
            session = cluster.connect(keyspace=db_name)

        except Exception as error:
            log.error("Error while connecting to Cassandra db %s", error)

        return session

    def close_connection(self, conn):
        print("Cassandra db connections close for", self.getName())
        try:
            if conn:
                conn.shutdown()
        except Exception as ex:
            print(ex)

    def clean(self):
        print("clean() called...")
        if len(self.connectionList) > 0:
            # print("connectionList ", str(self.connectionList))
            for conn_obj in self.connectionList:
                self.close_connection(conn_obj)
            print("Close connections")
            self.connectionList.clear()

        if len(self.threadList) > 0:
            # print("threadList ", str(self.threadList))
            for th in self.threadList:
                if th:
                    th.stop = True
            print("Close threads")
            self.threadList.clear()


class WorkerThread(threading.Thread):

    def __init__(self, fault_cls: CassandraConnectionLeakFault):
        super(WorkerThread, self).__init__()
        self.stop = False
        self.fault_cls = fault_cls
        self.fault_args = self.fault_cls.fault_args

    def run(self):
        try:
            duration = round(float(self.fault_args.get(FaultConstants.TIMEOUT)))
            start_time = round(time.time() * 1000)
            current_time = round(time.time() * 1000)
            while (current_time - start_time) < duration:
                conn = self.fault_cls.get_connection()
                if conn:
                    self.fault_cls.connectionList.append(conn)
                current_time = round(time.time() * 1000)
                time.sleep(500 / 1000)
                if self.stop:
                    break

        except Exception as error:
            print("Error while run() method of WorkerThread class", error)


if __name__ == '__main__':
    fault_args = {'--operation': 'inject', '--faultname': "dbConnectionLeakFault_cassandra", "--userName": "cassandra",
                  "--password": "cassandra", "--port": 9042, "--dbName": "system", "--sslEnabled": True,
                  "--timeout": 30000,
                  "--faultId": "1234"}
    fault = Infrafultfactory.get_fault(fault_args)
    print(fault.prereq_check())
    fault.start()
    fault.get_status(fault_args.get("--faultId"))
