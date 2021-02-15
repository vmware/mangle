import socket
import sys
from pathlib import Path


def main():
    command = '::'.join([str(elem) for elem in sys.argv])
    #print(command)
    client = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    fault_args = parse_args(command)
    if "--agentPort" in fault_args:
        server_port = int(fault_args['--agentPort'])
    else:
        base_path = Path(__file__).parent
        #print(base_path)
        path = (base_path / "portnumber.txt").resolve()
        # path = (base_path / "../mangleinfraagent/portnumber.txt").resolve()
        try:
            with open(str(path), 'r') as f:
                server_port = f.read()
        except IOError as ex:
            print("Unable to retrieve port Exception: {}".format(ex))
            sys.exit(1)
    #print(server_port)
    try:
        ip_add = get_host_ip_address()
        client.connect((ip_add, int(server_port)))
        client.sendall(bytes(command, 'UTF-8'))
    except socket.error as exc:
        print("Exception: {}".format(exc))
        sys.exit(1)
    while True:
        in_data = client.recv(1024)
        #print("From Server---- :", in_data.decode())
        if 'Fault Injection Triggered' in in_data.decode():
            print("Injection output:", in_data.decode())
            return
        elif 'Received Remediation Request Successfully' in in_data.decode():
            print("Remediation output:", in_data.decode())
            return
        elif 'Status is' in in_data.decode():
            print(in_data.decode())
            return in_data.decode()
        elif 'Requested Fault is already Remediated' in in_data.decode():
            raise Exception(in_data.decode())
        elif 'Exception:' in in_data.decode():
            raise Exception(in_data.decode())
        else:
            break
    client.close()


def get_host_ip_address():
    ip_add = '127.0.0.1'
    #print("IP", ip_add)
    return ip_add


def parse_args(command):
    fault_args = {}
    args_list = command.split("::")
    for i in range(len(args_list)):
        if args_list[i].startswith("--"):
            fault_args[args_list[i]] = args_list[i + 1]
    return fault_args


if __name__ == '__main__':
    try:
        main()
    except Exception as ex:
        print(ex)
        sys.exit(1)
