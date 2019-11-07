# Application Faults

For **version 1.0**, Mangle supported the following types of application faults: 

1. CPU Fault
2. Memory Fault

From **version 2.0**,  apart from the faults listed above, support has been extended to the following new faults:

1. File Handler Leak Fault
2. Thread Leak Fault
3. Java Method Latency Fault
4. Spring Service Latency Fault
5. Spring Service Exception Fault
6. Simulate Java Exception
7. Kill JVM Fault

## CPU Fault

CPU fault enables spiking cpu usage values for a selected application within a specified endpoint by a percentage specified by the user. Mangle uses a modified Byteman agent to simulate this fault and supports only Java based applications at present. With the help of a timeout field the duration for the fault run can be specified after which Mangle triggers the automatic remediation procedure which includes a cleanup of the Byteman agent from the target endpoint.

This fault therefore takes additional arguments to identify the application under test.

**Steps to follow:** 

1. Login as a user with read and write privileges to Mangle.
2. Navigate to Fault Execution tab ---&gt; Application Faults ---&gt; CPU.
3. Select an Endpoint.

   **If the Endpoint is of type Kubernetes:**

   Provide additional K8s arguments such as Container Name, Pod Labels and the Random Injection flag.

   **If the Endpoint is of type Docker:**

   Provide additional Docker argument such as Container Name.

4. Provide a "CPU Load" value. For eg: 80 to simulate a CPU usage of 80% on the selected application.
5. Provide "Injection Home Dir" only if you would like Mangle to push the script files needed to simulate the fault to a specific location on the endpoint. Else the default temp location will be used.
6. Provide a "Timeout" value in milliseconds. For eg: if you need the CPU load of 80% to be sustained for a duration of 1 hour then you should provide the timeout value as 3600000 \(1 hour = 3600000 ms\). After this duration, Mangle will ensure remediation of the fault without any manual intervention.
7. Provide additional JVM properties such as Java Home, JVM Process, Free Port and Logon User. For eg: If the application under test is a VMware application then the JRE for the application resides in a specific location so for Java Home enter string /usr/java/jre-vmware/bin/java. The JVM Process can be either the process id of the application or the JVM descriptor name. In cases where you schedule, application faults, it is preferable to specify the JVM descriptor name. The Free Port is for the Byteman agent to talk to the application, so provide one that is not in use. The Logon User should be a user who has permissions to access and run the application under test. If it is root specify that else specify the appropriate user id. 
8. Schedule options are required only if the fault needs to be re-executed at regular intervals against an endpoint.
9. Tags are key value pairs that will be send to the active monitoring tool under Mangle Admin settings ---&gt; Metric Providers at the time of publishing events for fault injection and remediation. They are not mandatory.
10. Click on Run Fault.
11. The user will be re-directed to the Processed Requests section under Requests & Reports tab.
12. If Mangle was able to successfully trigger the fault, the status of the task will change to "COMPLETED". The fault will continue to run at the endpoint until the timeout expires or a remediation request is triggered. The option to trigger a remediation request at anytime can be found on clicking the ![](../../.gitbook/assets/supportedactionsbutton.png) button against the task in the Processed Requests table.
13. For monitoring purposes, log into either Wavefront or Datadog once it is configured as an active Metric provider in Mangle and refer to the Events section. Events similar to the screenshots provided below will be available on the monitoring tool for tracking purposes.

     ![](../../.gitbook/assets/datadogevents.png) 



      ![](../../.gitbook/assets/wavefrontevents.png) 

## Memory Fault

Memory fault enables spiking memory usage values for a selected endpoint by a percentage specified by the user. With the help of a timeout field the duration for the fault run can be specified after which Mangle triggers the automatic remediation procedure.

**Steps to follow:** 

1. Login as a user with read and write privileges to Mangle.
2. Navigate to Fault Execution tab ---&gt; Application Faults ---&gt; Memory.
3. Select an Endpoint.

   **If the Endpoint is of type Kubernetes:**

   Provide additional K8s arguments such as Container Name, Pod Labels and the Random Injection flag.

   **If the Endpoint is of type Docker:**

   Provide additional Docker argument such as Container Name.

4. Provide a "Memory Load" value. For eg: 80 to simulate a Memory usage of 80% on the selected Endpoint.
5. Provide "Injection Home Dir" only if you would like Mangle to push the script files needed to simulate the fault to a specific location on the endpoint. Else the default temp location will be used.
6. Provide a "Timeout" value in milliseconds. For eg: if you need the Memory load of 80% to be sustained for a duration of 1 hour then you should provide the timeout value as 3600000 \(1 hour = 3600000 ms\). After this duration, Mangle will ensure remediation of the fault without any manual intervention.
7. Provide additional JVM properties such as Java Home, JVM Process, Free Port and Logon User. For eg: If the application under test is a VMware application then the JRE for the application resides in a specific location so for Java Home enter string /usr/java/jre-vmware/bin/java. The JVM Process can be either the process id of the application or the JVM descriptor name. In cases where you schedule, application faults, it is preferable to specify the JVM descriptor name. The Free Port is for the Byteman agent to talk to the application, so provide one that is not in use. The Logon User should be a user who has permissions to access and run the application under test. If it is root specify that else specify the appropriate user id. 
8. Schedule options are required only if the fault needs to be re-executed at regular intervals against an endpoint.
9. Tags are key value pairs that will be send to the active monitoring tool under Mangle Admin settings ---&gt; Metric Providers at the time of publishing events for fault injection and remediation. They are not mandatory.
10. Click on Run Fault.
11. The user will be re-directed to the Processed Requests section under Requests & Reports tab.
12. If Mangle was able to successfully trigger the fault, the status of the task will change to "COMPLETED". The fault will continue to run at the endpoint until the timeout expires or a remediation request is triggered. The option to trigger a remediation request at anytime can be found on clicking the ![](../../.gitbook/assets/supportedactionsbutton.png) button against the task in the Processed Requests table.
13. For monitoring purposes, log into either Wavefront or Datadog once it is configured as an active Metric provider in Mangle and refer to the Events section. Events similar to the screenshots provided below will be available on the monitoring tool for tracking purposes.

     ![](../../.gitbook/assets/datadogevents.png) 



      ![](../../.gitbook/assets/wavefrontevents.png) 

## File Handler Leak Fault

File Handler Leak fault enables you to simulate conditions where a program requests for a handle to a resource but does not release it when the resource is no longer in use. This condition if left over extended periods of time, will lead to "Too many open file handles" errors and will cause performance degradation or crashes. With the help of a timeout field the duration for the fault run can be specified after which Mangle triggers the automatic remediation procedure.

**Steps to follow:** 

1. Login as a user with read and write privileges to Mangle.
2. Navigate to Fault Execution tab ---&gt; Application Faults ---&gt; Memory.
3. Select an Endpoint.

   **If the Endpoint is of type Kubernetes:**

   Provide additional K8s arguments such as Container Name, Pod Labels and the Random Injection flag.

   **If the Endpoint is of type Docker:**

   Provide additional Docker argument such as Container Name.

4. Provide "Injection Home Dir" only if you would like Mangle to push the script files needed to simulate the fault to a specific location on the endpoint. Else the default temp location will be used.
5. Provide a "Timeout" value in milliseconds. For eg: if you need the File Handler leak to be sustained for a duration of 1 hour then you should provide the timeout value as 3600000 \(1 hour = 3600000 ms\). After this duration, Mangle will ensure remediation of the fault without any manual intervention.
6. Provide additional JVM properties such as Java Home, JVM Process, Free Port and Logon User. For eg: If the application under test is a VMware application then the JRE for the application resides in a specific location so for Java Home enter string /usr/java/jre-vmware/bin/java. The JVM Process can be either the process id of the application or the JVM descriptor name. In cases where you schedule, application faults, it is preferable to specify the JVM descriptor name. The Free Port is for the Byteman agent to talk to the application, so provide one that is not in use. The Logon User should be a user who has permissions to access and run the application under test. If it is root specify that else specify the appropriate user id. 
7. Schedule options are required only if the fault needs to be re-executed at regular intervals against an endpoint.
8. Tags are key value pairs that will be send to the active monitoring tool under Mangle Admin settings ---&gt; Metric Providers at the time of publishing events for fault injection and remediation. They are not mandatory.
9. Click on Run Fault.
10. The user will be re-directed to the Processed Requests section under Requests & Reports tab.
11. If Mangle was able to successfully trigger the fault, the status of the task will change to "COMPLETED". The fault will continue to run at the endpoint until the timeout expires or a remediation request is triggered. The option to trigger a remediation request at anytime can be found on clicking the ![](../../.gitbook/assets/supportedactionsbutton.png) button against the task in the Processed Requests table.
12. For monitoring purposes, log into either Wavefront or Datadog once it is configured as an active Metric provider in Mangle and refer to the Events section. Events similar to the screenshots provided below will be available on the monitoring tool for tracking purposes.

     ![](../../.gitbook/assets/datadogevents.png) 



      ![](../../.gitbook/assets/wavefrontevents.png) 

## Thread Leak Fault

Thread Leak fault enables you to simulate conditions where an open thread is not closed. This condition if left over extended periods of time, leads to too many open threads thus creating thread leaks and out of memory issues. Usually a thread dump is required to troubleshoot such issues. With the help of a timeout field the duration for the fault run can be specified after which Mangle triggers the automatic remediation procedure.

**Steps to follow:** 

1. Login as a user with read and write privileges to Mangle.
2. Navigate to Fault Execution tab ---&gt; Application Faults ---&gt; Memory.
3. Select an Endpoint.

   **If the Endpoint is of type Kubernetes:**

   Provide additional K8s arguments such as Container Name, Pod Labels and the Random Injection flag.

   **If the Endpoint is of type Docker:**

   Provide additional Docker argument such as Container Name.

4. Set of Out of Memory required flag to true if you want the thread leak to eventually result in OOM errors.
5. Provide "Injection Home Dir" only if you would like Mangle to push the script files needed to simulate the fault to a specific location on the endpoint. Else the default temp location will be used.
6. Provide a "Timeout" value in milliseconds. For eg: if you need the Thread leak to be sustained for a duration of 1 hour then you should provide the timeout value as 3600000 \(1 hour = 3600000 ms\). After this duration, Mangle will ensure remediation of the fault without any manual intervention.
7. Provide additional JVM properties such as Java Home, JVM Process, Free Port and Logon User. For eg: If the application under test is a VMware application then the JRE for the application resides in a specific location so for Java Home enter string /usr/java/jre-vmware/bin/java. The JVM Process can be either the process id of the application or the JVM descriptor name. In cases where you schedule, application faults, it is preferable to specify the JVM descriptor name. The Free Port is for the Byteman agent to talk to the application, so provide one that is not in use. The Logon User should be a user who has permissions to access and run the application under test. If it is root specify that else specify the appropriate user id. 
8. Schedule options are required only if the fault needs to be re-executed at regular intervals against an endpoint.
9. Tags are key value pairs that will be send to the active monitoring tool under Mangle Admin settings ---&gt; Metric Providers at the time of publishing events for fault injection and remediation. They are not mandatory.
10. Click on Run Fault.
11. The user will be re-directed to the Processed Requests section under Requests & Reports tab.
12. If Mangle was able to successfully trigger the fault, the status of the task will change to "COMPLETED". The fault will continue to run at the endpoint until the timeout expires or a remediation request is triggered. The option to trigger a remediation request at anytime can be found on clicking the ![](../../.gitbook/assets/supportedactionsbutton.png) button against the task in the Processed Requests table.
13. For monitoring purposes, log into either Wavefront or Datadog once it is configured as an active Metric provider in Mangle and refer to the Events section. Events similar to the screenshots provided below will be available on the monitoring tool for tracking purposes.

     ![](../../.gitbook/assets/datadogevents.png) 



      ![](../../.gitbook/assets/wavefrontevents.png) 

## Java Method Latency Fault

Java Method Latency Fault helps you simulate a condition where calls to a specific Java method can be delayed by a specific time. Please note that you would have to be familiar with the application code; Java classes and methods in order to simulate this fault. 

**Steps to follow:** 

1. Login as a user with read and write privileges to Mangle.
2. Navigate to Fault Execution tab ---&gt; Application Faults ---&gt; Memory.
3. Select an Endpoint.

   **If the Endpoint is of type Kubernetes:**

   Provide additional K8s arguments such as Container Name, Pod Labels and the Random Injection flag.

   **If the Endpoint is of type Docker:**

   Provide additional Docker argument such as Container Name.

4. Provide "Injection Home Dir" only if you would like Mangle to push the script files needed to simulate the fault to a specific location on the endpoint. Else the default temp location will be used.
5. Provide "Latency" value in milliseconds so that Mangle can delay calls to the method by that time.
6. Provide "Class Name" as PluginController if the class of interest is defined as `public class PluginController {...}`.
7. Provide "Method Name" as getPlugins if the method to be tested is defined as follows:

   `public ResponseEntity> getPlugins(` 

   `@RequestParam(value = "pluginId", required = false) String pluginId, @RequestParam(value = "extensionType", required = false) ExtensionType extensionType) {` 

   `log.info("PluginController getPlugins() Start.............");` 

   `if (StringUtils.hasLength(pluginId)) {` 

   `return new ResponseEntity<>(pluginService.getExtensions(pluginId, extensionType), HttpStatus.OK);` 

   `}` 

   `return new ResponseEntity<>(pluginService.getExtensions(), HttpStatus.OK);`

   `}`

8. Provide "Rule Event" as "AT ENTRY" OR "AT EXIT" to specify if the fault has to be introduced in the beginning or at the end of the method call.
9. Provide additional JVM properties such as Java Home, JVM Process, Free Port and Logon User. For eg: If the application under test is a VMware application then the JRE for the application resides in a specific location so for Java Home enter string /usr/java/jre-vmware/bin/java. The JVM Process can be either the process id of the application or the JVM descriptor name. In cases where you schedule, application faults, it is preferable to specify the JVM descriptor name. The Free Port is for the Byteman agent to talk to the application, so provide one that is not in use. The Logon User should be a user who has permissions to access and run the application under test. If it is root specify that else specify the appropriate user id. 
10. Schedule options are required only if the fault needs to be re-executed at regular intervals against an endpoint.
11. Tags are key value pairs that will be send to the active monitoring tool under Mangle Admin settings ---&gt; Metric Providers at the time of publishing events for fault injection and remediation. They are not mandatory.
12. Click on Run Fault.
13. The user will be re-directed to the Processed Requests section under Requests & Reports tab.
14. If Mangle was able to successfully trigger the fault, the status of the task will change to "COMPLETED". The fault will continue to run at the endpoint until the timeout expires or a remediation request is triggered. The option to trigger a remediation request at anytime can be found on clicking the ![](../../.gitbook/assets/supportedactionsbutton.png) button against the task in the Processed Requests table.
15. For monitoring purposes, log into either Wavefront or Datadog once it is configured as an active Metric provider in Mangle and refer to the Events section. Events similar to the screenshots provided below will be available on the monitoring tool for tracking purposes.

     ![](../../.gitbook/assets/datadogevents.png) 



      ![](../../.gitbook/assets/wavefrontevents.png) 

## Spring Service Latency Fault

Spring Service Latency Fault helps you simulate a condition where calls to a specific API can be delayed by a specific time. Please note that you would have to be familiar with the REST application URLs and calls in order to simulate this fault. 

**Steps to follow:** 

1. Login as a user with read and write privileges to Mangle.
2. Navigate to Fault Execution tab ---&gt; Application Faults ---&gt; Memory.
3. Select an Endpoint.

   **If the Endpoint is of type Kubernetes:**

   Provide additional K8s arguments such as Container Name, Pod Labels and the Random Injection flag.

   **If the Endpoint is of type Docker:**

   Provide additional Docker argument such as Container Name.

4. Provide "Injection Home Dir" only if you would like Mangle to push the script files needed to simulate the fault to a specific location on the endpoint. Else the default temp location will be used.
5. Provide "Latency" value in milliseconds so that Mangle can delay calls to the method by that time.
6. Provide "Service URI" as /rest/api/v1/plugin if the REST URL of interest is as follows `https://xxx.vmware.com/mangle-services/rest/api/v1/plugins`.
7. Provide "Http Method" as GET, POST, PUT, PATCH or DELETE as applicable.
8. Provide additional JVM properties such as Java Home, JVM Process, Free Port and Logon User. For eg: If the application under test is a VMware application then the JRE for the application resides in a specific location so for Java Home enter string /usr/java/jre-vmware/bin/java. The JVM Process can be either the process id of the application or the JVM descriptor name. In cases where you schedule, application faults, it is preferable to specify the JVM descriptor name. The Free Port is for the Byteman agent to talk to the application, so provide one that is not in use. The Logon User should be a user who has permissions to access and run the application under test. If it is root specify that else specify the appropriate user id. 
9. Schedule options are required only if the fault needs to be re-executed at regular intervals against an endpoint.
10. Tags are key value pairs that will be send to the active monitoring tool under Mangle Admin settings ---&gt; Metric Providers at the time of publishing events for fault injection and remediation. They are not mandatory.
11. Click on Run Fault.
12. The user will be re-directed to the Processed Requests section under Requests & Reports tab.
13. If Mangle was able to successfully trigger the fault, the status of the task will change to "COMPLETED". The fault will continue to run at the endpoint until the timeout expires or a remediation request is triggered. The option to trigger a remediation request at anytime can be found on clicking the ![](../../.gitbook/assets/supportedactionsbutton.png) button against the task in the Processed Requests table.
14. For monitoring purposes, log into either Wavefront or Datadog once it is configured as an active Metric provider in Mangle and refer to the Events section. Events similar to the screenshots provided below will be available on the monitoring tool for tracking purposes.

     ![](../../.gitbook/assets/datadogevents.png) 



      ![](../../.gitbook/assets/wavefrontevents.png) 

## Spring Service Exception Fault

Spring Service Exception Fault helps you simulate a condition where calls to a specific API can be simulated to throw an exception. Please note that you would have to be familiar with the REST application URLs and calls; application code, classes, methods and exceptions in order to simulate this fault. 

**Steps to follow:** 

1. Login as a user with read and write privileges to Mangle.
2. Navigate to Fault Execution tab ---&gt; Application Faults ---&gt; Memory.
3. Select an Endpoint.

   **If the Endpoint is of type Kubernetes:**

   Provide additional K8s arguments such as Container Name, Pod Labels and the Random Injection flag.

   **If the Endpoint is of type Docker:**

   Provide additional Docker argument such as Container Name.

4. Provide "Injection Home Dir" only if you would like Mangle to push the script files needed to simulate the fault to a specific location on the endpoint. Else the default temp location will be used.
5. Provide "Service URI" as /rest/api/v1/plugin if the REST URL of interest is as follows `https://xxx.vmware.com/mangle-services/rest/api/v1/plugins`.
6. Provide "Http Method" as GET, POST, PUT, PATCH or DELETE as applicable.
7. Provide "Exception Class" as for eg: java.lang.NullPointerException if you want a null pointer exception to be thrown.
8. Provide "Exception Message" as any sample message for testing purposes.
9. Provide additional JVM properties such as Java Home, JVM Process, Free Port and Logon User. For eg: If the application under test is a VMware application then the JRE for the application resides in a specific location so for Java Home enter string /usr/java/jre-vmware/bin/java. The JVM Process can be either the process id of the application or the JVM descriptor name. In cases where you schedule, application faults, it is preferable to specify the JVM descriptor name. The Free Port is for the Byteman agent to talk to the application, so provide one that is not in use. The Logon User should be a user who has permissions to access and run the application under test. If it is root specify that else specify the appropriate user id. 
10. Schedule options are required only if the fault needs to be re-executed at regular intervals against an endpoint.
11. Tags are key value pairs that will be send to the active monitoring tool under Mangle Admin settings ---&gt; Metric Providers at the time of publishing events for fault injection and remediation. They are not mandatory.
12. Click on Run Fault.
13. The user will be re-directed to the Processed Requests section under Requests & Reports tab.
14. If Mangle was able to successfully trigger the fault, the status of the task will change to "COMPLETED". The fault will continue to run at the endpoint until the timeout expires or a remediation request is triggered. The option to trigger a remediation request at anytime can be found on clicking the ![](../../.gitbook/assets/supportedactionsbutton.png) button against the task in the Processed Requests table.
15. For monitoring purposes, log into either Wavefront or Datadog once it is configured as an active Metric provider in Mangle and refer to the Events section. Events similar to the screenshots provided below will be available on the monitoring tool for tracking purposes.

     ![](../../.gitbook/assets/datadogevents.png) 



      ![](../../.gitbook/assets/wavefrontevents.png) 

## Simulate Java Exception

Java Method Exception Fault helps you simulate a condition where calls to a specific Java method can result in exceptions. Please note that you would have to be familiar with the application code; Java classes, methods and exceptions in order to simulate this fault. 

**Steps to follow:** 

1. Login as a user with read and write privileges to Mangle.
2. Navigate to Fault Execution tab ---&gt; Application Faults ---&gt; Memory.
3. Select an Endpoint.

   **If the Endpoint is of type Kubernetes:**

   Provide additional K8s arguments such as Container Name, Pod Labels and the Random Injection flag.

   **If the Endpoint is of type Docker:**

   Provide additional Docker argument such as Container Name.

4. Provide "Injection Home Dir" only if you would like Mangle to push the script files needed to simulate the fault to a specific location on the endpoint. Else the default temp location will be used.
5. Provide "Latency" value in milliseconds so that Mangle can delay calls to the method by that time.
6. Provide "Class Name" as PluginController if the class of interest is defined as `public class PluginController {...}`.
7. Provide "Method Name" as getPlugins if the method to be tested is defined as follows:

   `public ResponseEntity> getPlugins(` 

   `@RequestParam(value = "pluginId", required = false) String pluginId, @RequestParam(value = "extensionType", required = false) ExtensionType extensionType) {` 

   `log.info("PluginController getPlugins() Start.............");` 

   `if (StringUtils.hasLength(pluginId)) {` 

   `return new ResponseEntity<>(pluginService.getExtensions(pluginId, extensionType), HttpStatus.OK);` 

   `}` 

   `return new ResponseEntity<>(pluginService.getExtensions(), HttpStatus.OK);`

   `}`

8. Provide "Rule Event" as "AT ENTRY" OR "AT EXIT" to specify if the fault has to be introduced in the beginning or at the end of the method call.
9. Provide "Exception Class" as for eg: java.lang.NullPointerException if you want a null pointer exception to be thrown.
10. Provide "Exception Message" as any sample message for testing purposes.
11. Provide additional JVM properties such as Java Home, JVM Process, Free Port and Logon User. For eg: If the application under test is a VMware application then the JRE for the application resides in a specific location so for Java Home enter string /usr/java/jre-vmware/bin/java. The JVM Process can be either the process id of the application or the JVM descriptor name. In cases where you schedule, application faults, it is preferable to specify the JVM descriptor name. The Free Port is for the Byteman agent to talk to the application, so provide one that is not in use. The Logon User should be a user who has permissions to access and run the application under test. If it is root specify that else specify the appropriate user id. 
12. Schedule options are required only if the fault needs to be re-executed at regular intervals against an endpoint.
13. Tags are key value pairs that will be send to the active monitoring tool under Mangle Admin settings ---&gt; Metric Providers at the time of publishing events for fault injection and remediation. They are not mandatory.
14. Click on Run Fault.
15. The user will be re-directed to the Processed Requests section under Requests & Reports tab.
16. If Mangle was able to successfully trigger the fault, the status of the task will change to "COMPLETED". The fault will continue to run at the endpoint until the timeout expires or a remediation request is triggered. The option to trigger a remediation request at anytime can be found on clicking the ![](../../.gitbook/assets/supportedactionsbutton.png) button against the task in the Processed Requests table.
17. For monitoring purposes, log into either Wavefront or Datadog once it is configured as an active Metric provider in Mangle and refer to the Events section. Events similar to the screenshots provided below will be available on the monitoring tool for tracking purposes.

     ![](../../.gitbook/assets/datadogevents.png) 



      ![](../../.gitbook/assets/wavefrontevents.png) 

## Kill JVM Fault

Kill JVM Fault helps you simulate a condition where JVM crashes with specific exit codes when calls to a specific Java method are done. Please note that you would have to be familiar with the application code; Java classes, methods and exceptions in order to simulate this fault. 

**Steps to follow:** 

1. Login as a user with read and write privileges to Mangle.
2. Navigate to Fault Execution tab ---&gt; Application Faults ---&gt; Memory.
3. Select an Endpoint.

   **If the Endpoint is of type Kubernetes:**

   Provide additional K8s arguments such as Container Name, Pod Labels and the Random Injection flag.

   **If the Endpoint is of type Docker:**

   Provide additional Docker argument such as Container Name.

4. Provide "Injection Home Dir" only if you would like Mangle to push the script files needed to simulate the fault to a specific location on the endpoint. Else the default temp location will be used.
5. Provide "Latency" value in milliseconds so that Mangle can delay calls to the method by that time.
6. Provide "Class Name" as PluginController if the class of interest is defined as `public class PluginController {...}`.
7. Provide "Method Name" as getPlugins if the method to be tested is defined as follows:

   `public ResponseEntity> getPlugins(` 

   `@RequestParam(value = "pluginId", required = false) String pluginId, @RequestParam(value = "extensionType", required = false) ExtensionType extensionType) {` 

   `log.info("PluginController getPlugins() Start.............");` 

   `if (StringUtils.hasLength(pluginId)) {` 

   `return new ResponseEntity<>(pluginService.getExtensions(pluginId, extensionType), HttpStatus.OK);` 

   `}` 

   `return new ResponseEntity<>(pluginService.getExtensions(), HttpStatus.OK);`

   `}`

8. Provide "Rule Event" as "AT ENTRY" OR "AT EXIT" to specify if the fault has to be introduced in the beginning or at the end of the method call.
9. Select an appropriate "Exit Code" from the drop down menu.
10. Provide additional JVM properties such as Java Home, JVM Process, Free Port and Logon User. For eg: If the application under test is a VMware application then the JRE for the application resides in a specific location so for Java Home enter string /usr/java/jre-vmware/bin/java. The JVM Process can be either the process id of the application or the JVM descriptor name. In cases where you schedule, application faults, it is preferable to specify the JVM descriptor name. The Free Port is for the Byteman agent to talk to the application, so provide one that is not in use. The Logon User should be a user who has permissions to access and run the application under test. If it is root specify that else specify the appropriate user id. 
11. Schedule options are required only if the fault needs to be re-executed at regular intervals against an endpoint.
12. Tags are key value pairs that will be send to the active monitoring tool under Mangle Admin settings ---&gt; Metric Providers at the time of publishing events for fault injection and remediation. They are not mandatory.
13. Click on Run Fault.
14. The user will be re-directed to the Processed Requests section under Requests & Reports tab.
15. If Mangle was able to successfully trigger the fault, the status of the task will change to "COMPLETED". The fault will continue to run at the endpoint until the timeout expires or a remediation request is triggered. The option to trigger a remediation request at anytime can be found on clicking the ![](../../.gitbook/assets/supportedactionsbutton.png) button against the task in the Processed Requests table.
16. For monitoring purposes, log into either Wavefront or Datadog once it is configured as an active Metric provider in Mangle and refer to the Events section. Events similar to the screenshots provided below will be available on the monitoring tool for tracking purposes.

     ![](../../.gitbook/assets/datadogevents.png) 



      ![](../../.gitbook/assets/wavefrontevents.png) 

## Relevant API Reference

{% hint style="info" %}
**For access to relevant API Swagger documentation:**

Please traverse to link ****![](../../.gitbook/assets/help.png) -----&gt; API Documentation from the Mangle UI or access _https://&lt;Mangle IP or Hostname&gt;/mangle-services/swagger-ui.html\#_/_fault-injection-controller_

  ![](../../.gitbook/assets/faultinjectioncontroller.png) 
{% endhint %}

