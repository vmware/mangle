'''
Created on Jan 6, 2020

@author: jayasankarr
'''
import socket
import logging
from logging.config import fileConfig
import sys, os
import threading
import time
from mangleinfraagent import infraagent_helper
import psutil
import shutil
from pathlib import Path
import glob
import subprocess

server_sock = ()
INFRA_AGENT_TAR = "infra-agent.tar.gz"

class ClientThread(threading.Thread):

    def __init__(self, clientAdd, clientSock):
        threading.Thread.__init__(self)
        self.sock = clientSock
        self.clientAddr = clientAdd
        logger.info("New connection added IP:{} :port {}".format(clientAdd[0], clientAdd[1]))

    def run(self):
        command = ''
        data = self.sock.recv(2048)
        command = data.decode()
        logger.info("Command received from client")
        message_to_client = self.fault_action(command)
        if message_to_client is not None and len(str(message_to_client)) != 0:
            self.sock.send(bytes(str(message_to_client), 'UTF-8'))
        self.sock.close()
        if str(message_to_client) == "Stopping server as faults are not running":
            server_sock.close()

    def fault_action(self, command):
        output = ''
        fault_args = infraagent_helper.parse_args(command)
        if fault_args['--operation'] == 'inject':
            try:
                output = infraagent_helper.injectfault(fault_args)
            except Exception as error:
                logger.info('Caught this error: {}'.format(repr(error)))
                return "Exception:{}".format(str(error))

        elif fault_args['--operation'] == 'remediate':
            output = infraagent_helper.remediatefault(fault_args['--faultId'])
        elif fault_args['--operation'] == 'status':
            output = infraagent_helper.checkstatus(fault_args['--faultId'])
        elif fault_args['--operation'] == 'list':
            output = ",".join(infraagent_helper.list_faults())
            if len(output) == 0:
                output = "No faults running"
        elif fault_args['--operation'] == 'terminate':
            output = "Cannot stop server as faults are running"
            is_fault_running = infraagent_helper.is_faults_running()
            logger.info("Is faults running : {}".format(is_fault_running))
            if not bool(is_fault_running):
                output = "Stopping server as faults are not running"
        else:
            output = 'Wrong command'
        return output


def check_agent_running():
    agent_process_count = 0
    for proc in psutil.process_iter():
        try:
            if os.getpid() == proc.pid:
                logger.info("current process found:{}".format(proc.name()))
            if "infra_agent" in proc.name().lower():
                logger.info("Process found with status: {}".format(proc.status().lower()))
                if proc.status() not in "zombie" or proc.status() != psutil.STATUS_ZOMBIE:
                    agent_process_count = agent_process_count + 1
        except (psutil.NoSuchProcess, psutil.AccessDenied, psutil.ZombieProcess):
            pass
    if agent_process_count > 2:
        return True


def main():
    logger.info("Checking agent is already running at the endpoint before agent start")
    if check_agent_running():
        logger.info("Agent is already running.")
        return
    global server_sock
    server_port = -1
    cleanup_thread = threading.Thread(target=server_monitor_thread)
    cleanup_thread.start()
    command = '::'.join([str(elem) for elem in sys.argv])
    fault_args = infraagent_helper.parse_args(command)
    if "--agentPort" in fault_args:
        server_port = int(fault_args['--agentPort'])
    server_sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    server_sock.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
    if server_port >= 0:
        ip_add = infraagent_helper.get_host_ip_address()
        server_sock.bind((ip_add, server_port))
    else:
        server_port = infraagent_helper.get_available_port(server_sock)
        with open('portnumber.txt', 'w+') as f:
            f.write(str(server_sock.getsockname()[1]))
        #print(server_sock.getsockname()[1])
        logger.info("Port used {}".format(server_port))
    logger.info("Server started")
    logger.info("Waiting for client request..")
    server_sock.listen(2)
    while True:
        clientsock, clientAddress = server_sock.accept()
        newthread = ClientThread(clientAddress, clientsock)
        newthread.start()
    logger.info("Trying to Terminate server");


def is_tar_modified_recently():
    file_modified = False
    base_path = Path(__file__).parent
    try:
        agent_tar_path = (base_path / ".."/ INFRA_AGENT_TAR).resolve()
        if os.path.isfile(str(agent_tar_path)):
            logger.info("Checking for a recent update of agent tar")
            last_modified_time = time.time() - os.path.getmtime(str(agent_tar_path))
            logger.info("Modified {} seconds ago".format(last_modified_time))
            if last_modified_time < 60:
                logger.info("agent tar modfied recently")
                file_modified = True
    except FileNotFoundError:
        logger.info("Tar file not found")
    return file_modified

def server_monitor_thread():
    global server_sock
    logger.info(
        "Clean up Thread started.Clean up happens when there is no fault running for 5 minutes and when the mangle"
        " finishes querying the status of faults triggered on this endpoint")
    status_check_timeout = 0
    time.sleep(300)
    while True:
        if server_sock:
            logger.info("Checks for stopping agent and removing agent files")
            if not infraagent_helper.is_faults_running() and not check_agent_running() and not is_tar_modified_recently():
                if not infraagent_helper.has_completed_faults() or status_check_timeout == 6:
                    logger.info("Stopping server as no faults running for a while")
                    try:
                        server_sock.shutdown(socket.SHUT_RDWR)
                    except (socket.error, OSError, ValueError):
                        pass
                    server_sock.close()
                    base_path = Path(__file__).parent
                    print(base_path)
                    try:
                        agent_tar_path = (base_path / ".."/ INFRA_AGENT_TAR).resolve()
                        if os.path.isfile(str(agent_tar_path)):
                            logger.info("Removing agent tar")
                            print("Removing agent tar")
                            remove_tarfile_cmd = infraagent_helper.is_sudo_available() + "rm -rf {}".format(agent_tar_path)
                            subprocess.call(remove_tarfile_cmd, shell=True)
                            #os.remove(str(agent_tar_path))
                    except FileNotFoundError:
                        logger.info("Tar file not found")
                    dir_path = os.path.dirname(os.path.realpath(__file__))
                    logger.info(dir_path)
                    for path in glob.glob(dir_path+"/*"):
                        if os.path.isdir(path):
                            shutil.rmtree(path, ignore_errors=True)
                        else:
                            if not path.endswith("infra_agent.log"):
                                os.remove(path)
                    break
                else:
                    status_check_timeout = status_check_timeout + 1
                    logger.info("Setting status check to {}".format(str(status_check_timeout)))
            else:
                status_check_timeout = 0
                logger.info("Resetting status check to 0")
        time.sleep(300)


my_path = os.path.abspath(os.path.dirname(__file__))
logging_path = os.path.join(my_path, "logging.ini")
#print(logging_path)
fileConfig(logging_path)
logger = logging.getLogger("python_agent")

if __name__ == '__main__':
    try:
        main()
    except Exception as ex:
        logger.exception("Exception in main()".format(str(ex)))
        sys.exit(1)
