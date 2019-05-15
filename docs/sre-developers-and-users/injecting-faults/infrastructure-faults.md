# Infrastructure Faults

## CPU Fault

CPU fault enables spiking cpu usage values for a selected endpoint by a percentage specified by the user. With the help of a timeout field the duration for the fault run can be specified after which Mangle triggers the automatic remediation procedure.

**Steps to follow:** 

1. Login as an user with read and write privileges to Mangle.
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

1. Login as an user with read and write privileges to Mangle.
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

1. Login as an user with read and write privileges to Mangle.
2. Navigate to Fault Execution tab ---&gt; Infrastructure Faults ---&gt; Disk IO.
3. Select an Endpoint.
4. Provide a "IO Size" value in bytes. For eg: To write a total of 7.8 MB of data to the disk of the selected Endpoint specify the IO Size as 8192000.
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

1. Login as an user with read and write privileges to Mangle.
2. Navigate to Fault Execution tab ---&gt; Infrastructure Faults ---&gt; Kill Process.
3. Select an Endpoint.
4. Provide a "Process Identifier". This can either be a process id or process name. A process name is preferred if the fault is expected to be scheduled.
5. Provide a "Remediation Command". For eg: To start the sshd process that was killed on an Ubuntu 17 Server, specify the remediation command as _"sudo service ssh start" ._
6. Provide "Injection Home Dir" only if you would like Mangle to push the script files needed to simulate the fault to a specific location on the endpoint. Else the default temp location will be used.
7. Schedule options are required only if the fault needs to be re-executed at regular intervals against an endpoint.
8. Tags are key value pairs that will be send to the active monitoring tool under Mangle Admin settings ---&gt; Metric Providers at the time of publishing events for fault injection and remediation. They are not mandatory.
9. Click on Run Fault.
10. The user will be re-directed to the Processed Requests section under Requests & Reports tab.
11. If Mangle was able to successfully trigger the fault, the status of the task will change to "COMPLETED". The fault will continue to run at the endpoint until the timeout expires or a remediation request is triggered. The option to trigger a remediation request at anytime can be found on clicking the ![](../../.gitbook/assets/supportedactionsbutton.png) button against the task in the Processed Requests table.
12. For monitoring purposes, log into either Wavefront or Datadog once it is configured as an active Metric provider in Mangle and refer to the Events section. Events similar to the screenshots provided below will be available on the monitoring tool for tracking purposes.

     ![](../../.gitbook/assets/datadogevents.png) 



      ![](../../.gitbook/assets/wavefrontevents.png) 

## Docker State Change Faults

Docker State Change faults enable you to abruptly stop or pause containers running on a Docker host. Unlike other infrastructure faults like CPU, Memory and Disk IO this fault is specific to the Docker endpoint and does not have a timeout field because the fault completes very quickly. Some containers may be configured for auto-start and some might require a manual start command to be executed. For the first case, auto-remediation through Mangle is not needed. For the second case, a manual remediation can be triggered from the Requests and Reports tab after the fault completes.

**Steps to follow:** 

1. Login as an user with read and write privileges to Mangle.
2. Navigate to Fault Execution tab ---&gt; Infrastructure Faults ---&gt; Docker ---&gt; State Change.
3. Select an Endpoint \(Only Docker Endpoints are listed\).
4. Select the fault.
5. Provide a "Container Name".
6. Schedule options are not available for this fault.
7. Tags are key value pairs that will be send to the active monitoring tool under Mangle Admin settings ---&gt; Metric Providers at the time of publishing events for fault injection and remediation. They are not mandatory.
8. Click on Run Fault.
9. The user will be re-directed to the Processed Requests section under Requests & Reports tab.
10. If Mangle was able to successfully trigger the fault, the status of the task will change to "COMPLETED". The fault will continue to run at the endpoint until the timeout expires or a remediation request is triggered. The option to trigger a remediation request at anytime can be found on clicking the ![](../../.gitbook/assets/supportedactionsbutton.png) button against the task in the Processed Requests table.
11. For monitoring purposes, log into either Wavefront or Datadog once it is configured as an active Metric provider in Mangle and refer to the Events section. Events similar to the screenshots provided below will be available on the monitoring tool for tracking purposes.

     ![](../../.gitbook/assets/datadogevents.png) 



      ![](../../.gitbook/assets/wavefrontevents.png) 

