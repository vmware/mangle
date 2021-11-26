# Infrastructure Faults

For **version 1.0**, Mangle supported the following types of infrastructure faults:&#x20;

1. CPU Fault
2. Memory Fault
3. Disk IO Fault
4. Kill Process Fault
5. Docker State Change Faults
6. Kubernetes Delete Resource Fault
7. Kubernetes Resource Not Ready Fault
8. vCenter Disk Fault
9. vCenter NIC Fault
10. vCenter VM State Change Fault

From **version 2.0**,  apart from the faults listed above, support has been extended to the following new faults:

1. File Handler Leak Fault
2. Disk Space Fault
3. Kernel Panic Fault
4. Network Faults: Packet Delay, Packet Duplication, Packet Loss, Packet Corruption
5. Kubernetes  Service Unavailable Fault
6. AWS EC2 State Change Fault
7. AWS EC2 Network Fault

Minor improvements have also been included for Kill Process Fault in version 2 of Mangle.

For **version 3.0**,  support has been extended to the following new faults:

1. Stop Service
2. Clock Skew
3. Network Partition
4. Kubernetes - Support for other resource types for the Delete Resource Fault
5. AWS EC2 Storage Fault
6. AWS RDS Faults
7. Azure Virtual Machine Faults - State Change, Network and Storage

Significant improvements have been made to the Mangle agent in **version 3.0** to improve polling of fault execution status and reduce pre-requisites when it comes to running faults.

## CPU Fault

CPU fault enables spiking cpu usage values for a selected endpoint by a percentage specified by the user. With the help of a timeout field the duration for the fault run can be specified after which Mangle triggers the automatic remediation procedure.

**Steps to follow:**&#x20;

1. Login as a user with read and write privileges to Mangle.
2. Navigate to Fault Execution tab ---> Infrastructure Faults ---> CPU.
3. Select an Endpoint.
4. Provide a "CPU Load" value. For eg: 80 to simulate a CPU usage of 80% on the selected Endpoint.
5. Provide "Injection Home Dir" only if you would like Mangle to push the script files needed to simulate the fault to a specific location on the endpoint. Else the default temp location will be used.
6. Provide a "Timeout" value in milliseconds. For eg: if you need the CPU load of 80% to be sustained for a duration of 1 hour then you should provide the timeout value as 3600000 (1 hour = 3600000 ms). After this duration, Mangle will ensure remediation of the fault without any manual intervention.
7. Schedule options are required only if the fault needs to be re-executed at regular intervals against an endpoint.
8. Tags are key value pairs that will be send to the active monitoring tool under Mangle Admin settings ---> Integrations ---> Metric Providers at the time of publishing events for fault injection and remediation. They are optional and you can choose to exclude this while running faults.
9. Supported notifiers include Slack channels that are configured under Mangle Admin settings ---> Integrations ---> Notifiers. This will enable Mangle to automatically publish status of fault injections to the appropriate Slack channels for monitoring purposes. They are optional and you can choose to exclude this while running faults.
10. Click on Run Fault.
11. The user will be re-directed to the Processed Requests section under Requests & Reports tab.
12. If Mangle was able to successfully trigger the fault, the status of the task will change to "INJECTED". The fault will continue to run at the endpoint until the timeout expires or a remediation request is triggered. The option to trigger a remediation request at anytime can be found on clicking the ![](../../.gitbook/assets/actions\_button.png) button against the task in the Processed Requests table. The task will be updated to "COMPLETED" once the task is auto remediated or manually remediated before the fault timeout.&#x20;
13. For monitoring purposes, log into either Wavefront or Datadog once it is configured as an active Metric provider in Mangle and refer to the Events section. Events similar to the screenshots provided below will be available on the monitoring tool for tracking purposes.

    &#x20;![](../../.gitbook/assets/datadogevents.png)&#x20;



    &#x20; ![](../../.gitbook/assets/wavefrontevents.png)&#x20;

## Memory Fault

Memory fault enables spiking memory usage values for a selected endpoint by a percentage specified by the user. With the help of a timeout field the duration for the fault run can be specified after which Mangle triggers the automatic remediation procedure.

**Steps to follow:**&#x20;

1. Login as a user with read and write privileges to Mangle.
2. Navigate to Fault Execution tab ---> Infrastructure Faults ---> Memory.
3. Select an Endpoint.
4. Provide a "Memory Load" value. For eg: 80 to simulate a Memory usage of 80% on the selected Endpoint.
5. Provide "Injection Home Dir" only if you would like Mangle to push the script files needed to simulate the fault to a specific location on the endpoint. Else the default temp location will be used.
6. Provide a "Timeout" value in milliseconds. For eg: if you need the Memory load of 80% to be sustained for a duration of 1 hour then you should provide the timeout value as 3600000 (1 hour = 3600000 ms). After this duration, Mangle will ensure remediation of the fault without any manual intervention.
7. Schedule options are required only if the fault needs to be re-executed at regular intervals against an endpoint.
8. Tags are key value pairs that will be send to the active monitoring tool under Mangle Admin settings ---> Integrations ---> Metric Providers at the time of publishing events for fault injection and remediation. They are optional and you can choose to exclude this while running faults.
9. Supported notifiers include Slack channels that are configured under Mangle Admin settings ---> Integrations ---> Notifiers. This will enable Mangle to automatically publish status of fault injections to the appropriate Slack channels for monitoring purposes. They are optional and you can choose to exclude this while running faults.
10. Click on Run Fault.
11. The user will be re-directed to the Processed Requests section under Requests & Reports tab.
12. If Mangle was able to successfully trigger the fault, the status of the task will change to "INJECTED". The fault will continue to run at the endpoint until the timeout expires or a remediation request is triggered. The option to trigger a remediation request at anytime can be found on clicking the ![](../../.gitbook/assets/actions\_button.png) button against the task in the Processed Requests table. The task will be updated to "COMPLETED" once the task is auto remediated or manually remediated before the fault timeout.&#x20;
13. For monitoring purposes, log into either Wavefront or Datadog once it is configured as an active Metric provider in Mangle and refer to the Events section. Events similar to the screenshots provided below will be available on the monitoring tool for tracking purposes.

    &#x20;![](../../.gitbook/assets/datadogevents.png)&#x20;



    &#x20; ![](../../.gitbook/assets/wavefrontevents.png)&#x20;

## Disk IO Fault

Disk IO fault enables spiking disk IO operation for a selected endpoint by an IO size specified by the user in bytes. With the help of a timeout field the duration for the fault run can be specified after which Mangle triggers the automatic remediation procedure.

**Steps to follow:**&#x20;

1. Login as a user with read and write privileges to Mangle.
2. Navigate to Fault Execution tab ---> Infrastructure Faults ---> Disk IO.
3. Select an Endpoint.
4. Provide a "IO Size" value in bytes. For eg: To write in blocks of 5 KB to the disk of the selected Endpoint specify the IO Size as 5120 (5 KB = 5120 bytes). With the specified block size of 5120 bytes, Mangle will not use more than 5 MB (5 MB = 5120 \* 1024 bytes) of disk space during the simulation of fault. The space is cleared at the time of fault remediation.
5. Provide "Injection Home Dir" only if you would like Mangle to push the script files needed to simulate the fault to a specific location on the endpoint. Else the default temp location will be used.
6. Provide a "Timeout" value in milliseconds. For eg: if you need the IO load of 8192000 to be sustained for a duration of 1 hour then you should provide the timeout value as 3600000 (1 hour = 3600000 ms). After this duration, Mangle will ensure remediation of the fault without any manual intervention.
7. Schedule options are required only if the fault needs to be re-executed at regular intervals against an endpoint.
8. Tags are key value pairs that will be send to the active monitoring tool under Mangle Admin settings ---> Integrations ---> Metric Providers at the time of publishing events for fault injection and remediation. They are optional and you can choose to exclude this while running faults.
9. Supported notifiers include Slack channels that are configured under Mangle Admin settings ---> Integrations ---> Notifiers. This will enable Mangle to automatically publish status of fault injections to the appropriate Slack channels for monitoring purposes. They are optional and you can choose to exclude this while running faults.
10. Click on Run Fault.
11. The user will be re-directed to the Processed Requests section under Requests & Reports tab.
12. If Mangle was able to successfully trigger the fault, the status of the task will change to "INJECTED". The fault will continue to run at the endpoint until the timeout expires or a remediation request is triggered. The option to trigger a remediation request at anytime can be found on clicking the ![](../../.gitbook/assets/actions\_button.png) button against the task in the Processed Requests table. The task will be updated to "COMPLETED" once the task is auto remediated or manually remediated before the fault timeout.&#x20;
13. For monitoring purposes, log into either Wavefront or Datadog once it is configured as an active Metric provider in Mangle and refer to the Events section. Events similar to the screenshots provided below will be available on the monitoring tool for tracking purposes.

    &#x20;![](../../.gitbook/assets/datadogevents.png)&#x20;



    &#x20; ![](../../.gitbook/assets/wavefrontevents.png)&#x20;

## Kill Process Fault

Kill Process fault enables abrupt termination of any process that is running on the specified endpoint. Unlike other infrastructure faults like CPU, Memory and Disk IO this fault does not have a timeout field because the fault completes very quickly. Some processes/services may be configured for auto-start and some might require a manual start command to be executed. For the first case, auto-remediation through Mangle is not needed. For the second case, you can specify the remediation command that Mangle should use to start the process again. After the fault in completed and if remediation command was accurately specified, a manual remediation can be triggered from the Requests and Reports tab.

**Steps to follow:**&#x20;

1. Login as a user with read and write privileges to Mangle.
2. Navigate to Fault Execution tab ---> Infrastructure Faults ---> Kill Process.
3. Select an Endpoint.
4. Provide a "Process Identifier". This can either be a process id or process name. A process name is preferred if the fault is expected to be scheduled.
5. From version 2.0 onward,  Kill Process fault can  kill multiple processes with the same  process descriptor. This can be done by setting the "Kill All" drop down to true. If set to false, it will fail if the process descriptor is not unique. Alternatively, you can also use the process id to uniquely identify and kill a process.
6. Provide a "Remediation Command". For eg: To start the sshd process that was killed on an Ubuntu 17 Server, specify the remediation command as _"sudo service ssh start" ._
7. Provide "Injection Home Dir" only if you would like Mangle to push the script files needed to simulate the fault to a specific location on the endpoint. Else the default temp location will be used.
8. Schedule options are required only if the fault needs to be re-executed at regular intervals against an endpoint.
9. Tags are key value pairs that will be send to the active monitoring tool under Mangle Admin settings ---> Integrations ---> Metric Providers at the time of publishing events for fault injection and remediation. They are optional and you can choose to exclude this while running faults.
10. Supported notifiers include Slack channels that are configured under Mangle Admin settings ---> Integrations ---> Notifiers. This will enable Mangle to automatically publish status of fault injections to the appropriate Slack channels for monitoring purposes. They are optional and you can choose to exclude this while running faults.
11. Click on Run Fault.
12. The user will be re-directed to the Processed Requests section under Requests & Reports tab.
13. If Mangle was able to successfully trigger the fault, the status of the task will change to "INJECTED". The fault will continue to run at the endpoint until the timeout expires or a remediation request is triggered. The option to trigger a remediation request at anytime can be found on clicking the ![](../../.gitbook/assets/actions\_button.png) button against the task in the Processed Requests table. The task will be updated to "COMPLETED" once the task is auto remediated or manually remediated before the fault timeout.&#x20;
14. For monitoring purposes, log into either Wavefront or Datadog once it is configured as an active Metric provider in Mangle and refer to the Events section. Events similar to the screenshots provided below will be available on the monitoring tool for tracking purposes.

&#x20;![](../../.gitbook/assets/datadogevents.png)&#x20;



&#x20; ![](../../.gitbook/assets/wavefrontevents.png)&#x20;

## ![](../../.gitbook/assets/new\_logo.png) Stop Service Fault

Stop service fault enables graceful shutdown of any process that is running on the specified endpoint using the appropriate stop commands. This fault supports only on the Remote Machine Endpoint. With the help of a timeout field, the duration for the fault run can be specified after which Mangle triggers the automatic remediation procedure.

**Steps to follow:**&#x20;

1. Login as a user with read and write privileges to Mangle.
2. Navigate to Fault Execution tab ---> Infrastructure Faults ---> Stop Service.
3. Select an Endpoint.
4. Provide a "Service Name".&#x20;
5. Provide "Injection Home Dir" only if you would like Mangle to push the script files needed to simulate the fault to a specific location on the endpoint. Else the default temp location will be used.
6. Provide a "Timeout" value in milliseconds. For eg: if you need the Service to be stopped for a duration of 1 hour, then you should provide the timeout value as 3600000 (1 hour = 3600000 ms). After this duration, Mangle will ensure remediation of the fault without any manual intervention.
7. Schedule options are required only if the fault needs to be re-executed at regular intervals against an endpoint.
8. Tags are key value pairs that will be sent to the active monitoring tool under Mangle Admin settings ---> Integrations ---> Metric Providers at the time of publishing events for fault injection and remediation. They are optional and you can choose to exclude this while running faults.
9. Supported notifiers include Slack channels that are configured under Mangle Admin settings ---> Integrations ---> Notifiers. This will enable Mangle to automatically publish status of fault injections to the appropriate Slack channels for monitoring purposes. They are optional and you can choose to exclude this while running faults.
10. Click on Run Fault.
11. The user will be re-directed to the Processed Requests section under Requests & Reports tab.
12. If Mangle was able to successfully trigger the fault, the status of the task will change to "INJECTED". The fault will continue to run at the endpoint until the timeout expires or a remediation request is triggered. The option to trigger a remediation request at anytime can be found on clicking the ![](../../.gitbook/assets/actions\_button.png) button against the task in the Processed Requests table. The task will be updated to "COMPLETED" once the task is auto remediated or manually remediated before the fault timeout.&#x20;
13. For monitoring purposes, log into either Wavefront or Datadog once it is configured as an active Metric provider in Mangle and refer to the Events section. Events similar to the screenshots provided below will be available on the monitoring tool for tracking purposes.

    &#x20;![](../../.gitbook/assets/datadogevents.png)&#x20;



    &#x20; ![](../../.gitbook/assets/wavefrontevents.png)&#x20;

## File Handler Leak Fault

File Handler Leak fault enables you to simulate conditions where a program requests for a handle to a resource but does not release it when the resource is no longer in use. This condition if left over extended periods of time, will lead to "Too many open file handles" errors and will cause performance degradation or crashes. With the help of a timeout field the duration for the fault run can be specified after which Mangle triggers the automatic remediation procedure.

**Steps to follow:**&#x20;

1. Login as a user with read and write privileges to Mangle.
2. Navigate to Fault Execution tab ---> Infrastructure Faults ---> File Handler Leak.
3. Select an Endpoint.
4. Provide "Injection Home Dir" only if you would like Mangle to push the script files needed to simulate the fault to a specific location on the endpoint. Else the default temp location will be used.
5. Provide a "Timeout" value in milliseconds. For eg: if you need the out of file handles error to be sustained for a duration of 1 hour then you should provide the timeout value as 3600000 (1 hour = 3600000 ms). After this duration, Mangle will ensure remediation of the fault without any manual intervention.
6. Schedule options are required only if the fault needs to be re-executed at regular intervals against an endpoint.
7. Tags are key value pairs that will be send to the active monitoring tool under Mangle Admin settings ---> Integrations ---> Metric Providers at the time of publishing events for fault injection and remediation. They are optional and you can choose to exclude this while running faults.
8. Supported notifiers include Slack channels that are configured under Mangle Admin settings ---> Integrations ---> Notifiers. This will enable Mangle to automatically publish status of fault injections to the appropriate Slack channels for monitoring purposes. They are optional and you can choose to exclude this while running faults.
9. Click on Run Fault.
10. The user will be re-directed to the Processed Requests section under Requests & Reports tab.
11. If Mangle was able to successfully trigger the fault, the status of the task will change to "INJECTED". The fault will continue to run at the endpoint until the timeout expires or a remediation request is triggered. The option to trigger a remediation request at anytime can be found on clicking the ![](../../.gitbook/assets/actions\_button.png) button against the task in the Processed Requests table. The task will be updated to "COMPLETED" once the task is auto remediated or manually remediated before the fault timeout.&#x20;
12. For monitoring purposes, log into either Wavefront or Datadog once it is configured as an active Metric provider in Mangle and refer to the Events section. Events similar to the screenshots provided below will be available on the monitoring tool for tracking purposes.



    &#x20;![](../../.gitbook/assets/datadogevents.png)&#x20;



    &#x20; ![](../../.gitbook/assets/wavefrontevents.png)&#x20;

## Disk Space Fault

Disk Space Fault enables you to simulate out of disk or low disk space conditions. With the help of a timeout field the duration for the fault run can be specified after which Mangle triggers the automatic remediation procedure.

**Steps to follow:**&#x20;

1. Login as a user with read and write privileges to Mangle.
2. Navigate to Fault Execution tab ---> Infrastructure Faults ---> Disk Space.
3. Select an Endpoint.
4. Provide a "Target Directory" so Mangle can target a specific directory location or partition to write to for simulating the low disk space condition.&#x20;
5. Provide a "Load" value. For eg: 80 to simulate a Disk usage of 80% of the total disk size or space allocated for a partition, on the selected Endpoint.
6. Provide "Injection Home Dir" only if you would like Mangle to push the script files needed to simulate the fault to a specific location on the endpoint. Else the default temp location will be used.
7. Provide a "Timeout" value in milliseconds. For eg: if you need the low disk or out of disk condition to be sustained for a duration of 1 hour then you should provide the timeout value as 3600000 (1 hour = 3600000 ms). After this duration, Mangle will ensure remediation of the fault without any manual intervention.
8. Schedule options are required only if the fault needs to be re-executed at regular intervals against an endpoint.
9. Tags are key value pairs that will be send to the active monitoring tool under Mangle Admin settings ---> Integrations ---> Metric Providers at the time of publishing events for fault injection and remediation. They are optional and you can choose to exclude this while running faults.
10. Supported notifiers include Slack channels that are configured under Mangle Admin settings ---> Integrations ---> Notifiers. This will enable Mangle to automatically publish status of fault injections to the appropriate Slack channels for monitoring purposes. They are optional and you can choose to exclude this while running faults.
11. Click on Run Fault.
12. The user will be re-directed to the Processed Requests section under Requests & Reports tab.
13. If Mangle was able to successfully trigger the fault, the status of the task will change to "INJECTED". The fault will continue to run at the endpoint until the timeout expires or a remediation request is triggered. The option to trigger a remediation request at anytime can be found on clicking the ![](../../.gitbook/assets/actions\_button.png) button against the task in the Processed Requests table. The task will be updated to "COMPLETED" once the task is auto remediated or manually remediated before the fault timeout.&#x20;
14. For monitoring purposes, log into either Wavefront or Datadog once it is configured as an active Metric provider in Mangle and refer to the Events section. Events similar to the screenshots provided below will be available on the monitoring tool for tracking purposes.



    &#x20;![](../../.gitbook/assets/datadogevents.png)&#x20;



    &#x20; ![](../../.gitbook/assets/wavefrontevents.png)&#x20;

## Kernel Panic Fault

Kernel Panic Fault simulates conditions where the operating system abruptly stops to prevent further damages, security breaches or data corruption and facilitate diagnosis of a sudden hardware or software failure.&#x20;

{% hint style="info" %}
**REMEDIATION OPTIONS FOR KERNEL PANIC**

Remediation for Kernel Panic is controlled by the operating system itself.  Typically on Linux systems, Kernel Panic is usually followed by a system reboot. But in some cases due to the settings specified under file  /etc/sysctl.d/99-sysctl.conf the automatic system reboot may not occur. For such cases, a manual reboot needs to be triggered on the endpoint to  bring it back to a usable state.

To modify this setting as a one-time option, please run the following command on the endpoint  `sysctl --system`

To modify this setting permanently, remotely log in to the endpoint, modify file   /etc/sysctl.d/99-sysctl.conf and add the following command

`kernel.panic = 20`
{% endhint %}

**Steps to follow:**&#x20;

1. Login as a user with read and write privileges to Mangle.
2. Navigate to Fault Execution tab ---> Infrastructure Faults ---> Kernel Panic.
3. Select an Endpoint.
4. Provide "Injection Home Dir" only if you would like Mangle to push the script files needed to simulate the fault to a specific location on the endpoint. Else the default temp location will be used.
5. Schedule options are required only if the fault needs to be re-executed at regular intervals against an endpoint.
6. Tags are key value pairs that will be send to the active monitoring tool under Mangle Admin settings ---> Integrations ---> Metric Providers at the time of publishing events for fault injection and remediation. They are optional and you can choose to exclude this while running faults.
7. Supported notifiers include Slack channels that are configured under Mangle Admin settings ---> Integrations ---> Notifiers. This will enable Mangle to automatically publish status of fault injections to the appropriate Slack channels for monitoring purposes. They are optional and you can choose to exclude this while running faults.
8. Click on Run Fault.
9. The user will be re-directed to the Processed Requests section under Requests & Reports tab.
10. If Mangle was able to successfully trigger the fault, the status of the task will change to "INJECTED". The fault will continue to run at the endpoint until the timeout expires or a remediation request is triggered. The option to trigger a remediation request at anytime can be found on clicking the ![](../../.gitbook/assets/actions\_button.png) button against the task in the Processed Requests table. The task will be updated to "COMPLETED" once the task is auto remediated or manually remediated before the fault timeout.&#x20;
11. For monitoring purposes, log into either Wavefront or Datadog once it is configured as an active Metric provider in Mangle and refer to the Events section. Events similar to the screenshots provided below will be available on the monitoring tool for tracking purposes.



    &#x20;![](../../.gitbook/assets/datadogevents.png)&#x20;



    &#x20; ![](../../.gitbook/assets/wavefrontevents.png)&#x20;

## ![](../../.gitbook/assets/new\_logo.png) Clock Skew Fault

Clock Skew Fault simulates conditions where the endpoint time is distorted and doesn't align with the standard NTP time. You can simulate conditions where the time of the endpoint is set in the future or in the past. The skew can be in seconds, minutes, hours or days as specified at the time of running the fault.

**Steps to follow:**&#x20;

1. Login as a user with read and write privileges to Mangle.
2. Navigate to Fault Execution tab ---> Infrastructure Faults ---> Clock Skew.
3. Select an Endpoint. Only remote machine and remote machine clusters are supported for this fault.
4. Provide "Injection Home Dir" only if you would like Mangle to push the script files needed to simulate the fault to a specific location on the endpoint. Else the default temp location will be used.
5. Select a skew operation. It can either set to a Past time or a Future time.
6. Set the skew time by specifying the seconds, minutes, hours and days or a combination of these options.
7. Provide a "Timeout" value in milliseconds. For eg: if you need the clock skew condition to be sustained for a duration of 1 hour then you should provide the timeout value as 3600000 (1 hour = 3600000 ms). After this duration, Mangle will ensure remediation of the fault without any manual intervention.
8. Schedule options are required only if the fault needs to be re-executed at regular intervals against an endpoint.
9. Tags are key value pairs that will be send to the active monitoring tool under Mangle Admin settings ---> Integrations ---> Metric Providers at the time of publishing events for fault injection and remediation. They are optional and you can choose to exclude this while running faults.
10. Supported notifiers include Slack channels that are configured under Mangle Admin settings ---> Integrations ---> Notifiers. This will enable Mangle to automatically publish status of fault injections to the appropriate Slack channels for monitoring purposes. They are optional and you can choose to exclude this while running faults.
11. Click on Run Fault.
12. The user will be re-directed to the Processed Requests section under Requests & Reports tab.
13. If Mangle was able to successfully trigger the fault, the status of the task will change to "INJECTED". The fault will continue to run at the endpoint until the timeout expires or a remediation request is triggered. The option to trigger a remediation request at anytime can be found on clicking the ![](../../.gitbook/assets/actions\_button.png) button against the task in the Processed Requests table. The task will be updated to "COMPLETED" once the task is auto remediated or manually remediated before the fault timeout.&#x20;
14. For monitoring purposes, log into either Wavefront or Datadog once it is configured as an active Metric provider in Mangle and refer to the Events section. Events similar to the screenshots provided below will be available on the monitoring tool for tracking purposes.



    &#x20;![](../../.gitbook/assets/datadogevents.png)&#x20;



    &#x20; ![](../../.gitbook/assets/wavefrontevents.png)&#x20;

## ![](../../.gitbook/assets/new\_logo.png) Network Partition Fault

Network Partition Fault simulates conditions where endpoints lose connectivity due to a network split primarily due to failures in underlying network devices. This induces cases where clustered setups lose nodes with impact to high availability, data consistency and end up in split brain scenario in the worst cases.

**Steps to follow:**&#x20;

1. Login as a user with read and write privileges to Mangle.
2. Navigate to Fault Execution tab ---> Infrastructure Faults ---> Network Partition. Only remote machine and remote machine clusters are supported for this fault.
3. Select an Endpoint.
4. Provide a host IP or a list of host IPs to which the endpoint should lose network connectivity due to network partition.
5. If the single host IP provided is identical to the Endpoint host, it throws error at the injection of fault. Because, the Endpoint host and the host IP provided must be different.\
   But if user provides host IPs list and if a host IP is identical to the one in Endpoint host/ Endpoint group hosts, the fault injection proceeds by selecting the Endpoint -Host IP pair of the remaining list.
6. Provide "Injection Home Dir" only if you would like Mangle to push the script files needed to simulate the fault to a specific location on the endpoint. Else the default temp location will be used.
7. Provide a "Timeout" value in milliseconds. For eg: if you need the partitioning to be sustained for a duration of 1 hour then you should provide the timeout value as 3600000 (1 hour = 3600000 ms). After this duration, Mangle will ensure remediation of the fault without any manual intervention.
8. Schedule options are required only if the fault needs to be re-executed at regular intervals against an endpoint.
9. Tags are key value pairs that will be send to the active monitoring tool under Mangle Admin settings ---> Integrations ---> Metric Providers at the time of publishing events for fault injection and remediation. They are optional and you can choose to exclude this while running faults.
10. Supported notifiers include Slack channels that are configured under Mangle Admin settings ---> Integrations ---> Notifiers. This will enable Mangle to automatically publish status of fault injections to the appropriate Slack channels for monitoring purposes. They are optional and you can choose to exclude this while running faults.
11. Click on Run Fault.
12. The user will be re-directed to the Processed Requests section under Requests & Reports tab.
13. If Mangle was able to successfully trigger the fault, the status of the task will change to "INJECTED". The fault will continue to run at the endpoint until the timeout expires or a remediation request is triggered. The option to trigger a remediation request at anytime can be found on clicking the ![](../../.gitbook/assets/actions\_button.png) button against the task in the Processed Requests table. The task will be updated to "COMPLETED" once the task is auto remediated or manually remediated before the fault timeout.&#x20;
14. For monitoring purposes, log into either Wavefront or Datadog once it is configured as an active Metric provider in Mangle and refer to the Events section. Events similar to the screenshots provided below will be available on the monitoring tool for tracking purposes.



    &#x20;![](../../.gitbook/assets/datadogevents.png)&#x20;



    &#x20; ![](../../.gitbook/assets/wavefrontevents.png)&#x20;

## Network Faults

Network Faults enables you to simulate unfavorable conditions such as packet delay, packet duplication, packet loss and packet corruption. With the help of a timeout field the duration for the fault run can be specified after which Mangle triggers the automatic remediation procedure.

### Packet Delay

**Steps to follow:**&#x20;

1. Login as a user with read and write privileges to Mangle.
2. Navigate to Fault Execution tab ---> Infrastructure Faults ---> Network ---> Packet Delay.
3. Select an Endpoint.
4. Provide a "Nic Name".  For eg:  For a remote machine endpoint Nic name could be eth0, eth1 etc depending on what adapter you would want to target for the fault.
5. Provide a "Latency" value in milliseconds. For eg: 1000 to simulate a packet delay of 1 second on a particular network interface of an Endpoint.
6. Provide "Injection Home Dir" only if you would like Mangle to push the script files needed to simulate the fault to a specific location on the endpoint. Else the default temp location will be used.
7. Provide a "Timeout" value in milliseconds. For eg: if you need the packet delay to be sustained for a duration of 1 hour then you should provide the timeout value as 3600000 (1 hour = 3600000 ms). After this duration, Mangle will ensure remediation of the fault without any manual intervention.
8. Schedule options are required only if the fault needs to be re-executed at regular intervals against an endpoint.
9. Tags are key value pairs that will be send to the active monitoring tool under Mangle Admin settings ---> Integrations ---> Metric Providers at the time of publishing events for fault injection and remediation. They are optional and you can choose to exclude this while running faults.
10. Supported notifiers include Slack channels that are configured under Mangle Admin settings ---> Integrations ---> Notifiers. This will enable Mangle to automatically publish status of fault injections to the appropriate Slack channels for monitoring purposes. They are optional and you can choose to exclude this while running faults.
11. Click on Run Fault.
12. The user will be re-directed to the Processed Requests section under Requests & Reports tab.
13. If Mangle was able to successfully trigger the fault, the status of the task will change to "INJECTED". The fault will continue to run at the endpoint until the timeout expires or a remediation request is triggered. The option to trigger a remediation request at anytime can be found on clicking the ![](../../.gitbook/assets/actions\_button.png) button against the task in the Processed Requests table. The task will be updated to "COMPLETED" once the task is auto remediated or manually remediated before the fault timeout.&#x20;
14. For monitoring purposes, log into either Wavefront or Datadog once it is configured as an active Metric provider in Mangle and refer to the Events section. Events similar to the screenshots provided below will be available on the monitoring tool for tracking purposes.



    &#x20;![](../../.gitbook/assets/datadogevents.png)&#x20;



    &#x20; ![](../../.gitbook/assets/wavefrontevents.png)

### Packet Duplication

**Steps to follow:**&#x20;

1. Login as a user with read and write privileges to Mangle.
2. Navigate to Fault Execution tab ---> Infrastructure Faults ---> Network ---> Packet Duplicate.
3. Select an Endpoint.
4. Provide a "Nic Name".  For eg:  For a remote machine endpoint Nic name could be eth0, eth1 etc depending on what adapter you would want to target for the fault.
5. Provide a "Percentage" value to specify what percentage of the packets should be duplicated. For eg: 10 to simulate a packet duplication of 10 percentage on a particular network interface of an Endpoint.
6. Provide "Injection Home Dir" only if you would like Mangle to push the script files needed to simulate the fault to a specific location on the endpoint. Else the default temp location will be used.
7. Provide a "Timeout" value in milliseconds. For eg: if you need the packet duplication to be sustained for a duration of 1 hour then you should provide the timeout value as 3600000 (1 hour = 3600000 ms). After this duration, Mangle will ensure remediation of the fault without any manual intervention.
8. Schedule options are required only if the fault needs to be re-executed at regular intervals against an endpoint.
9. Tags are key value pairs that will be send to the active monitoring tool under Mangle Admin settings ---> Integrations ---> Metric Providers at the time of publishing events for fault injection and remediation. They are optional and you can choose to exclude this while running faults.
10. Supported notifiers include Slack channels that are configured under Mangle Admin settings ---> Integrations ---> Notifiers. This will enable Mangle to automatically publish status of fault injections to the appropriate Slack channels for monitoring purposes. They are optional and you can choose to exclude this while running faults.
11. Click on Run Fault.
12. The user will be re-directed to the Processed Requests section under Requests & Reports tab.
13. If Mangle was able to successfully trigger the fault, the status of the task will change to "INJECTED". The fault will continue to run at the endpoint until the timeout expires or a remediation request is triggered. The option to trigger a remediation request at anytime can be found on clicking the ![](../../.gitbook/assets/actions\_button.png) button against the task in the Processed Requests table. The task will be updated to "COMPLETED" once the task is auto remediated or manually remediated before the fault timeout.&#x20;
14. For monitoring purposes, log into either Wavefront or Datadog once it is configured as an active Metric provider in Mangle and refer to the Events section. Events similar to the screenshots provided below will be available on the monitoring tool for tracking purposes.



    &#x20;![](../../.gitbook/assets/datadogevents.png)&#x20;



    &#x20; ![](../../.gitbook/assets/wavefrontevents.png)&#x20;

### Packet Loss

**Steps to follow:**&#x20;

1. Login as a user with read and write privileges to Mangle.
2. Navigate to Fault Execution tab ---> Infrastructure Faults ---> Network ---> Packet Loss.
3. Select an Endpoint.
4. Provide a "Nic Name".  For eg:  For a remote machine endpoint Nic name could be eth0, eth1 etc depending on what adapter you would want to target for the fault.
5. Provide a "Percentage" value to specify what percentage of the packets should be dropped. For eg: 10 to simulate a packet drop of 10 percentage on a particular network interface of an Endpoint.
6. Provide "Injection Home Dir" only if you would like Mangle to push the script files needed to simulate the fault to a specific location on the endpoint. Else the default temp location will be used.
7. Provide a "Timeout" value in milliseconds. For eg: if you need the packet drop to be sustained for a duration of 1 hour then you should provide the timeout value as 3600000 (1 hour = 3600000 ms). After this duration, Mangle will ensure remediation of the fault without any manual intervention.
8. Schedule options are required only if the fault needs to be re-executed at regular intervals against an endpoint.
9. Tags are key value pairs that will be send to the active monitoring tool under Mangle Admin settings ---> Integrations ---> Metric Providers at the time of publishing events for fault injection and remediation. They are optional and you can choose to exclude this while running faults.
10. Supported notifiers include Slack channels that are configured under Mangle Admin settings ---> Integrations ---> Notifiers. This will enable Mangle to automatically publish status of fault injections to the appropriate Slack channels for monitoring purposes. They are optional and you can choose to exclude this while running faults.
11. Click on Run Fault.
12. The user will be re-directed to the Processed Requests section under Requests & Reports tab.
13. If Mangle was able to successfully trigger the fault, the status of the task will change to "INJECTED". The fault will continue to run at the endpoint until the timeout expires or a remediation request is triggered. The option to trigger a remediation request at anytime can be found on clicking the ![](../../.gitbook/assets/actions\_button.png) button against the task in the Processed Requests table. The task will be updated to "COMPLETED" once the task is auto remediated or manually remediated before the fault timeout.&#x20;
14. For monitoring purposes, log into either Wavefront or Datadog once it is configured as an active Metric provider in Mangle and refer to the Events section. Events similar to the screenshots provided below will be available on the monitoring tool for tracking purposes.



    &#x20;![](../../.gitbook/assets/datadogevents.png)&#x20;



    &#x20; ![](../../.gitbook/assets/wavefrontevents.png)&#x20;

### Packet Corruption

**Steps to follow:**&#x20;

1. Login as a user with read and write privileges to Mangle.
2. Navigate to Fault Execution tab ---> Infrastructure Faults ---> Network ---> Packet Corruption.
3. Select an Endpoint.
4. Provide a "Nic Name".  For eg:  For a remote machine endpoint Nic name could be eth0, eth1 etc depending on what adapter you would want to target for the fault.
5. Provide a "Percentage" value to specify what percentage of the packets should be corrupted. For eg: 10 to simulate a packet corruption of 10 percentage on a particular network interface of an Endpoint.
6. Provide "Injection Home Dir" only if you would like Mangle to push the script files needed to simulate the fault to a specific location on the endpoint. Else the default temp location will be used.
7. Provide a "Timeout" value in milliseconds. For eg: if you need the packet corruption to be sustained for a duration of 1 hour then you should provide the timeout value as 3600000 (1 hour = 3600000 ms). After this duration, Mangle will ensure remediation of the fault without any manual intervention.
8. Schedule options are required only if the fault needs to be re-executed at regular intervals against an endpoint.
9. Tags are key value pairs that will be send to the active monitoring tool under Mangle Admin settings ---> Integrations ---> Metric Providers at the time of publishing events for fault injection and remediation. They are optional and you can choose to exclude this while running faults.
10. Supported notifiers include Slack channels that are configured under Mangle Admin settings ---> Integrations ---> Notifiers. This will enable Mangle to automatically publish status of fault injections to the appropriate Slack channels for monitoring purposes. They are optional and you can choose to exclude this while running faults.
11. Click on Run Fault.
12. The user will be re-directed to the Processed Requests section under Requests & Reports tab.
13. If Mangle was able to successfully trigger the fault, the status of the task will change to "INJECTED". The fault will continue to run at the endpoint until the timeout expires or a remediation request is triggered. The option to trigger a remediation request at anytime can be found on clicking the ![](../../.gitbook/assets/actions\_button.png) button against the task in the Processed Requests table. The task will be updated to "COMPLETED" once the task is auto remediated or manually remediated before the fault timeout.&#x20;
14. For monitoring purposes, log into either Wavefront or Datadog once it is configured as an active Metric provider in Mangle and refer to the Events section. Events similar to the screenshots provided below will be available on the monitoring tool for tracking purposes.



    &#x20;![](../../.gitbook/assets/datadogevents.png)&#x20;



    &#x20; ![](../../.gitbook/assets/wavefrontevents.png)&#x20;

## Docker State Change

Docker State Change faults enable you to abruptly stop or pause containers running on a Docker host. Unlike other infrastructure faults like CPU, Memory and Disk IO this fault is specific to the Docker endpoint and does not have a timeout field because the fault completes very quickly. Some containers may be configured for auto-start and some might require a manual start command to be executed. For the first case, auto-remediation through Mangle is not needed. For the second case, a manual remediation can be triggered from the Requests and Reports tab after the fault completes.

**Steps to follow:**&#x20;

1. Login as a user with read and write privileges to Mangle.
2. Navigate to Fault Execution tab ---> Infrastructure Faults ---> Docker ---> State Change.
3. Select an Endpoint (Only Docker Endpoints are listed).
4. Select the fault.
5. Provide a "Container Name".
6. Schedule options are not available for this fault.
7. Tags are key value pairs that will be send to the active monitoring tool under Mangle Admin settings ---> Integrations ---> Metric Providers at the time of publishing events for fault injection and remediation. They are optional and you can choose to exclude this while running faults.
8. Supported notifiers include Slack channels that are configured under Mangle Admin settings ---> Integrations ---> Notifiers. This will enable Mangle to automatically publish status of fault injections to the appropriate Slack channels for monitoring purposes. They are optional and you can choose to exclude this while running faults.
9. Click on Run Fault.
10. The user will be re-directed to the Processed Requests section under Requests & Reports tab.
11. If Mangle was able to successfully trigger the fault, the status of the task will change to "INJECTED". The fault will continue to run at the endpoint until the timeout expires or a remediation request is triggered. The option to trigger a remediation request at anytime can be found on clicking the ![](../../.gitbook/assets/actions\_button.png) button against the task in the Processed Requests table. The task will be updated to "COMPLETED" once the task is auto remediated or manually remediated before the fault timeout.&#x20;
12. For monitoring purposes, log into either Wavefront or Datadog once it is configured as an active Metric provider in Mangle and refer to the Events section. Events similar to the screenshots provided below will be available on the monitoring tool for tracking purposes.

    &#x20;![](../../.gitbook/assets/datadogevents.png)&#x20;



    &#x20; ![](../../.gitbook/assets/wavefrontevents.png)&#x20;

## Kubernetes Delete Resource

Kubernetes (K8s) Delete Resource faults enable you to abruptly delete pods or nodes within a K8s cluster. Unlike other infrastructure faults like CPU, Memory and Disk IO this fault is specific to the K8s endpoint and does not have a timeout field because the fault completes very quickly. In most cases, K8s will automatically replace the deleted resource. This fault allows you see how the applications hosted on these pods behave in the event of rescheduling when a K8s resource is deleted and re-created.

**Steps to follow:**&#x20;

1. Login as a user with read and write privileges to Mangle.
2. Navigate to Fault Execution tab ---> Infrastructure Faults ---> K8S ---> Delete Resource.
3. Select an Endpoint (Only K8S endpoints are listed).
4. Select a Resource Type. Supported resource types include POD, NODE, SERVICE, DEPLOYMENT, STATEFULSET, SECRET, DAEMONSET, CONFIGMAP, JOB, REPLICASET, REPLICATIONCONTROLLER, PV, PVC.
5. Select a Resource identifier: Resource Name or Resource Labels.
6. If you choose Resource Name to identify a pod or a node, enter a string.
7. If you choose Resource Labels provide a key value pair for eg: app=mangle. Since multiple resources can have the same label, you also need to specify if you are interested in a Random Injection. If "Random Injection" is set to true, Mangle will randomly choose one resource in a list of resources identified using the label, for introducing the fault. If "Random Injection" is set to false, it will introduce fault into all resources identified using the resource label.
8. Schedule options are not available for this fault.
9. Tags are key value pairs that will be send to the active monitoring tool under Mangle Admin settings ---> Integrations ---> Metric Providers at the time of publishing events for fault injection and remediation. They are optional and you can choose to exclude this while running faults.
10. Supported notifiers include Slack channels that are configured under Mangle Admin settings ---> Integrations ---> Notifiers. This will enable Mangle to automatically publish status of fault injections to the appropriate Slack channels for monitoring purposes. They are optional and you can choose to exclude this while running faults.
11. Click on Run Fault.
12. The user will be re-directed to the Processed Requests section under Requests & Reports tab.
13. If Mangle was able to successfully trigger the fault, the status of the task will change to "INJECTED". The fault will continue to run at the endpoint until the timeout expires or a remediation request is triggered. The option to trigger a remediation request at anytime can be found on clicking the ![](../../.gitbook/assets/actions\_button.png) button against the task in the Processed Requests table. The task will be updated to "COMPLETED" once the task is auto remediated or manually remediated before the fault timeout.&#x20;
14. For monitoring purposes, log into either Wavefront or Datadog once it is configured as an active Metric provider in Mangle and refer to the Events section. Events similar to the screenshots provided below will be available on the monitoring tool for tracking purposes.

    &#x20;![](../../.gitbook/assets/datadogevents.png)&#x20;



    &#x20; ![](../../.gitbook/assets/wavefrontevents.png)&#x20;

## Kubernetes Resource Not Ready

Kubernetes (K8s) Resource Not Ready faults enable you to abruptly put pods or nodes within a K8s cluster into a state that is not suitable for scheduling.&#x20;

**Steps to follow:**&#x20;

1. Login as a user with read and write privileges to Mangle.
2. Navigate to Fault Execution tab ---> Infrastructure Faults ---> K8S ---> Delete Resource.
3. Select an Endpoint (Only K8S endpoints are listed).
4. Select a Resource Type: POD or NODE.
5. Select a Resource identifier: Resource Name or Resource Labels.
6. If you choose Resource Name to identify a pod or a node, enter a string.
7. If you choose Resource Labels provide a key value pair for eg: app=mangle. Since multiple resources can have the same label, you also need to specify if you are interested in a Random Injection. If "Random Injection" is set to true, Mangle will randomly choose one resource in a list of resources identified using the label, for introducing the fault. If "Random Injection" is set to false, it will introduce fault into all resources identified using the resource label.
8. Provide an app container name. Please note that the application specified should have a readiness probe configured for this fault to be triggered successfully.&#x20;
9. Schedule options are not available for this fault.
10. Tags are key value pairs that will be send to the active monitoring tool under Mangle Admin settings ---> Integrations ---> Metric Providers at the time of publishing events for fault injection and remediation. They are optional and you can choose to exclude this while running faults.
11. Supported notifiers include Slack channels that are configured under Mangle Admin settings ---> Integrations ---> Notifiers. This will enable Mangle to automatically publish status of fault injections to the appropriate Slack channels for monitoring purposes. They are optional and you can choose to exclude this while running faults.
12. Click on Run Fault.
13. The user will be re-directed to the Processed Requests section under Requests & Reports tab.
14. If Mangle was able to successfully trigger the fault, the status of the task will change to "INJECTED". The fault will continue to run at the endpoint until the timeout expires or a remediation request is triggered. The option to trigger a remediation request at anytime can be found on clicking the  ![](../../.gitbook/assets/actions\_button.png) button against the task in the Processed Requests table.
15. For monitoring purposes, log into either Wavefront or Datadog once it is configured as an active Metric provider in Mangle and refer to the Events section. Events similar to the screenshots provided below will be available on the monitoring tool for tracking purposes.

    &#x20;![](../../.gitbook/assets/datadogevents.png)&#x20;



    &#x20; ![](../../.gitbook/assets/wavefrontevents.png)

## Kubernetes Service Not Available

Kubernetes (K8s) Service Not Available faults enable you to abruptly make a service resource in K8s cluster not available, although the pod will be healthy and running.&#x20;

**Steps to follow:**&#x20;

1. Login as a user with read and write privileges to Mangle.
2. Navigate to Fault Execution tab ---> Infrastructure Faults ---> K8S ---> Service Unavailable.
3. Select an Endpoint (Only K8S endpoints are listed).
4. Choose the appropriate service identifier: Service Name or Service Labels.
5. If you choose Service Name, enter an appropriate string.
6. If you choose Service Labels provide a key value pair for eg: app=mangle. Since multiple resources can have the same label, you also need to specify if you are interested in a Random Injection. If "Random Injection" is set to true, Mangle will randomly choose one resource in a list of resources identified using the label, for introducing the fault. If "Random Injection" is set to false, it will introduce fault into all resources identified using the resource label.
7. Schedule options are not available for this fault.
8. Tags are key value pairs that will be send to the active monitoring tool under Mangle Admin settings ---> Integrations ---> Metric Providers at the time of publishing events for fault injection and remediation. They are optional and you can choose to exclude this while running faults.
9. Supported notifiers include Slack channels that are configured under Mangle Admin settings ---> Integrations ---> Notifiers. This will enable Mangle to automatically publish status of fault injections to the appropriate Slack channels for monitoring purposes. They are optional and you can choose to exclude this while running faults.
10. Click on Run Fault.
11. The user will be re-directed to the Processed Requests section under Requests & Reports tab.
12. If Mangle was able to successfully trigger the fault, the status of the task will change to "INJECTED". The fault will continue to run at the endpoint until the timeout expires or a remediation request is triggered. The option to trigger a remediation request at anytime can be found on clicking the ![](../../.gitbook/assets/actions\_button.png) button against the task in the Processed Requests table. The task will be updated to "COMPLETED" once the task is auto remediated or manually remediated before the fault timeout.&#x20;
13. For monitoring purposes, log into either Wavefront or Datadog once it is configured as an active Metric provider in Mangle and refer to the Events section. Events similar to the screenshots provided below will be available on the monitoring tool for tracking purposes.

    &#x20;![](../../.gitbook/assets/datadogevents.png)&#x20;



    &#x20; ![](../../.gitbook/assets/wavefrontevents.png)

## vCenter VM Disk Fault

vCenter VM Disk faults enable you to abruptly disconnect disks from a virtual machine in its inventory. It requires the VM Disk ID and VM Name to trigger the fault. For all vCenter faults, Mangle talks to the mangle-vc-adapter to connect and perform the required action on VC. So it is mandatory that you install the mangle-vc-adapter container prior to adding vCenter Endpoints or running vCenter faults. To find how to install and configure the mangle-vc-adapter, please refer [here](https://app.gitbook.com/@vmware-1/s/workspace/mangle-administration/supported-deployment-models#deploying-the-mangle-vcenter-adapter-container).

**Steps to follow:**&#x20;

1. Login as a user with read and write privileges to Mangle.
2. Navigate to Fault Execution tab ---> Infrastructure Faults ---> vCenter ---> VM ---> Storage.
3. Select an Endpoint (Only vCenter endpoints are listed).
4. Select the fault: Disconnect Disk.
5.  Provide the VM Name and VM Disk ID. To identify the disk id, the VM moid is required. This information can be gathered from the vCenter MOB (Managed Object Browser). Refer to [Looking up Managed Object Reference for vCenter](https://kb.vmware.com/s/article/1017126) for help on this. Once you have retrieved the VM moid, the disk id can be retrieved from the disk properties section in the link below after replacing the values in angle braces <>:

    https://_**\<VC\_SERVER>**_/mob/?moid=_**\<vm-moid>**_\&doPath=layout
6. With version 3.0, you can choose to leave the VM Disk ID field blank and set the Random Injection value to either true or false. If set to true, Mangle will pick one of the disks attached to the VM and will detach it. If set to false, Mangle will detach all disks attached to the VM.
7. With version 3.0, you can set a Filter to shortlist VMs from either a Datacenter, Cluster, Folder, Resource Pool or Host or a combination of these. This will optimize the search for the corresponding target VM.
8. Schedule options are not available for this fault.
9. Tags are key value pairs that will be send to the active monitoring tool under Mangle Admin settings ---> Integrations ---> Metric Providers at the time of publishing events for fault injection and remediation. They are optional and you can choose to exclude this while running faults.
10. Supported notifiers include Slack channels that are configured under Mangle Admin settings ---> Integrations ---> Notifiers. This will enable Mangle to automatically publish status of fault injections to the appropriate Slack channels for monitoring purposes. They are optional and you can choose to exclude this while running faults.
11. Click on Run Fault.
12. The user will be re-directed to the Processed Requests section under Requests & Reports tab.
13. If Mangle was able to successfully trigger the fault, the status of the task will change to "COMPLETED". The option to trigger a remediation request at anytime can be found on clicking the ![](../../.gitbook/assets/actions\_button.png) button against the task in the Processed Requests table. Please note that this applicable only to child tasks created under the vCenter fault injection parent task.
14. For monitoring purposes, log into either Wavefront or Datadog once it is configured as an active Metric provider in Mangle and refer to the Events section. Events similar to the screenshots provided below will be available on the monitoring tool for tracking purposes.

    &#x20;![](../../.gitbook/assets/datadogevents.png)&#x20;



    &#x20; ![](../../.gitbook/assets/wavefrontevents.png)

## vCenter NIC Fault

vCenter NIC faults enable you to abruptly disconnect network interface cards from a virtual machine in its inventory. It requires the VM Nic ID and VM Name to trigger the fault. For all vCenter faults, Mangle talks to the mangle-vc-adapter to connect and perform the required action on VC. So it is mandatory that you install the mangle-vc-adapter container prior to adding vCenter Endpoints or running vCenter faults. To find how to install and configure the mangle-vc-adapter, please refer [here](https://app.gitbook.com/@vmware-1/s/workspace/mangle-administration/supported-deployment-models#deploying-the-mangle-vcenter-adapter-container).

**Steps to follow:**&#x20;

1. Login as a user with read and write privileges to Mangle.
2. Navigate to Fault Execution tab ---> Infrastructure Faults ---> vCenter ---> VM ---> Network.
3. Select an Endpoint (Only vCenter endpoints are listed).
4. Select the fault: Disconnect Nic.
5.  Provide the VM Name and VM Nic ID. To identify the Nic id, the VM moid is required. This information can be gathered from the vCenter MOB (Managed Object Browser). Refer to [Looking up Managed Object Reference for vCenter](https://kb.vmware.com/s/article/1017126) for help on this. Once you have retrieved the VM moid, the Nic id can be retrieved from the deviceConfigId section in the link below after replacing the values in angle braces <>:

    https://_**\<VC\_SERVER>**_/mob/?moid=_**\<vm-moid>**_\&doPath=guest%2enet
6. With version 3.0, you can choose to leave the VM Nic ID field blank and set the Random Injection value to either true or false. If set to true, Mangle will pick one of the networks adapters attached to the VM and will detach it. If set to false, Mangle will detach all the network adapters attached to the VM.
7. With version 3.0, you can set a Filter to shortlist VMs from either a Datacenter, Cluster, Folder, Resource Pool or Host or a combination of these. This will optimize the search for the corresponding target VM.
8. Schedule options are not available for this fault.
9. Tags are key value pairs that will be send to the active monitoring tool under Mangle Admin settings ---> Integrations ---> Metric Providers at the time of publishing events for fault injection and remediation. They are optional and you can choose to exclude this while running faults.
10. Supported notifiers include Slack channels that are configured under Mangle Admin settings ---> Integrations ---> Notifiers. This will enable Mangle to automatically publish status of fault injections to the appropriate Slack channels for monitoring purposes. They are optional and you can choose to exclude this while running faults.
11. Click on Run Fault.
12. The user will be re-directed to the Processed Requests section under Requests & Reports tab.
13. If Mangle was able to successfully trigger the fault, the status of the task will change to "COMPLETED". The option to trigger a remediation request at anytime can be found on clicking the ![](../../.gitbook/assets/actions\_button.png) button against the task in the Processed Requests table. Please note that this applicable only to child tasks created under the vCenter fault injection parent task.
14. For monitoring purposes, log into either Wavefront or Datadog once it is configured as an active Metric provider in Mangle and refer to the Events section. Events similar to the screenshots provided below will be available on the monitoring tool for tracking purposes.

    &#x20;![](../../.gitbook/assets/datadogevents.png)&#x20;



    &#x20; ![](../../.gitbook/assets/wavefrontevents.png)

## vCenter VM State Change Fault

vCenter VM State Change faults enable you to abruptly power-off, reset or suspend any virtual machine in its inventory. It requires just the VM Name to trigger the fault. For all vCenter faults, Mangle talks to the mangle-vc-adapter to connect and perform the required action on VC. So it is mandatory that you install the mangle-vc-adapter container prior to adding vCenter Endpoints or running vCenter faults. To find how to install and configure the mangle-vc-adapter, please refer [here](https://app.gitbook.com/@vmware-1/s/workspace/mangle-administration/supported-deployment-models#deploying-the-mangle-vcenter-adapter-container).

**Steps to follow:**&#x20;

1. Login as a user with read and write privileges to Mangle.
2. Navigate to Fault Execution tab ---> Infrastructure Faults ---> vCenter ---> VM ---> State.
3. Select an Endpoint (Only vCenter endpoints are listed).
4. Select one of the faults: Poweroff, Suspend or Reset VM.
5. Provide the VM Name.
6. With version 3.0, you can choose to leave the VM Name field blank as well. Instead, you can set a Filter to shortlist VMs from either a Datacenter, Cluster, Folder, Resource Pool or Host or a combination of these. Random injection value can be set to either true or false. If set to true, Mangle will pick one of VMs in the filtered list for the fault else, it will run the fault again all the VMs in the filtered list.
7. Schedule options are not available for this fault.
8. Tags are key value pairs that will be send to the active monitoring tool under Mangle Admin settings ---> Integrations ---> Metric Providers at the time of publishing events for fault injection and remediation. They are optional and you can choose to exclude this while running faults.
9. Supported notifiers include Slack channels that are configured under Mangle Admin settings ---> Integrations ---> Notifiers. This will enable Mangle to automatically publish status of fault injections to the appropriate Slack channels for monitoring purposes. They are optional and you can choose to exclude this while running faults.
10. Click on Run Fault.
11. The user will be re-directed to the Processed Requests section under Requests & Reports tab.
12. If Mangle was able to successfully trigger the fault, the status of the task will change to "COMPLETED". The option to trigger a remediation request at anytime can be found on clicking the ![](../../.gitbook/assets/actions\_button.png) button against the task in the Processed Requests table. Please note that this applicable only to child tasks created under the vCenter fault injection parent task.
13. For monitoring purposes, log into either Wavefront or Datadog once it is configured as an active Metric provider in Mangle and refer to the Events section. Events similar to the screenshots provided below will be available on the monitoring tool for tracking purposes.

    &#x20;![](../../.gitbook/assets/datadogevents.png)&#x20;



    &#x20; ![](../../.gitbook/assets/wavefrontevents.png)

## ![](../../.gitbook/assets/new\_logo.png) vCenter Host Fault

vCenter Host fault enables you to abruptly disconnect a host under a datacenter, cluster or folder. For all vCenter faults, Mangle talks to the mangle-vc-adapter to connect and perform the required action on VC. So it is mandatory that you install the mangle-vc-adapter container prior to adding vCenter Endpoints or running vCenter faults. To find how to install and configure the mangle-vc-adapter, please refer [here](https://app.gitbook.com/@vmware-1/s/workspace/mangle-administration/supported-deployment-models#deploying-the-mangle-vcenter-adapter-container).

**Steps to follow:**&#x20;

1. Login as a user with read and write privileges to Mangle.
2. Navigate to Fault Execution tab ---> Infrastructure Faults ---> vCenter ---> Host.
3. Select an Endpoint (Only vCenter endpoints are listed).
4. Provide the Host Name. You can choose to leave the Host Name field blank as well. Instead, you can set a Filter to shortlist Hosts from either a Datacenter, Cluster or Folder or a combination of these. Random injection value can be set to either true or false. If set to true, Mangle will pick one of Hosts in the filtered list for the fault else, it will run the fault again all the Hosts in the filtered list.
5. Schedule options are not available for this fault.
6. Tags are key value pairs that will be send to the active monitoring tool under Mangle Admin settings ---> Integrations ---> Metric Providers at the time of publishing events for fault injection and remediation. They are optional and you can choose to exclude this while running faults.
7. Supported notifiers include Slack channels that are configured under Mangle Admin settings ---> Integrations ---> Notifiers. This will enable Mangle to automatically publish status of fault injections to the appropriate Slack channels for monitoring purposes. They are optional and you can choose to exclude this while running faults.
8. Click on Run Fault.
9. The user will be re-directed to the Processed Requests section under Requests & Reports tab.
10. If Mangle was able to successfully trigger the fault, the status of the task will change to "COMPLETED". The option to trigger a remediation request at anytime can be found on clicking the ![](../../.gitbook/assets/actions\_button.png) button against the task in the Processed Requests table. Please note that this applicable only to child tasks created under the vCenter fault injection parent task.
11. For monitoring purposes, log into either Wavefront or Datadog once it is configured as an active Metric provider in Mangle and refer to the Events section. Events similar to the screenshots provided below will be available on the monitoring tool for tracking purposes.

    &#x20;![](../../.gitbook/assets/datadogevents.png)&#x20;



    &#x20; ![](../../.gitbook/assets/wavefrontevents.png)

## AWS EC2 State Change Fault

AWS EC2 State Change fault enables you to abruptly terminate, stop or reboot any EC2 instance. It requires AWS tags to uniquely identify instances against which the fault should run.&#x20;

**Steps to follow:**&#x20;

1. Login as a user with read and write privileges to Mangle.
2. Navigate to Fault Execution tab ---> Infrastructure Faults ---> AWS ---> EC2 ---> State.
3. Select an Endpoint (Only AWS end points are listed).
4. Select one of the faults: Terminate\_Instances, Stop\_Instances, Reboot\_Instances.
5. Provide the AWS tag (key value pair to uniquely identify the instance(s). Since multiple instances can have the same tag, you also need to specify if you are interested in a Random Injection. If "Random Injection" is set to true, Mangle will randomly choose one instance from a list of instances identified using the tag, for injecting the fault. If "Random Injection" is set to false, it will inject the fault into all the instances identified using the tag.
6. Schedule options are not available for this fault.
7. Tags are key value pairs that will be send to the active monitoring tool under Mangle Admin settings ---> Integrations ---> Metric Providers at the time of publishing events for fault injection and remediation. They are optional and you can choose to exclude this while running faults.
8. Supported notifiers include Slack channels that are configured under Mangle Admin settings ---> Integrations ---> Notifier. This will enable Mangle to automatically publish status of fault injections to the appropriate Slack channels for monitoring purposes. They are optional and you can choose to exclude this while running faults.
9. Click on RUN FAULT button.
10. The user will be re-directed to the Processed Requests section under Requests & Reports tab.
11. If Mangle was able to successfully trigger the fault, the status of the task will change to "INJECTED". The fault will continue to run at the endpoint until the timeout expires or a remediation request is triggered.&#x20;
12. To check latest status of injected Fault, click on ENABLE AUTO REFRESH button.
13. The option to trigger a remediation request at anytime can be found on clicking the ![](../../.gitbook/assets/actions\_button.png) button against the task in the Processed Requests table. The task will be updated to "COMPLETED" once the task is auto remediated or manually remediated before the fault timeout.&#x20;
14. For monitoring purposes, log into either Wavefront or Datadog once it is configured as an active Metric provider in Mangle and refer to the Events section. Events similar to the screenshots provided below will be available on the monitoring tool for tracking purposes.

    &#x20;![](../../.gitbook/assets/datadogevents.png)&#x20;



    &#x20; ![](../../.gitbook/assets/wavefrontevents.png)

## AWS EC2 Network Fault

AWS EC2 Network fault enables you to block all network traffic to specific EC2 instances. It requires AWS tags to uniquely identify instances against which the fault should run.&#x20;

**Steps to follow:**&#x20;

1. Login as a user with read and write privileges to Mangle.
2. Navigate to Fault Execution tab ---> Infrastructure Faults ---> AWS ---> EC2 ---> Network.
3. Select an Endpoint (Only AWS end points are listed).
4. Select the fault: BLOCK\_ALL\_NETWORK\_TRAFFIC.
5. Provide the AWS tags (key value pair to uniquely identify the instance(s). Since multiple instances can have the same tag, you also need to specify if you are interested in a Random Injection. If "Random Injection" is set to true, Mangle will randomly choose one instance from a list of instances identified using the tags, for injecting the fault. If "Random Injection" is set to false, it will inject the fault into all the instances identified using the tags.
6. Schedule options are not available for this fault.
7. Tags are key value pairs that will be send to the active monitoring tool under Mangle Admin settings ---> Integrations ---> Metric Providers at the time of publishing events for fault injection and remediation. They are optional and you can choose to exclude this while running faults.
8. Supported notifiers include Slack channels that are configured under Mangle Admin settings ---> Integrations ---> Notifier. This will enable Mangle to automatically publish status of fault injections to the appropriate Slack channels for monitoring purposes. They are optional and you can choose to exclude this while running faults.
9. Click on RUN FAULT button.
10. The user will be re-directed to the Processed Requests section under Requests & Reports tab.
11. If Mangle was able to successfully trigger the fault, the status of the task will change to "INJECTED". The fault will continue to run at the endpoint until the timeout expires or a remediation request is triggered.&#x20;
12. To check latest status of injected fault, click on ENABLE AUTO REFRESH button.
13. The option to trigger a remediation request at anytime can be found on clicking the ![](../../.gitbook/assets/actions\_button.png) button against the task in the Processed Requests table. The task will be updated to "COMPLETED" once the task is auto remediated or manually remediated before the fault timeout.&#x20;
14. For monitoring purposes, log into either Wavefront or Datadog once it is configured as an active Metric provider in Mangle and refer to the Events section. Events similar to the screenshots provided below will be available on the monitoring tool for tracking purposes.

    &#x20;![](../../.gitbook/assets/datadogevents.png)&#x20;



    &#x20; ![](../../.gitbook/assets/wavefrontevents.png)

## ![](../../.gitbook/assets/new\_logo.png) AWS EC2 Storage Fault

AWS EC2 Storage fault enables you to detach all or one random volume attached to specific EC2 instances. It requires AWS tags to uniquely identify instances against which the fault should run.&#x20;

**Steps to follow:**&#x20;

1. Login as a user with read and write privileges to Mangle.
2. Navigate to Fault Execution tab ---> Infrastructure Faults ---> AWS ---> EC2 ---> Storage.
3. Select an Endpoint (Only AWS end points are listed).
4. Select the faults: DETACH\_VOLUMES.
5. Provide the AWS tags (key value pair to uniquely identify the instance(s). Since multiple instances can have the same tag, you also need to specify if you are interested in a Random Injection. If "Random Injection" is set to true, Mangle will randomly choose one instance from a list of instances identified using the tags, for injecting the fault. If "Random Injection" is set to false, it will inject the fault into all the instances identified using the tags.
6. Schedule options are not available for this fault.
7. Tags are key value pairs that will be send to the active monitoring tool under Mangle Admin settings ---> Integrations ---> Metric Providers at the time of publishing events for fault injection and remediation. They are optional and you can choose to exclude this while running faults.
8. Supported notifiers include Slack channels that are configured under Mangle Admin settings ---> Integrations ---> Notifier. This will enable Mangle to automatically publish status of fault injections to the appropriate Slack channels for monitoring purposes. They are optional and you can choose to exclude this while running faults.
9. Click on RUN FAULT button.
10. The user will be re-directed to the Processed Requests section under Requests & Reports tab.
11. If Mangle was able to successfully trigger the fault, the status of the task will change to "INJECTED". The fault will continue to run at the endpoint until the timeout expires or a remediation request is triggered.
12. To check latest status of injected fault, click on ENABLE AUTO REFRESH button.
13. &#x20;The option to trigger a remediation request at anytime can be found on clicking the ![](../../.gitbook/assets/actions\_button.png) button against the task in the Processed Requests table. The task will be updated to "COMPLETED" once the task is auto remediated or manually remediated before the fault timeout.&#x20;
14. For monitoring purposes, log into either Wavefront or Datadog once it is configured as an active Metric provider in Mangle and refer to the Events section. Events similar to the screenshots provided below will be available on the monitoring tool for tracking purposes.

    &#x20;![](../../.gitbook/assets/datadogevents.png)&#x20;



    &#x20; ![](../../.gitbook/assets/wavefrontevents.png)

## ![](../../.gitbook/assets/new\_logo.png) AWS RDS Faults

AWS RDS Faults enables you to stop, reboot, failover and induce connection loss to specific RDS instances. It requires AWS tags to uniquely identify instances against which the fault should run.&#x20;

**Steps to follow:**&#x20;

1. Login as a user with read and write privileges to Mangle.
2. Navigate to Fault Execution tab ---> Infrastructure Faults ---> AWS ---> RDS.
3. Select an Endpoint (Only AWS end points are listed).
4. Select one of the faults to run against the RDS instance: STOP\_INSTANCES, REBOOT\_INSTANCES, FAILOVER\_INSTANCES_ _or_ _CONNECTION\_LOSS.
5. Provide the appropriate DB identifiers.
6. If "Random Injection" is set to true, Mangle will randomly choose one of the DB instances that is identified using the DB identifier. If "Random Injection" is set to false, it will introduce fault into all the instances.
7. Schedule options are not available for this fault.
8. Tags are key value pairs that will be send to the active monitoring tool under Mangle Admin settings ---> Integrations ---> Metric Providers at the time of publishing events for fault injection and remediation. They are optional and you can choose to exclude this while running faults.
9. Supported notifiers include Slack channels that are configured under Mangle Admin settings ---> Integrations ---> Notifiers. This will enable Mangle to automatically publish status of fault injections to the appropriate Slack channels for monitoring purposes. They are optional and you can choose to exclude this while running faults.
10. Click on RUN FAULT button.
11. The user will be re-directed to the Processed Requests section under Requests & Reports tab.
12. If Mangle was able to successfully trigger the fault, the status of the task will change to "INJECTED". The fault will continue to run at the endpoint until the timeout expires or a remediation request is triggered.&#x20;
13. To check latest status of injected fault, click on ENABLE AUTO REFRESH button
14. The option to trigger a remediation request at anytime can be found on clicking the ![](../../.gitbook/assets/actions\_button.png) button against the task in the Processed Requests table. The task will be updated to "COMPLETED" once the task is auto remediated or manually remediated before the fault timeout.&#x20;
15. For monitoring purposes, log into either Wavefront or Datadog once it is configured as an active Metric provider in Mangle and refer to the Events section. Events similar to the screenshots provided below will be available on the monitoring tool for tracking purposes.

    &#x20;![](../../.gitbook/assets/datadogevents.png)&#x20;



    &#x20; ![](../../.gitbook/assets/wavefrontevents.png)

## ![](../../.gitbook/assets/new\_logo.png) Azure Virtual Machine State Change Fault

Azure Virtual Machine State Change fault enables you to abruptly delete, stop or reboot any Virtual Machine instance on Azure. It requires Azure tags to uniquely identify instances against which the fault should run.&#x20;

**Steps to follow:**&#x20;

1. Login as a user with read and write privileges to Mangle.
2. Navigate to Fault Execution tab ---> Infrastructure Faults ---> Azure ---> Virtual Machine---> State.
3. Select an Endpoint (Only Azure accounts are listed).
4. Select one of the faults: Delete\_VMs, Stop\_VMs_, _Restart\_VMs.
5. Provide the Azure tag (key value pair to uniquely identify the instance(s). Since multiple instances can have the same tag, you also need to specify if you are interested in a Random Injection. If "Random Injection" is set to true, Mangle will randomly choose one instance from a list of instances identified using the tag, for introducing the fault. If "Random Injection" is set to false, it will introduce the fault into all the instances identified using the tag.
6. Schedule options are not available for this fault.
7. Tags are key value pairs that will be send to the active monitoring tool under Mangle Admin settings ---> Integrations ---> Metric Providers at the time of publishing events for fault injection and remediation. They are optional and you can choose to exclude this while running faults.
8. Supported notifiers include Slack channels that are configured under Mangle Admin settings ---> Integrations ---> Notifiers. This will enable Mangle to automatically publish status of fault injections to the appropriate Slack channels for monitoring purposes. They are optional and you can choose to exclude this while running faults.
9. Click on Run Fault.
10. The user will be re-directed to the Processed Requests section under Requests & Reports tab.
11. If Mangle was able to successfully trigger the fault, the status of the task will change to "INJECTED". The fault will continue to run at the endpoint until the timeout expires or a remediation request is triggered. The option to trigger a remediation request at anytime can be found on clicking the ![](../../.gitbook/assets/actions\_button.png) button against the task in the Processed Requests table. The task will be updated to "COMPLETED" once the task is auto remediated or manually remediated before the fault timeout.&#x20;
12. For monitoring purposes, log into either Wavefront or Datadog once it is configured as an active Metric provider in Mangle and refer to the Events section. Events similar to the screenshots provided below will be available on the monitoring tool for tracking purposes.

    &#x20;![](../../.gitbook/assets/datadogevents.png)&#x20;



    &#x20; ![](../../.gitbook/assets/wavefrontevents.png)

## ![](../../.gitbook/assets/new\_logo.png) Azure Virtual Machine Network Fault

Azure Virtual Machine Network fault enables you to block all network traffic to specific VM instances on Azure.  It requires Azure tags to uniquely identify instances against which the fault should run.&#x20;

**Steps to follow:**&#x20;

1. Login as a user with read and write privileges to Mangle.
2. Navigate to Fault Execution tab ---> Infrastructure Faults ---> Azure ---> Virtual Machine---> State.
3. Select an Endpoint (Only Azure accounts are listed).
4. Select one of the faults: Block\_All\_VM\_Network\_Traffic.
5. Provide the Azure tag (key value pair to uniquely identify the instance(s). Since multiple instances can have the same tag, you also need to specify if you are interested in a Random Injection. If "Random Injection" is set to true, Mangle will randomly choose one instance from a list of instances identified using the tag, for introducing the fault. If "Random Injection" is set to false, it will introduce the fault into all the instances identified using the tag.
6. Schedule options are not available for this fault.
7. Tags are key value pairs that will be send to the active monitoring tool under Mangle Admin settings ---> Integrations ---> Metric Providers at the time of publishing events for fault injection and remediation. They are optional and you can choose to exclude this while running faults.
8. Supported notifiers include Slack channels that are configured under Mangle Admin settings ---> Integrations ---> Notifiers. This will enable Mangle to automatically publish status of fault injections to the appropriate Slack channels for monitoring purposes. They are optional and you can choose to exclude this while running faults.
9. Click on Run Fault.
10. The user will be re-directed to the Processed Requests section under Requests & Reports tab.
11. If Mangle was able to successfully trigger the fault, the status of the task will change to "INJECTED". The fault will continue to run at the endpoint until the timeout expires or a remediation request is triggered. The option to trigger a remediation request at anytime can be found on clicking the ![](../../.gitbook/assets/actions\_button.png) button against the task in the Processed Requests table. The task will be updated to "COMPLETED" once the task is auto remediated or manually remediated before the fault timeout.&#x20;
12. For monitoring purposes, log into either Wavefront or Datadog once it is configured as an active Metric provider in Mangle and refer to the Events section. Events similar to the screenshots provided below will be available on the monitoring tool for tracking purposes.

    &#x20;![](../../.gitbook/assets/datadogevents.png)&#x20;



    &#x20; ![](../../.gitbook/assets/wavefrontevents.png)

## ![](../../.gitbook/assets/new\_logo.png) Azure Virtual Machine Storage Fault

Azure Virtual Machine Storage fault enables you to detach all or one random volume attached to specific VM instances. It requires Azure tags to uniquely identify instances against which the fault should run.&#x20;

**Steps to follow:**&#x20;

1. Login as a user with read and write privileges to Mangle.
2. Navigate to Fault Execution tab ---> Infrastructure Faults ---> Azure---> VirtualMachine ---> Storage.
3. Select an Endpoint (Only Azure accounts are listed).
4. Select the faults: Detach\_Disks.
5. Provide the Azure tag (key value pair to uniquely identify the instance(s). Since multiple instances can have the same tag, you also need to specify if you are interested in a Random Injection. If "Random Injection" is set to true, Mangle will randomly choose one instance from a list of instances identified using the tag, for introducing the fault. If "Random Injection" is set to false, it will introduce the fault into all the instances identified using the tag.
6. Schedule options are not available for this fault.
7. Tags are key value pairs that will be send to the active monitoring tool under Mangle Admin settings ---> Integrations ---> Metric Providers at the time of publishing events for fault injection and remediation. They are optional and you can choose to exclude this while running faults.
8. Supported notifiers include Slack channels that are configured under Mangle Admin settings ---> Integrations ---> Notifiers. This will enable Mangle to automatically publish status of fault injections to the appropriate Slack channels for monitoring purposes. They are optional and you can choose to exclude this while running faults.
9. Click on Run Fault.
10. The user will be re-directed to the Processed Requests section under Requests & Reports tab.
11. If Mangle was able to successfully trigger the fault, the status of the task will change to "INJECTED". The fault will continue to run at the endpoint until the timeout expires or a remediation request is triggered. The option to trigger a remediation request at anytime can be found on clicking the ![](../../.gitbook/assets/actions\_button.png) button against the task in the Processed Requests table. The task will be updated to "COMPLETED" once the task is auto remediated or manually remediated before the fault timeout.&#x20;
12. For monitoring purposes, log into either Wavefront or Datadog once it is configured as an active Metric provider in Mangle and refer to the Events section. Events similar to the screenshots provided below will be available on the monitoring tool for tracking purposes.

    &#x20;![](../../.gitbook/assets/datadogevents.png)&#x20;



    &#x20; ![](../../.gitbook/assets/wavefrontevents.png)

## Relevant API Reference

{% hint style="info" %}
**For access to relevant API Swagger documentation:**

Please traverse to link** **![](../../.gitbook/assets/help.png) -----> API Documentation from the Mangle UI or access _https://\<Mangle IP or Hostname>/mangle-services/swagger-ui.html#_/_fault-injection-controller_

&#x20;![](broken-reference) ![](../../.gitbook/assets/faultinjectioncontroller.png)&#x20;
{% endhint %}
