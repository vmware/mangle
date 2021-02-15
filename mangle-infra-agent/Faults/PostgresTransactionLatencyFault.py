from Faults import InfraFault
import psycopg2
import threading
import os

from Faults.FaultStatus import FaultStatus
from Faults.Constants.FaultConstants import FaultConstants
from infrautils import Infrafultfactory
import logging

log = logging.getLogger("python_agent")


class PostgresTransactionLatencyFault(InfraFault.InfraFault):

    def __init__(self, fault_arg):
        super().__init__(fault_arg)
        self.threadList = []
        self.connectionList = []
        self.no_of_connection = 0

    def get_status(self, fault_id):
        print("Start get_status() method...")
        print("Status of faultId:{} is {}".format(fault_id, self.faultinfo.status))
        return self.faultinfo.status

    def prereq_check(self):
        pre_req_error_msg = ''
        status = self.test_connection()
        print("Test connection", "Passed" if status else "Failed")
        if not status:
            pre_req_error_msg = FaultConstants.DB_CONNECTION_PROPERTIES_NOT_VALID
        else:
            is_table_exist = self.is_table_exist()
            if not is_table_exist:
                pre_req_error_msg = FaultConstants.DB_TABLE_NOT_EXIST.format(
                    self.fault_args.get(FaultConstants.DB_TABLE_NAME))
            else:
                is_trigger_exist = self.is_trigger_exist()
                if is_trigger_exist:
                    pre_req_error_msg = FaultConstants.DB_TRIGGER_NOT_EXIST.format("latency",
                                                                                   self.fault_args.get(
                                                                                       FaultConstants.DB_TABLE_NAME))
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
            if self.create_trigger_function():
                if not self.create_trigger():
                    self.faultinfo.status = FaultStatus.INJECTION_FAILED.name
            else:
                self.faultinfo.status = FaultStatus.INJECTION_FAILED.name
        except Exception as error:
            print("Error while injecting Postgres Db transaction latency fault", error)
            self.faultinfo.status = FaultStatus.INJECTION_FAILED.name
            self.clean()
        log.info("Triggered Postgres Db transaction latency fault...")

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

    def is_table_exist(self):
        print("Start is_table_exist() method...")
        status = False
        try:
            conn = self.get_connection()
            if conn:
                cursor = conn.cursor()
                query = """SELECT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = '{tableName}');"""
                query = query.format(tableName=self.fault_args.get(FaultConstants.DB_TABLE_NAME))
                cursor.execute(query)
                rows = cursor.fetchone()
                status = rows[0]

        except (Exception, psycopg2.DatabaseError) as error:
            log.error("Error while get active connection of Postgres %s", error)
        finally:
            if conn:
                cursor.close()
                self.close_connection(conn)
        return status

    def is_trigger_exist(self):
        print("Start is_trigger_exist() method...")
        status = False
        try:
            conn = self.get_connection()
            if conn:
                cursor = conn.cursor()
                query = """select count(*) from pg_trigger where not tgisinternal and tgrelid = '{tableName}'::regclass;"""
                query = query.format(tableName=self.fault_args.get(FaultConstants.DB_TABLE_NAME))
                cursor.execute(query)
                rows = cursor.fetchone()
                count = rows[0]
                print("count", count)
                if count >= 1:
                    status = True

        except (Exception, psycopg2.DatabaseError) as error:
            log.error("Error while get trigger on table of Postgres %s", error)
        finally:
            if conn:
                cursor.close()
                self.close_connection(conn)
        return status

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

    def create_trigger_function(self):
        print("Start create_trigger_function() method...")
        status = False
        try:
            conn = self.get_connection()
            if conn:
                cursor = conn.cursor()
                query = """
                       CREATE OR REPLACE FUNCTION triggerSleepFunction()
                       RETURNS trigger AS
                       $BODY$
                       declare
                          enable integer := {percentage};
                          config integer[] := array[enable,100-enable];
                          rn integer;
                          temp integer;
                       BEGIN
                         rn := (SELECT floor(random() * 100) + 1 ::int);
                         FOR i in 1 .. 2 LOOP
                            temp := rn-config[i];
                            -- RAISE NOTICE 'rn=% temp=% config=%',rn,temp,config[i];
                            IF temp < 0 THEN
                              IF config[i] = enable THEN
                                  PERFORM pg_sleep({latency});
                              END IF;
                              EXIT;
                            END IF;
                         END LOOP;
                         RETURN NEW;
                       END
                       $BODY$
                       LANGUAGE plpgsql VOLATILE;
                    """
                latency = round(float(self.fault_args.get(FaultConstants.DB_LATENCY)) / 1000)
                query = query.format(percentage=self.fault_args.get(FaultConstants.DB_PERCENTAGE),
                                     latency=latency)
                cursor.execute(query)
                conn.commit()
                status = True

        except (Exception, psycopg2.DatabaseError) as error:
            log.error("Error while create trigger sleep function of Postgres %s", error)
        finally:
            if conn:
                cursor.close()
                self.close_connection(conn)
        return status

    def create_trigger(self):
        print("Start create_trigger_function() method...")
        status = False
        try:
            conn = self.get_connection()
            if conn:
                cursor = conn.cursor()
                query = """
                       CREATE TRIGGER mangleSleeptrigger
                       BEFORE INSERT OR UPDATE OR DELETE
                       ON {tableName}
                       FOR EACH ROW
                       EXECUTE PROCEDURE triggerSleepFunction();
                    """
                query = query.format(tableName=self.fault_args.get(FaultConstants.DB_TABLE_NAME))
                # print(query)
                cursor.execute(query)
                conn.commit()
                status = True
        except (Exception, psycopg2.DatabaseError) as error:
            log.error("Error while create sleep trigger of Postgres %s", error)
        finally:
            if conn:
                cursor.close()
                self.close_connection(conn)
        return status

    def clean(self):
        print("clean() called...")
        status = False
        try:
            conn = self.get_connection()
            if conn:
                cursor = conn.cursor()
                query = """
                        DROP TRIGGER mangleSleeptrigger on {tableName};
                        DROP FUNCTION triggerSleepFunction();
                    """
                query = query.format(tableName=self.fault_args.get(FaultConstants.DB_TABLE_NAME))
                # print(query)
                cursor.execute(query)
                conn.commit()
                status = True

        except (Exception, psycopg2.DatabaseError) as error:
            log.error("Error while cleaning the Postgres Db transaction latency fault %s", error)
        finally:
            if conn:
                cursor.close()
                self.close_connection(conn)
        return status


if __name__ == '__main__':
    fault_args = {'--operation': 'inject', '--faultname': "dbTransactionLatencyFault_postgres",
                  "--userName": "postgres",
                  "--password": "vmware", "--port": 5432, "--dbName": "postgres", "--sslEnabled": True,
                  "--tableName": "customer", "--percentage": 75, "--latency": 1000,
                  "--timeout": 120000,
                  "--faultId": "1234"}
    fault = Infrafultfactory.get_fault(fault_args)
    fault.start()
    fault.get_status(fault_args.get("--faultId"))
