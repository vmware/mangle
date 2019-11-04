

# Known Issues and Limitations

## Version 2.0 (Released 4th November 2019)

### Deploying a multi node cluster of Mangle on Kubernetes may not work as expected

There is a known limitation in deploying a Mangle multi node cluster on Kubernetes. So we recommend that you deploy only a single node setup of Mangle on Kubernetes.

### Invalid message in response of API call when user is not logged in

The current description field in the response shows invalid username and password instead of user is unauthorized.

### The processed requests page takes a while to load

Depending on the number of requests to load, the page might take anywhere between 5 to 25 seconds to load. This is due to server pagination limitations.

### FileHandler leak fault failing on machines with optimized shell configured as default shell

FileHandler leak fault injection can sometimes fail if the endpoint that it runs against has an optimized shell configured as the default shell.

### Root password provided at the time of OVA deployment is not actually changed

The default root password will not be over written even though a new password is given at the time of Mangle ova deployment.

### Custom plugin execution UI throws a 'getRequestJson.faultName: must not be blank' message even before the user attempts to execute the Fault

This error can be ignored.

### Retrigger of certain application faults fail with a command not found error/exception

This error is displayed if the same fault is already running against the same endpoint.

### While injecting a fault, it fails with an "Agent JAR loaded but agent failed to initialize" error

The above issue occurs when a user trying to inject a fault onto a java service without remediating the fault that is already running. To successfully inject, the user should be aware of byteman port used earlier.

### Manual remediation of faults doesn't work for K8s endpoint

This is a known issue. To workaround this use the timeout field to auto-remediate the fault.

### Remediation of K8S Resource Not Ready fault does not work when random injection flag is set as false

