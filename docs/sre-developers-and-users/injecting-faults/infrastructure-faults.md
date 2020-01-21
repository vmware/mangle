# Infrastructure Faults

For **version 1.0**, Mangle supported the following types of infrastructure faults: 

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

## CPU Fault

CPU fault enables spiking cpu usage values for a selected endpoint by a percentage specified by the user. With the help of a timeout field the duration for the fault run can be specified after which Mangle triggers the automatic remediation procedure.

**Steps to follow:** 

1. Login as a user with read and write privileges to Mangle.
2. Navigate to Fault Execution tab ---&gt; Infrastructure Faults ---&gt; CPU.
3. Select an Endpoint.
4. Provide a "CPU Load" value. For eg: 80 to simulate a CPU usage of 80% on the selected Endpoint.
5. Provide "Injection Home Dir" only if you would like Mangle to push the script files needed to simulate the fault to a specific location on the endpoint. Else the default temp location will be used.
6. Provide a "Timeout" value in milliseconds. For eg: if you need the CPU load of 80% to be sustained for a duration of 1 hour then you should provide the timeout value as 3600000 \(1 hour = 3600000 ms\). After this duration, Mangle will ensure remediation of the fault without any manual intervention.
7. Schedule options are required only if the fault needs to be re-executed at regular intervals against an endpoint.
8. Tags are key value pairs that will be send to the active monitoring tool under Mangle Admin settings ---&gt; Metric Providers at the time of publishing events for fault injection and remediation. They are not mandatory.
9. Click on Run Fault.
10. The user will be re-directed to the Processed Requests section under Requests & Reports tab.
11. If Mangle was able to successfully trigger the fault, the status of the task will change to "COMPLETED". The fault will continue to run at the endpoint until the timeout expires or a remediation request is triggered. The option to trigger a remediation request at anytime can be found on clicking the ![](../../.gitbook/assets/supportedactionsbutton.png) button against the task in the Processed Requests table.
12. For monitoring purposes, log into either Wavefront or Datadog once it is configured as an active Metric provider in Mangle and refer to the Events section. Events similar to the screenshots provided below will be available on the monitoring tool for tracking purposes.

     ![](../../.gitbook/assets/datadogevents.png) 



      ![](../../.gitbook/assets/wavefrontevents.png) 

## Memory Fault

Memory fault enables spiking memory usage values for a selected endpoint by a percentage specified by the user. With the help of a timeout field the duration for the fault run can be specified after which Mangle triggers the automatic remediation procedure.

**Steps to follow:** 

1. Login as a user with read and write privileges to Mangle.
2. Navigate to Fault Execution tab ---&gt; Infrastructure Faults ---&gt; Memory.
3. Select an Endpoint.
4. Provide a "Memory Load" value. For eg: 80 to simulate a Memory usage of 80% on the selected Endpoint.
5. Provide "Injection Home Dir" only if you would like Mangle to push the script files needed to simulate the fault to a specific location on the endpoint. Else the default temp location will be used.
6. Provide a "Timeout" value in milliseconds. For eg: if you need the Memory load of 80% to be sustained for a duration of 1 hour then you should provide the timeout value as 3600000 \(1 hour = 3600000 ms\). After this duration, Mangle will ensure remediation of the fault without any manual intervention.
7. Schedule options are required only if the fault needs to be re-executed at regular intervals against an endpoint.
8. Tags are key value pairs that will be send to the active monitoring tool under Mangle Admin settings ---&gt; Metric Providers at the time of publishing events for fault injection and remediation. They are not mandatory.
9. Click on Run Fault.
10. The user will be re-directed to the Processed Requests section under Requests & Reports tab.
11. If Mangle was able to successfully trigger the fault, the status of the task will change to "COMPLETED". The fault will continue to run at the endpoint until the timeout expires or a remediation request is triggered. The option to trigger a remediation request at anytime can be found on clicking the ![](../../.gitbook/assets/supportedactionsbutton.png) button against the task in the Processed Requests table.
12. For monitoring purposes, log into either Wavefront or Datadog once it is configured as an active Metric provider in Mangle and refer to the Events section. Events similar to the screenshots provided below will be available on the monitoring tool for tracking purposes.

     ![](../../.gitbook/assets/datadogevents.png) 



      ![](../../.gitbook/assets/wavefrontevents.png) 

## Disk IO Fault

Disk IO fault enables spiking disk IO operation for a selected endpoint by an IO size specified by the user in bytes. With the help of a timeout field the duration for the fault run can be specified after which Mangle triggers the automatic remediation procedure.

**Steps to follow:** 

1. Login as a user with read and write privileges to Mangle.
2. Navigate to Fault Execution tab ---&gt; Infrastructure Faults ---&gt; Disk IO.
3. Select an Endpoint.
4. Provide a "IO Size" value in bytes. For eg: To write in blocks of 5 KB to the disk of the selected Endpoint specify the IO Size as 5120 \(5 KB = 5120 bytes\). With the specified block size of 5120 bytes, Mangle will not use more than 5 MB \(5 MB = 5120 \* 1024 bytes\) of disk space during the simulation of fault. The space is cleared at the time of fault remediation.
5. Provide "Injection Home Dir" only if you would like Mangle to push the script files needed to simulate the fault to a specific location on the endpoint. Else the default temp location will be used.
6. Provide a "Timeout" value in milliseconds. For eg: if you need the IO load of 8192000 to be sustained for a duration of 1 hour then you should provide the timeout value as 3600000 \(1 hour = 3600000 ms\). After this duration, Mangle will ensure remediation of the fault without any manual intervention.
7. Schedule options are required only if the fault needs to be re-executed at regular intervals against an endpoint.
8. Tags are key value pairs that will be send to the active monitoring tool under Mangle Admin settings ---&gt; Metric Providers at the time of publishing events for fault injection and remediation. They are not mandatory.
9. Click on Run Fault.
10. The user will be re-directed to the Processed Requests section under Requests & Reports tab.
11. If Mangle was able to successfully trigger the fault, the status of the task will change to "COMPLETED". The fault will continue to run at the endpoint until the timeout expires or a remediation request is triggered. The option to trigger a remediation request at anytime can be found on clicking the ![](../../.gitbook/assets/supportedactionsbutton.png) button against the task in the Processed Requests table.
12. For monitoring purposes, log into either Wavefront or Datadog once it is configured as an active Metric provider in Mangle and refer to the Events section. Events similar to the screenshots provided below will be available on the monitoring tool for tracking purposes.

     ![](../../.gitbook/assets/datadogevents.png) 



      ![](../../.gitbook/assets/wavefrontevents.png) 

## Kill Process Fault

Kill Process fault enables abrupt termination of any process that is running on the specified endpoint. Unlike other infrastructure faults like CPU, Memory and Disk IO this fault does not have a timeout field because the fault completes very quickly. Some processes/services may be configured for auto-start and some might require a manual start command to be executed. For the first case, auto-remediation through Mangle is not needed. For the second case, you can specify the remediation command that Mangle should use to start the process again. After the fault in completed and if remediation command was accurately specified, a manual remediation can be triggered from the Requests and Reports tab.

**Steps to follow:** 

1. Login as a user with read and write privileges to Mangle.
2. Navigate to Fault Execution tab ---&gt; Infrastructure Faults ---&gt; Kill Process.
3. Select an Endpoint.
4. Provide a "Process Identifier". This can either be a process id or process name. A process name is preferred if the fault is expected to be scheduled.
5. From version 2.0 onward,  Kill Process fault can  kill multiple processes with the same  process descriptor. This can be done by setting the "Kill All" drop down to true. If set to false, it will fail if the process descriptor is not unique. Alternatively, you can also use the process id to uniquely identify and kill a process.
6. Provide a "Remediation Command". For eg: To start the sshd process that was killed on an Ubuntu 17 Server, specify the remediation command as _"sudo service ssh start" ._
7. Provide "Injection Home Dir" only if you would like Mangle to push the script files needed to simulate the fault to a specific location on the endpoint. Else the default temp location will be used.
8. Schedule options are required only if the fault needs to be re-executed at regular intervals against an endpoint.
9. Tags are key value pairs that will be send to the active monitoring tool under Mangle Admin settings ---&gt; Metric Providers at the time of publishing events for fault injection and remediation. They are not mandatory.
10. Click on Run Fault.
11. The user will be re-directed to the Processed Requests section under Requests & Reports tab.
12. If Mangle was able to successfully trigger the fault, the status of the task will change to "COMPLETED". The option to trigger a remediation request at anytime can be found on clicking the ![](../../.gitbook/assets/supportedactionsbutton.png) button against the task in the Processed Requests table.
13. For monitoring purposes, log into either Wavefront or Datadog once it is configured as an active Metric provider in Mangle and refer to the Events section. Events similar to the screenshots provided below will be available on the monitoring tool for tracking purposes.

     ![](../../.gitbook/assets/datadogevents.png) 



      ![](../../.gitbook/assets/wavefrontevents.png) 

## File Handler Leak Fault

File Handler Leak fault enables you to simulate conditions where a program requests for a handle to a resource but does not release it when the resource is no longer in use. This condition if left over extended periods of time, will lead to "Too many open file handles" errors and will cause performance degradation or crashes. With the help of a timeout field the duration for the fault run can be specified after which Mangle triggers the automatic remediation procedure.

**Steps to follow:** 

1. Login as a user with read and write privileges to Mangle.
2. Navigate to Fault Execution tab ---&gt; Infrastructure Faults ---&gt; File Handler Leak.
3. Select an Endpoint.
4. Provide "Injection Home Dir" only if you would like Mangle to push the script files needed to simulate the fault to a specific location on the endpoint. Else the default temp location will be used.
5. Provide a "Timeout" value in milliseconds. For eg: if you need the out of file handles error to be sustained for a duration of 1 hour then you should provide the timeout value as 3600000 \(1 hour = 3600000 ms\). After this duration, Mangle will ensure remediation of the fault without any manual intervention.
6. Schedule options are required only if the fault needs to be re-executed at regular intervals against an endpoint.
7. Tags are key value pairs that will be send to the active monitoring tool under Mangle Admin settings ---&gt; Metric Providers at the time of publishing events for fault injection and remediation. They are not mandatory.
8. Click on Run Fault.
9. The user will be re-directed to the Processed Requests section under Requests & Reports tab.
10. If Mangle was able to successfully trigger the fault, the status of the task will change to "COMPLETED". The option to trigger a remediation request at anytime can be found on clicking the ![](../../.gitbook/assets/supportedactionsbutton.png) button against the task in the Processed Requests table.
11. For monitoring purposes, log into either Wavefront or Datadog once it is configured as an active Metric provider in Mangle and refer to the Events section. Events similar to the screenshots provided below will be available on the monitoring tool for tracking purposes.



     ![](../../.gitbook/assets/datadogevents.png) 



      ![](../../.gitbook/assets/wavefrontevents.png) 

## Disk Space Fault

Disk Space Fault enables you to simulate out of disk or low disk space conditions. With the help of a timeout field the duration for the fault run can be specified after which Mangle triggers the automatic remediation procedure.

**Steps to follow:** 

1. Login as a user with read and write privileges to Mangle.
2. Navigate to Fault Execution tab ---&gt; Infrastructure Faults ---&gt; Disk Space.
3. Select an Endpoint.
4. Provide a "Target Directory" so Mangle can target a specific directory location or partition to write to for simulating the low disk space condition. 
5. Provide a "Load" value. For eg: 80 to simulate a Disk usage of 80% of the total disk size or space allocated for a partition, on the selected Endpoint.
6. Provide "Injection Home Dir" only if you would like Mangle to push the script files needed to simulate the fault to a specific location on the endpoint. Else the default temp location will be used.
7. Provide a "Timeout" value in milliseconds. For eg: if you need the low disk or out of disk condition to be sustained for a duration of 1 hour then you should provide the timeout value as 3600000 \(1 hour = 3600000 ms\). After this duration, Mangle will ensure remediation of the fault without any manual intervention.
8. Schedule options are required only if the fault needs to be re-executed at regular intervals against an endpoint.
9. Tags are key value pairs that will be send to the active monitoring tool under Mangle Admin settings ---&gt; Metric Providers at the time of publishing events for fault injection and remediation. They are not mandatory.
10. Click on Run Fault.
11. The user will be re-directed to the Processed Requests section under Requests & Reports tab.
12. If Mangle was able to successfully trigger the fault, the status of the task will change to "COMPLETED". The option to trigger a remediation request at anytime can be found on clicking the ![](../../.gitbook/assets/supportedactionsbutton.png) button against the task in the Processed Requests table.
13. For monitoring purposes, log into either Wavefront or Datadog once it is configured as an active Metric provider in Mangle and refer to the Events section. Events similar to the screenshots provided below will be available on the monitoring tool for tracking purposes.



     ![](../../.gitbook/assets/datadogevents.png) 



      ![](../../.gitbook/assets/wavefrontevents.png) 

## Kernel Panic Fault

Kernel Panic Fault simulates conditions where the operating system abruptly stops to prevent further damages, security breaches or data corruption and facilitate diagnosis of a sudden hardware or software failure. 

{% hint style="info" %}
**REMEDIATION OPTIONS FOR KERNEL PANIC**

Remediation for Kernel Panic is controlled by the operating system itself.  Typically on Linux systems, Kernel Panic is usually followed by a system reboot. But in some cases due to the settings specified under file  /etc/sysctl.d/99-sysctl.conf the automatic system reboot may not occur. For such cases, a manual reboot needs to be triggered on the endpoint to  bring it back to a usable state.

To modify this setting as a one-time option, please run the following command on the endpoint  `sysctl --system`

To modify this setting permanently, remotely log in to the endpoint, modify file   /etc/sysctl.d/99-sysctl.conf and add the following command

`kernel.panic = 20`
{% endhint %}

**Steps to follow:** 

1. Login as a user with read and write privileges to Mangle.
2. Navigate to Fault Execution tab ---&gt; Infrastructure Faults ---&gt; Kernel Panic.
3. Select an Endpoint.
4. Provide "Injection Home Dir" only if you would like Mangle to push the script files needed to simulate the fault to a specific location on the endpoint. Else the default temp location will be used.
5. Schedule options are required only if the fault needs to be re-executed at regular intervals against an endpoint.
6. Tags are key value pairs that will be send to the active monitoring tool under Mangle Admin settings ---&gt; Metric Providers at the time of publishing events for fault injection and remediation. They are not mandatory.
7. Click on Run Fault.
8. The user will be re-directed to the Processed Requests section under Requests & Reports tab.
9. If Mangle was able to successfully trigger the fault, the status of the task will change to "COMPLETED". The option to trigger a remediation request at anytime can be found on clicking the ![](../../.gitbook/assets/supportedactionsbutton.png) button against the task in the Processed Requests table.
10. For monitoring purposes, log into either Wavefront or Datadog once it is configured as an active Metric provider in Mangle and refer to the Events section. Events similar to the screenshots provided below will be available on the monitoring tool for tracking purposes.



     ![](../../.gitbook/assets/datadogevents.png) 



      ![](../../.gitbook/assets/wavefrontevents.png) 

## Network Faults

Network Faults enables you to simulate unfavorable conditions such as packet delay, packet duplication, packet loss and packet corruption. With the help of a timeout field the duration for the fault run can be specified after which Mangle triggers the automatic remediation procedure.

### Packet Delay

**Steps to follow:** 

1. Login as a user with read and write privileges to Mangle.
2. Navigate to Fault Execution tab ---&gt; Infrastructure Faults ---&gt; Network ---&gt; Packet Delay.
3. Select an Endpoint.
4. Provide a "Nic Name".  For eg:  For a remote machine endpoint Nic name could be eth0, eth1 etc depending on what adapter you would want to target for the fault.
5. Provide a "Latency" value in milliseconds. For eg: 1000 to simulate a packet delay of 1 second on a particular network interface of an Endpoint.
6. Provide "Injection Home Dir" only if you would like Mangle to push the script files needed to simulate the fault to a specific location on the endpoint. Else the default temp location will be used.
7. Provide a "Timeout" value in milliseconds. For eg: if you need the packet delay to be sustained for a duration of 1 hour then you should provide the timeout value as 3600000 \(1 hour = 3600000 ms\). After this duration, Mangle will ensure remediation of the fault without any manual intervention.
8. Schedule options are required only if the fault needs to be re-executed at regular intervals against an endpoint.
9. Tags are key value pairs that will be send to the active monitoring tool under Mangle Admin settings ---&gt; Metric Providers at the time of publishing events for fault injection and remediation. They are not mandatory.
10. Click on Run Fault.
11. The user will be re-directed to the Processed Requests section under Requests & Reports tab.
12. If Mangle was able to successfully trigger the fault, the status of the task will change to "COMPLETED". The option to trigger a remediation request at anytime can be found on clicking the ![](../../.gitbook/assets/supportedactionsbutton.png) button against the task in the Processed Requests table.
13. For monitoring purposes, log into either Wavefront or Datadog once it is configured as an active Metric provider in Mangle and refer to the Events section. Events similar to the screenshots provided below will be available on the monitoring tool for tracking purposes.



     ![](../../.gitbook/assets/datadogevents.png) 



      ![](../../.gitbook/assets/wavefrontevents.png)

### Packet Duplication

**Steps to follow:** 

1. Login as a user with read and write privileges to Mangle.
2. Navigate to Fault Execution tab ---&gt; Infrastructure Faults ---&gt; Network ---&gt; Packet Duplicate.
3. Select an Endpoint.
4. Provide a "Nic Name".  For eg:  For a remote machine endpoint Nic name could be eth0, eth1 etc depending on what adapter you would want to target for the fault.
5. Provide a "Percentage" value to specify what percentage of the packets should be duplicated. For eg: 10 to simulate a packet duplication of 10 percentage on a particular network interface of an Endpoint.
6. Provide "Injection Home Dir" only if you would like Mangle to push the script files needed to simulate the fault to a specific location on the endpoint. Else the default temp location will be used.
7. Provide a "Timeout" value in milliseconds. For eg: if you need the packet duplication to be sustained for a duration of 1 hour then you should provide the timeout value as 3600000 \(1 hour = 3600000 ms\). After this duration, Mangle will ensure remediation of the fault without any manual intervention.
8. Schedule options are required only if the fault needs to be re-executed at regular intervals against an endpoint.
9. Tags are key value pairs that will be send to the active monitoring tool under Mangle Admin settings ---&gt; Metric Providers at the time of publishing events for fault injection and remediation. They are not mandatory.
10. Click on Run Fault.
11. The user will be re-directed to the Processed Requests section under Requests & Reports tab.
12. If Mangle was able to successfully trigger the fault, the status of the task will change to "COMPLETED". The option to trigger a remediation request at anytime can be found on clicking the ![](../../.gitbook/assets/supportedactionsbutton.png) button against the task in the Processed Requests table.
13. For monitoring purposes, log into either Wavefront or Datadog once it is configured as an active Metric provider in Mangle and refer to the Events section. Events similar to the screenshots provided below will be available on the monitoring tool for tracking purposes.



     ![](../../.gitbook/assets/datadogevents.png) 



      ![](../../.gitbook/assets/wavefrontevents.png) 

### Packet Loss

**Steps to follow:** 

1. Login as a user with read and write privileges to Mangle.
2. Navigate to Fault Execution tab ---&gt; Infrastructure Faults ---&gt; Network ---&gt; Packet Loss.
3. Select an Endpoint.
4. Provide a "Nic Name".  For eg:  For a remote machine endpoint Nic name could be eth0, eth1 etc depending on what adapter you would want to target for the fault.
5. Provide a "Percentage" value to specify what percentage of the packets should be dropped. For eg: 10 to simulate a packet drop of 10 percentage on a particular network interface of an Endpoint.
6. Provide "Injection Home Dir" only if you would like Mangle to push the script files needed to simulate the fault to a specific location on the endpoint. Else the default temp location will be used.
7. Provide a "Timeout" value in milliseconds. For eg: if you need the packet drop to be sustained for a duration of 1 hour then you should provide the timeout value as 3600000 \(1 hour = 3600000 ms\). After this duration, Mangle will ensure remediation of the fault without any manual intervention.
8. Schedule options are required only if the fault needs to be re-executed at regular intervals against an endpoint.
9. Tags are key value pairs that will be send to the active monitoring tool under Mangle Admin settings ---&gt; Metric Providers at the time of publishing events for fault injection and remediation. They are not mandatory.
10. Click on Run Fault.
11. The user will be re-directed to the Processed Requests section under Requests & Reports tab.
12. If Mangle was able to successfully trigger the fault, the status of the task will change to "COMPLETED". The option to trigger a remediation request at anytime can be found on clicking the ![](../../.gitbook/assets/supportedactionsbutton.png) button against the task in the Processed Requests table.
13. For monitoring purposes, log into either Wavefront or Datadog once it is configured as an active Metric provider in Mangle and refer to the Events section. Events similar to the screenshots provided below will be available on the monitoring tool for tracking purposes.



     ![](../../.gitbook/assets/datadogevents.png) 



      ![](../../.gitbook/assets/wavefrontevents.png) 

### Packet Corruption

**Steps to follow:** 

1. Login as a user with read and write privileges to Mangle.
2. Navigate to Fault Execution tab ---&gt; Infrastructure Faults ---&gt; Network ---&gt; Packet Corruption.
3. Select an Endpoint.
4. Provide a "Nic Name".  For eg:  For a remote machine endpoint Nic name could be eth0, eth1 etc depending on what adapter you would want to target for the fault.
5. Provide a "Percentage" value to specify what percentage of the packets should be corrupted. For eg: 10 to simulate a packet corruption of 10 percentage on a particular network interface of an Endpoint.
6. Provide "Injection Home Dir" only if you would like Mangle to push the script files needed to simulate the fault to a specific location on the endpoint. Else the default temp location will be used.
7. Provide a "Timeout" value in milliseconds. For eg: if you need the packet corruption to be sustained for a duration of 1 hour then you should provide the timeout value as 3600000 \(1 hour = 3600000 ms\). After this duration, Mangle will ensure remediation of the fault without any manual intervention.
8. Schedule options are required only if the fault needs to be re-executed at regular intervals against an endpoint.
9. Tags are key value pairs that will be send to the active monitoring tool under Mangle Admin settings ---&gt; Metric Providers at the time of publishing events for fault injection and remediation. They are not mandatory.
10. Click on Run Fault.
11. The user will be re-directed to the Processed Requests section under Requests & Reports tab.
12. If Mangle was able to successfully trigger the fault, the status of the task will change to "COMPLETED". The option to trigger a remediation request at anytime can be found on clicking the ![](../../.gitbook/assets/supportedactionsbutton.png) button against the task in the Processed Requests table.
13. For monitoring purposes, log into either Wavefront or Datadog once it is configured as an active Metric provider in Mangle and refer to the Events section. Events similar to the screenshots provided below will be available on the monitoring tool for tracking purposes.



     ![](../../.gitbook/assets/datadogevents.png) 



      ![](../../.gitbook/assets/wavefrontevents.png) 

## Docker State Change

Docker State Change faults enable you to abruptly stop or pause containers running on a Docker host. Unlike other infrastructure faults like CPU, Memory and Disk IO this fault is specific to the Docker endpoint and does not have a timeout field because the fault completes very quickly. Some containers may be configured for auto-start and some might require a manual start command to be executed. For the first case, auto-remediation through Mangle is not needed. For the second case, a manual remediation can be triggered from the Requests and Reports tab after the fault completes.

**Steps to follow:** 

1. Login as a user with read and write privileges to Mangle.
2. Navigate to Fault Execution tab ---&gt; Infrastructure Faults ---&gt; Docker ---&gt; State Change.
3. Select an Endpoint \(Only Docker Endpoints are listed\).
4. Select the fault.
5. Provide a "Container Name".
6. Schedule options are not available for this fault.
7. Tags are key value pairs that will be send to the active monitoring tool under Mangle Admin settings ---&gt; Metric Providers at the time of publishing events for fault injection and remediation. They are not mandatory.
8. Click on Run Fault.
9. The user will be re-directed to the Processed Requests section under Requests & Reports tab.
10. If Mangle was able to successfully trigger the fault, the status of the task will change to "COMPLETED". The option to trigger a remediation request at anytime can be found on clicking the ![](../../.gitbook/assets/supportedactionsbutton.png) button against the task in the Processed Requests table.
11. For monitoring purposes, log into either Wavefront or Datadog once it is configured as an active Metric provider in Mangle and refer to the Events section. Events similar to the screenshots provided below will be available on the monitoring tool for tracking purposes.

     ![](../../.gitbook/assets/datadogevents.png) 



      ![](../../.gitbook/assets/wavefrontevents.png) 

## Kubernetes Delete Resource

Kubernetes \(K8s\) Delete Resource faults enable you to abruptly delete pods or nodes within a K8s cluster. Unlike other infrastructure faults like CPU, Memory and Disk IO this fault is specific to the K8s endpoint and does not have a timeout field because the fault completes very quickly. In most cases, K8s will automatically replace the deleted resource. This fault allows you see how the applications hosted on these pods behave in the event of rescheduling when a K8s resource is deleted and re-created.

**Steps to follow:** 

1. Login as a user with read and write privileges to Mangle.
2. Navigate to Fault Execution tab ---&gt; Infrastructure Faults ---&gt; K8S ---&gt; Delete Resource.
3. Select an Endpoint \(Only K8S endpoints are listed\).
4. Select a Resource Type: POD or NODE.
5. Select a Resource identifier: Resource Name or Resource Labels.
6. If you choose Resource Name to identify a pod or a node, enter a string.
7. If you choose Resource Labels provide a key value pair for eg: app=mangle. Since multiple resources can have the same label, you also need to specify if you are interested in a Random Injection. If "Random Injection" is set to true, Mangle will randomly choose one resource in a list of resources identified using the label, for introducing the fault. If "Random Injection" is set to false, it will introduce fault into all resources identified using the resource label.
8. Schedule options are not available for this fault.
9. Tags are key value pairs that will be send to the active monitoring tool under Mangle Admin settings ---&gt; Metric Providers at the time of publishing events for fault injection and remediation. They are not mandatory.
10. Click on Run Fault.
11. The user will be re-directed to the Processed Requests section under Requests & Reports tab.
12. If Mangle was able to successfully trigger the fault, the status of the task will change to "COMPLETED". Remediation requests are not supported for this fault.
13. For monitoring purposes, log into either Wavefront or Datadog once it is configured as an active Metric provider in Mangle and refer to the Events section. Events similar to the screenshots provided below will be available on the monitoring tool for tracking purposes.

     ![](../../.gitbook/assets/datadogevents.png) 



      ![](../../.gitbook/assets/wavefrontevents.png) 

## Kubernetes Resource Not Ready

Kubernetes \(K8s\) Resource Not Ready faults enable you to abruptly put pods or nodes within a K8s cluster into a state that is not suitable for scheduling. 

**Steps to follow:** 

1. Login as a user with read and write privileges to Mangle.
2. Navigate to Fault Execution tab ---&gt; Infrastructure Faults ---&gt; K8S ---&gt; Delete Resource.
3. Select an Endpoint \(Only K8S endpoints are listed\).
4. Select a Resource Type: POD or NODE.
5. Select a Resource identifier: Resource Name or Resource Labels.
6. If you choose Resource Name to identify a pod or a node, enter a string.
7. If you choose Resource Labels provide a key value pair for eg: app=mangle. Since multiple resources can have the same label, you also need to specify if you are interested in a Random Injection. If "Random Injection" is set to true, Mangle will randomly choose one resource in a list of resources identified using the label, for introducing the fault. If "Random Injection" is set to false, it will introduce fault into all resources identified using the resource label.
8. Provide an app container name. Please note that the application specified should have a readiness probe configured for this fault to be triggered successfully. 
9. Schedule options are not available for this fault.
10. Tags are key value pairs that will be send to the active monitoring tool under Mangle Admin settings ---&gt; Metric Providers at the time of publishing events for fault injection and remediation. They are not mandatory.
11. Click on Run Fault.
12. The user will be re-directed to the Processed Requests section under Requests & Reports tab.
13. If Mangle was able to successfully trigger the fault, the status of the task will change to "COMPLETED". Remediation requests are not supported for this fault.
14. For monitoring purposes, log into either Wavefront or Datadog once it is configured as an active Metric provider in Mangle and refer to the Events section. Events similar to the screenshots provided below will be available on the monitoring tool for tracking purposes.

     ![](../../.gitbook/assets/datadogevents.png) 



      ![](../../.gitbook/assets/wavefrontevents.png)

## Kubernetes Service Not Available

Kubernetes \(K8s\) Service Not Available faults enable you to abruptly make a service resource in K8s cluster not available, although the pod will be healthy and running. 

**Steps to follow:** 

1. Login as a user with read and write privileges to Mangle.
2. Navigate to Fault Execution tab ---&gt; Infrastructure Faults ---&gt; K8S ---&gt; Service Unavailable.
3. Select an Endpoint \(Only K8S endpoints are listed\).
4. Choose the appropriate service identifier: Service Name or Service Labels.
5. If you choose Service Name, enter an appropriate string.
6. If you choose Service Labels provide a key value pair for eg: app=mangle. Since multiple resources can have the same label, you also need to specify if you are interested in a Random Injection. If "Random Injection" is set to true, Mangle will randomly choose one resource in a list of resources identified using the label, for introducing the fault. If "Random Injection" is set to false, it will introduce fault into all resources identified using the resource label.
7. Schedule options are not available for this fault.
8. Tags are key value pairs that will be send to the active monitoring tool under Mangle Admin settings ---&gt; Metric Providers at the time of publishing events for fault injection and remediation. They are not mandatory.
9. Click on Run Fault.
10. The user will be re-directed to the Processed Requests section under Requests & Reports tab.
11. If Mangle was able to successfully trigger the fault, the status of the task will change to "COMPLETED". Remediation requests are not supported for this fault.
12. For monitoring purposes, log into either Wavefront or Datadog once it is configured as an active Metric provider in Mangle and refer to the Events section. Events similar to the screenshots provided below will be available on the monitoring tool for tracking purposes.

     ![](../../.gitbook/assets/datadogevents.png) 



      ![](../../.gitbook/assets/wavefrontevents.png)

## vCenter Disk Fault

vCenter Disk faults enable you to abruptly disconnect disks from a virtual machine in its inventory. It requires the VM Disk ID and VM Name to trigger the fault. For all vCenter faults, Mangle talks to the mangle-vc-adapter to connect and perform the required action on VC. So it is mandatory that you install the mangle-vc-adapter container prior to adding vCenter Endpoints or running vCenter faults. To find how to install and configure the mangle-vc-adapter, please refer [here](https://app.gitbook.com/@vmware-1/s/workspace/mangle-administration/supported-deployment-models#deploying-the-mangle-vcenter-adapter-container).

**Steps to follow:** 

1. Login as a user with read and write privileges to Mangle.
2. Navigate to Fault Execution tab ---&gt; Infrastructure Faults ---&gt; vCenter ---&gt; Disk.
3. Select an Endpoint \(Only vCenter endpoints are listed\).
4. Select the fault: Disconnect Disk.
5. Provide the VM Name and VM Disk ID. To identify the disk id, the VM moid is required. This information can be gathered from the vCenter MOB \(Managed Object Browser\). Refer to [Looking up Managed Object Reference for vCenter](https://kb.vmware.com/s/article/1017126) for help on this. Once you have retrieved the VM moid, the disk id can be retrieved from the disk properties section in the link below after replacing the values in angle braces &lt;&gt;:

   https://_**&lt;VC\_SERVER&gt;**_/mob/?moid=_**&lt;vm-moid&gt;**_&doPath=layout

6. Schedule options are not available for this fault.
7. Tags are key value pairs that will be send to the active monitoring tool under Mangle Admin settings ---&gt; Metric Providers at the time of publishing events for fault injection and remediation. They are not mandatory.
8. Click on Run Fault.
9. The user will be re-directed to the Processed Requests section under Requests & Reports tab.
10. If Mangle was able to successfully trigger the fault, the status of the task will change to "COMPLETED". The option to trigger a remediation request at anytime can be found on clicking the ![](../../.gitbook/assets/supportedactionsbutton.png) button against the task in the Processed Requests table.
11. For monitoring purposes, log into either Wavefront or Datadog once it is configured as an active Metric provider in Mangle and refer to the Events section. Events similar to the screenshots provided below will be available on the monitoring tool for tracking purposes.

     ![](../../.gitbook/assets/datadogevents.png) 



      ![](../../.gitbook/assets/wavefrontevents.png)

## vCenter NIC Fault

vCenter NIC faults enable you to abruptly disconnect network interface cards from a virtual machine in its inventory. It requires the VM Nic ID and VM Name to trigger the fault. For all vCenter faults, Mangle talks to the mangle-vc-adapter to connect and perform the required action on VC. So it is mandatory that you install the mangle-vc-adapter container prior to adding vCenter Endpoints or running vCenter faults. To find how to install and configure the mangle-vc-adapter, please refer [here](https://app.gitbook.com/@vmware-1/s/workspace/mangle-administration/supported-deployment-models#deploying-the-mangle-vcenter-adapter-container).

**Steps to follow:** 

1. Login as a user with read and write privileges to Mangle.
2. Navigate to Fault Execution tab ---&gt; Infrastructure Faults ---&gt; vCenter ---&gt; Nic.
3. Select an Endpoint \(Only vCenter endpoints are listed\).
4. Select the fault: Disconnect Nic.
5. Provide the VM Name and VM Nic ID. To identify the Nic id, the VM moid is required. This information can be gathered from the vCenter MOB \(Managed Object Browser\). Refer to [Looking up Managed Object Reference for vCenter](https://kb.vmware.com/s/article/1017126) for help on this. Once you have retrieved the VM moid, the disk id can be retrieved from the deviceConfigId section in the link below after replacing the values in angle braces &lt;&gt;:

   https://_**&lt;VC\_SERVER&gt;**_/mob/?moid=_**&lt;vm-moid&gt;**_&doPath=guest%2enet

6. Schedule options are not available for this fault.
7. Tags are key value pairs that will be send to the active monitoring tool under Mangle Admin settings ---&gt; Metric Providers at the time of publishing events for fault injection and remediation. They are not mandatory.
8. Click on Run Fault.
9. The user will be re-directed to the Processed Requests section under Requests & Reports tab.
10. If Mangle was able to successfully trigger the fault, the status of the task will change to "COMPLETED". The option to trigger a remediation request at anytime can be found on clicking the ![](../../.gitbook/assets/supportedactionsbutton.png) button against the task in the Processed Requests table.
11. For monitoring purposes, log into either Wavefront or Datadog once it is configured as an active Metric provider in Mangle and refer to the Events section. Events similar to the screenshots provided below will be available on the monitoring tool for tracking purposes.

     ![](../../.gitbook/assets/datadogevents.png) 



      ![](../../.gitbook/assets/wavefrontevents.png)

## vCenter VM State Change Fault

vCenter VM State Change faults enable you to abruptly power-off, reset or suspend any virtual machine in its inventory. It requires just the VM Name to trigger the fault. For all vCenter faults, Mangle talks to the mangle-vc-adapter to connect and perform the required action on VC. So it is mandatory that you install the mangle-vc-adapter container prior to adding vCenter Endpoints or running vCenter faults. To find how to install and configure the mangle-vc-adapter, please refer [here](https://app.gitbook.com/@vmware-1/s/workspace/mangle-administration/supported-deployment-models#deploying-the-mangle-vcenter-adapter-container).

**Steps to follow:** 

1. Login as a user with read and write privileges to Mangle.
2. Navigate to Fault Execution tab ---&gt; Infrastructure Faults ---&gt; vCenter ---&gt; State.
3. Select an Endpoint \(Only vCenter endpoints are listed\).
4. Select one of the faults: Poweroff, Suspend or Reset VM.
5. Provide the VM Name.
6. Schedule options are not available for this fault.
7. Tags are key value pairs that will be send to the active monitoring tool under Mangle Admin settings ---&gt; Metric Providers at the time of publishing events for fault injection and remediation. They are not mandatory.
8. Click on Run Fault.
9. The user will be re-directed to the Processed Requests section under Requests & Reports tab.
10. If Mangle was able to successfully trigger the fault, the status of the task will change to "COMPLETED". The option to trigger a remediation request at anytime can be found on clicking the ![](../../.gitbook/assets/supportedactionsbutton.png) button against the task in the Processed Requests table.
11. For monitoring purposes, log into either Wavefront or Datadog once it is configured as an active Metric provider in Mangle and refer to the Events section. Events similar to the screenshots provided below will be available on the monitoring tool for tracking purposes.

     ![](../../.gitbook/assets/datadogevents.png) 



      ![](../../.gitbook/assets/wavefrontevents.png)

## AWS EC2 State Change Fault

AWS EC2 State Change fault enables you to abruptly terminate, stop or reboot any EC2 instance. It requires AWS tags to uniquely identify instances against which the fault should run. 

**Steps to follow:** 

1. Login as a user with read and write privileges to Mangle.
2. Navigate to Fault Execution tab ---&gt; Infrastructure Faults ---&gt; AWS ---&gt; EC2 ---&gt; State.
3. Select an Endpoint \(Only AWS accounts are listed\).
4. Select one of the faults: Terminate\_Instances, Stop\_Instances, Reboot\_Instances.
5. Provide the AWS tag \(key value pair to uniquely identify the instance\(s\). Since multiple instances can have the same tag, you also need to specify if you are interested in a Random Injection. If "Random Injection" is set to true, Mangle will randomly choose one instance from a list of instances identified using the tag, for introducing the fault. If "Random Injection" is set to false, it will introduce the fault into all the instances identified using the tag.
6. Schedule options are not available for this fault.
7. Tags are key value pairs that will be send to the active monitoring tool under Mangle Admin settings ---&gt; Metric Providers at the time of publishing events for fault injection and remediation. They are not mandatory.
8. Click on Run Fault.
9. The user will be re-directed to the Processed Requests section under Requests & Reports tab.
10. If Mangle was able to successfully trigger the fault, the status of the task will change to "COMPLETED". The option to trigger a remediation request at anytime can be found on clicking the ![](../../.gitbook/assets/supportedactionsbutton.png) button against the task in the Processed Requests table.
11. For monitoring purposes, log into either Wavefront or Datadog once it is configured as an active Metric provider in Mangle and refer to the Events section. Events similar to the screenshots provided below will be available on the monitoring tool for tracking purposes.

     ![](../../.gitbook/assets/datadogevents.png) 



      ![](../../.gitbook/assets/wavefrontevents.png)

## AWS EC2 Network Fault

AWS EC2 Network fault enable you to abruptly terminate, stop or reboot any EC2 instance. It requires AWS tags to uniquely identify instances against which the fault should run. 

**Steps to follow:** 

1. Login as a user with read and write privileges to Mangle.
2. Navigate to Fault Execution tab ---&gt; Infrastructure Faults ---&gt; AWS ---&gt; EC2 ---&gt; Network.
3. Select an Endpoint \(Only AWS accounts are listed\).
4. Select the faults: Block\_All\_Network\_Traffic.
5. Provide the AWS tag \(key value pair to uniquely identify the instance\(s\). Since multiple instances can have the same tag, you also need to specify if you are interested in a Random Injection. If "Random Injection" is set to true, Mangle will randomly choose one instance from a list of instances identified using the tag, for introducing the fault. If "Random Injection" is set to false, it will introduce the fault into all the instances identified using the tag.
6. Schedule options are not available for this fault.
7. Tags are key value pairs that will be send to the active monitoring tool under Mangle Admin settings ---&gt; Metric Providers at the time of publishing events for fault injection and remediation. They are not mandatory.
8. Click on Run Fault.
9. The user will be re-directed to the Processed Requests section under Requests & Reports tab.
10. If Mangle was able to successfully trigger the fault, the status of the task will change to "COMPLETED". The option to trigger a remediation request at anytime can be found on clicking the ![](../../.gitbook/assets/supportedactionsbutton.png) button against the task in the Processed Requests table.
11. For monitoring purposes, log into either Wavefront or Datadog once it is configured as an active Metric provider in Mangle and refer to the Events section. Events similar to the screenshots provided below will be available on the monitoring tool for tracking purposes.

     ![](../../.gitbook/assets/datadogevents.png) 



      ![](../../.gitbook/assets/wavefrontevents.png)

## Relevant API Reference

{% hint style="info" %}
**For access to relevant API Swagger documentation:**

Please traverse to link ****![](../../.gitbook/assets/help.png) -----&gt; API Documentation from the Mangle UI or access _https://&lt;Mangle IP or Hostname&gt;/mangle-services/swagger-ui.html\#_/_fault-injection-controller_

  ![](../../.gitbook/assets/faultinjectioncontroller.png) 
{% endhint %}

