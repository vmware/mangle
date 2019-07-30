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

### Cannot connect to adapter while adding a vCenter Endpoint

1. Verify if you are able to open up the vCenter adapter Swagger URL which is normally available  at  _https://&lt;docker host IP&gt;:8443/mangle-vc-adapter/swagger-ui.html_
2. Verify if they can get the health of the vCenter adapter from the mangle container using: 

   curl -k _https://&lt;docker host IP&gt;:8443/mangle-vc-adapter/application/health_

3. If  the first verification succeeds and the second fails, inter-container communication is blocker. So ensure that the port used for the vCenter adapter is open. If you have followed the mangle documentation this port is usually 8443.
4. If the first and second verification fails, check if the vCenter adapter container is up and running.

