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

It is important to determine what stage in the appliance lifecycle you are at when encountering issues so that targeted troubleshooting steps can be followed. Please use the links below to identify what stage the failure is in, to apply the appropriate troubleshooting steps, and to provide the appropriate troubleshooting information for support.

{% page-ref page="deployment-stage.md" %}

{% page-ref page="boot-stage.md" %}

{% page-ref page="endpoint-addition-stage.md" %}

{% page-ref page="fault-injection-stage.md" %}

