from Faults import InfraFault
import psycopg2
import threading
import time
import os

from Faults.FaultStatus import FaultStatus
from Faults.Constants.FaultConstants import FaultConstants
from infrautils import Infrafultfactory
import logging

"""
This is class used to inject the connection leak fault
on Postgres DB.
@ author: kumargautam

"""

log = logging.getLogger("python_agent")


class PostgresConnectionLeakFault(InfraFault.InfraFault):

    def __init__(self, fault_arg):
        super().__init__(fault_arg)
        self.threadList = []
        self.connectionList = []
        self.no_of_connection = 0

    def get_status(self, fault_id):
        print("Start get_status() method...")
        log.info("Status of faultId:{} is {}".format(fault_id, self.faultinfo.status))
        log.info("No of active connection: {}".format(self.no_of_connection + len(self.connectionList)))
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
        self.no_of_connection = self.get_active_connections()
        print("no_of_connection:", self.no_of_connection)
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
            print("Error while injecting Postgres Db connection leak fault", error)
            self.faultinfo.status = FaultStatus.INJECTION_FAILED.name
            self.clean()
        log.info("Triggered Postgres Db connection leak fault...")

    def test_connection(self):
        print("Start test_connection() method...")
        status = False
        conn = None
        try:
            conn = self.get_connection()
            if conn:
                status = True

        except Exception as error:
            print("Error while test connection of Postgres db", error)

        finally:
            self.close_connection(conn)

        return status

    def get_active_connections(self):
        print("Start get_active_connections() method...")
        count = 0
        conn = None
        try:
            conn = self.get_connection()
            if conn:
                cursor = conn.cursor()
                query = """select count(*) from pg_stat_activity;"""
                cursor.execute(query)
                rows = cursor.fetchone()
                count = rows[0] - 1
        except (Exception, psycopg2.DatabaseError) as error:
            log.error("Error while get active connection of Postgres %s", error)
        finally:
            if conn:
                cursor.close()
                self.close_connection(conn)
        return count

    def get_connection(self):
        print("Start get_connection() for", threading.current_thread().getName())
        ssl_mode = "disable"
        if self.fault_args.get(FaultConstants.DB_SSL_ENABLED):
            ssl_mode = "prefer"
        conn = None
        try:
            conn = psycopg2.connect(user=self.fault_args.get(FaultConstants.USER_NAME),
                                    password=self.fault_args.get(FaultConstants.PASSWORD_KEY),
                                    host="127.0.0.1",
                                    port=self.fault_args.get(FaultConstants.DB_PORT),
                                    database=self.fault_args.get(FaultConstants.DB_NAME),
                                    sslmode=ssl_mode)

        except (Exception, psycopg2.DatabaseError) as error:
            log.error("Error while connecting to Postgres SQL %s", error)

        return conn

    def close_connection(self, conn):
        print("Close connections for", self.getName())
        try:
            if conn:
                conn.close()
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

    def __init__(self, fault_cls: PostgresConnectionLeakFault):
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
    fault_args = {'--operation': 'inject', '--faultname': "dbConnectionLeakFault_postgres", "--userName": "postgres",
                  "--password": "vmware", "--port": 5432, "--dbName": "postgres", "--sslEnabled": True,
                  "--timeout": 30000,
                  "--faultId": "1234"}
    fault = Infrafultfactory.get_fault(fault_args)
    fault.start()
    fault.get_status(fault_args.get("--faultId"))
