# Boot/Initialization Stage

Boot involves the Mangle appliance powering on and the containers being setup.

At the end of boot or initialization phase, the Mangle application should be available at URL: https://_&lt;IP or Hostname provided&gt;_/mangle-services and the default admin user should be prompted to change password on login.

## **Boot Failures**

### **Support Information**

Provide the following information to support if encountering Boot Stage failures:

* Is this a fresh deploy or an upgrade?
  * If upgrading, what version is the old Mangle application?
* Were any changes made to the Mangle configuration at the time of deployment?
  * If yes, what changes?
* Are you able to view the web page at http://?
  * If no, did you provide custom TLS certificates during the Deployment Stage?
    * If yes and the version is 1.3.1 or less, verify the format is correct \[Certificate

      Reference\]\(\#additional-information\)

    * Run `journalctl -u fileserver`and provide the entire resulting output to support
  * If no, attempt SSH debugging in the following step.
* Are you able to SSH into the Mangle appliance? If no, continue with \[Network

  Troubleshooting\]\(\#network-troubleshooting\)

  * Please obtain a Mangle appliance support bundle

### **Network Troubleshooting**

* Does the VM console of the deployed Mangle appliance show an IP address? Is this address the expected

  value based on DHCP or provided static IP settings?

* Do you have a route to the deployed Mangle appliance's IP address?
  * `ping <Mangle appliance IP>`
    * If ping is successful, but SSH is not, check network firewall settings.
    * If ping is not successful, check network settings and continue with \[Console

      Troubleshooting\]\(\#console-troubleshooting\)

### **Console Troubleshooting**

The goal of this step is to be able to SSH to the Mangle appliance to allow for better debugging information to be obtained from the appliance.

* Access the vSphere console for the Mangle appliance. Press `ALT` + `F2` to access the login prompt.
  * Login with username `root` and the credentials you provided in the OVA deployment

    customization. If the deployment has failed to set your credentials, the default password is

    `vmware`.

  * Are there any startup components that failed to start? 
    * Run `docker ps`. It should list two or three containers in running state; mangle or mangleWEB, mangleDB and the mangle-vsphere-adapter.
      * If no, continue with the next steps. If DB container is not running execute:

        `docker start mangleDB`. Wait for 10-20 seconds and run `docker start mangleWEB`. Wait for a couple of seconds and see if the portal below can be reached.

        ```text
        https://<IP or Hostname provided>/mangle-services
        ```
  * Run `ip addr show`
    * Is the IP address the expected value based on DHCP or provided static IP settings?
  * Run `ip route show`
    * Is the default route valid?
  * Can you ping the default gateway? Run `ping <default gateway IP>`. Obtain the default gateway IP from the `ip route show` command output.
    * If no, check your network settings. Attach the Mangle appliance to a network that has a valid

      route between your client and the appliance.

    * If yes, verify the routing configuration between the client that is unable to SSH to the mangle appliance.
  * If still unable to SSH to the Mangle appliance, provide the output of the following commands to

    support:

    * `docker start mangleDB`
    * `docker start mangleWEB`
    * `ip addr show`
    * `ip route show`
    * `ping <default gateway IP>`

