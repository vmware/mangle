# Troubleshooting Guide

This guide will help you to diagnose common issues with Mangle deployment and operation and determine what information to collect for further debugging.

### General Support Information

This information about the environment and events that occurred leading to the failure _should be included in every support request to assist in debugging_.

_Basic Information_

REQUIRED:

* Endpoint type:
* Details of Fault that was injected:
* Type of Deployment: OVA, Container \(Single Node\), Container \(Multi Node\), Container \(Multi Node with HA Proxy\)
* What stage of the Mangle lifecycle are you running into the issue?
* Attach the Mangle support bundle

OPTIONAL, but helpful:

* IP address of Mangle:
* Hostname of Mangle:
* IP address of Endpoint:
* Hostname of Endpoint:

_Detailed Information_

* What operation was being performed when the failure was noticed?
* Provide information from the `Support Information` section of the appropriate Mangle Lifecycle stage
* Provide additional detail as necessary

#### Mangle Support Bundle

Please run `/etc/vmware/support/mangle-support.sh` and provide the resulting file to support. This script gathers application state and log information and is the best tool for gathering comprehensive support information. Provide this output for all support requests along with the _Support Information_ from the corresponding stage of the Appliance Lifecycle.

The location of the resulting log bundle is shown in the script output, similar to this example:

```text
Created log bundle /storage/log/mangle_logs_2018-01-01-00-01-00.tar.gz
```

Provide this `.tar.gz` file to support.

### Mangle Lifecycle

It is important to determine what stage in the appliance lifecycle you are at when encountering issues so that targeted troubleshooting steps can be followed. Please use the following names to describe what stage the failure is in, to apply the appropriate troubleshooting steps, and to provide the appropriate troubleshooting information to support.

#### Deployment Stage

Deployment involves deploying the Mangle appliance OVA or the containers.

During Deployment, the user provides customization such as configuring the root password and other optional configurations such as providing TLS certificates.

**Deployment Failures**

We have not experienced many failures during Deployment. If any issues occur, provide the Support Information from below.

**Support Information**

Provide the following information to support if encountering Deployment Stage failures:

* Hash \(MD5, SHA-1, or SHA-256\) of the OVA/container images you deployed
* Deployment method:
* Deployment environment
  * Verify that the targeted datastore has enough space
  * Provide details about the targeted vCenter compute, storage, and networking

#### Boot Stage

**Boot Failures**

**Support Information**

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

**Network Troubleshooting**

* Does the VM console of the deployed Mangle appliance show an IP address? Is this address the expected

  value based on DHCP or provided static IP settings?

* Do you have a route to the deployed Mangle appliance's IP address?
  * `ping <Mangle appliance IP>`
    * If ping is successful, but SSH is not, check network firewall settings.
    * If ping is not successful, check network settings and continue with \[Console

      Troubleshooting\]\(\#console-troubleshooting\)

**Console Troubleshooting**

The goal of this step is to be able to SSH to the Mangle appliance to allow for better debugging information to be obtained from the appliance.

* Access the vSphere console for the Mangle appliance. Press `ALT` + `->` to access the login prompt.
  * Login with username `root` and the credentials you provided in the OVA deployment

    customizations. If the deployment has failed to set your credentials, the default password is

    `VMw@re!23`.

  * Are there any startup components that failed to start? Run `systemctl list-units --state=failed`
    * If no, continue with the next steps. If there are any failed units, provide the output of the

      following commands to support:

      * `systemctl list-units --state=failed`
        * For each failed unit: `journalctl -u <unit name>`
      * `ip addr show`
      * `ip route show`
  * Run `ip addr show`
    * Is the IP address the expected value based on DHCP or provided static IP settings?
  * Run `ip route show`
    * Is the default route valid?
  * Can you ping the default gateway? Run `ping <default gateway IP>`. Obtain the default gateway IP from the `ip route show` command output.
    * If no, check your network settings. Attach the Mangle appliance to a network that has a valid

      route between your client and the appliance.

    * If yes, verify the routing configuration between the client that is unable to SSH to the mangle appliance.
  * If still unable to SSH to the VIC appliance, provide the output of the following commands to

    support:

    * `systemctl list-units --state=failed`
      * For each failed unit: `journalctl -u <unit name>`
    * `ip addr show`
    * `ip route show`

#### Initialization Stage

**Initialization Failures**

#### Running Stage

Reaching the Running Stage means that you can successfully view the Password reset page at http://. The Mangle application is ready for use.

**Running Failures**

**Support Information**

#### End Point Addition Stage

#### Fault Injection Stage

