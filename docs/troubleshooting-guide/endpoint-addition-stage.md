# Endpoint Addition Stage

Endpoint addition is the first step to starting your chaos engineering experiments. It helps you set up the targets for the experiment.

Usually you encounter failures in this stage if Mangle has some difficulty reaching the endpoint either due to network connectivity issues, blocked ports or restrictive firewalls, bad credentials or wrong IP/Hostname values.

## **Common Error Codes and Next Steps**

### **FIRM01: Connection Refused**

1. Usually affects a remote machine endpoint.
2. Ensure that the machine is remotely accessible by running the ping command.
3. Ensure that the ssh service is running and the credentials are correct.

### **FIRM03: Mangle requires file transfer access**

1. Usually affects a remote machine endpoint.
2. Ensure that the sftp configuration on the remote machine is correct.
3. Ensure that the ssh service is running and the credentials are correct.

