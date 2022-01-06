# Fault Injection Stage

There are some known issues and troubleshooting steps to follow when you run into issues while running faults.

## **Common Error Codes and Next Steps**

### FI0101, ErrorMessage : Infra agent files are missing at the endpoint! More details available in mangle log.

1. Usually the fault is run against an unsupported endpoint eg: Photon v1.0
2. There is unusually high latency while connecting to the endpoint while running a fault.
3. Ensure that the ssh service is running and the credentials are correct.

### **When faults such as** Spring service exception and latency, JAVA method exception and latency, kill JVM don't run as expected:

1. ssh into the target machine and execute these commands:\
   &#x20;      `sh /tmp/mangle-java-agent-3.5.0/bin/bmsubmit.sh -l`
2. If the provided classname/methodname is not valid, we still get the btm rule created. But it fails to compile and transform. To confirm this, run the command:\
   &#x20;  `sh /tmp/mangle-java-agent-3.5.0/bin/bmsubmit.sh -l`
3. If the rule description contains "NO COMPILE" and with errors, the provided joint points didn't execute. In this case, check the methodname/classname values again and retry the fault
4. If the rule description contains "NO RULES INSTALLED", then the rules were not installed. In this case, please  re-run the fault.

## **Known Issues**

#### Application memory fault injection does not run for applications using JDK version 9.0 and above

The Byteman agent connects to Java process. Out of memory exceptions are never thrown and the memory usage at target application remains as it is. There are no known workarounds for this and is currently a known limitation for Mangle.

#### CONNECTION\_LOSS AWS fault on RDS not supported for DB Cluster

The current implementation of AWS fault CONNECTION\_LOSS for RDS works only when the RDS database is an instance and not a cluster. Executing the fault on a cluster throws this error.

```
ErrorCode : FI0015, ErrorMessage : Execution of Command: CONNECTION_LOSS: --dbIdentifiers mangle3-5validation-instance-1 failed. errorCode: 1 output: The specified DB Instance is a member of a cluster. Modify database endpoint port number for the DB Cluster using the ModifyDbCluster API (Service: AmazonRDS; Status Code: 400; Error Code: InvalidParameterCombination; Request ID: 1d662ceb-dfa6-45bb-a634-e9a090104b21).
```

There are no known workarounds for this and is currently a known limitation for Mangle.

#### Spring cron expression to schedule fault injection job on an hourly basis doesn't run as expected if there is a missing wildcard character

Expressions such as the one below for running a fault at "second :00, at minute :18, every hour starting at 11am of every day"

```
0 18 11/1 * * ? 
```

should be replaced with "at second :00 of minute :35 of every hour" as below

```
0 35 * ? * *    --> 
```

#### Test connection for Database endpoints succeed but faults executed against the endpoint fail with a connection error.

When we add a database endpoint and click on test connection, then it will not test the DB credentials but only the parent endpoint (which could be a remote machine, K8s or Docker endpoint). So, errors in DB credentials are not detected as part of testing the connection. This can be further confirmed by looking at the logs. Typically the logs will capture errors such as below. In such cases, please validate the DB credentials and try the fault again.

```
2021-10-12 08:05:07.127 [SystemResourceFaultTaskHelper2-1634025149363] ERROR com.vmware.mangle.task.framework.helpers.CommandInfoExecutionHelper.executeRetriableCommand (85) - Command Execution Attempt: 2 Failed. Reason:Exception:Prerequisite failed:Provided db connection properties are not valid!2021-10-12 08:05:07.128 [SystemResourceFaultTaskHelper2-1634025149363] INFO  com.vmware.mangle.utils.CommonUtils.delayInSeconds (71) - Sleeping for 2 seconds
2021-10-12 08:05:09.128 [SystemResourceFaultTaskHelper2-1634025149363] INFO  com.vmware.mangle.utils.clients.ssh.SSHUtils.runCommandReturningResult (156) - Running Command ...
2021-10-12 08:05:10.261 [SystemResourceFaultTaskHelper2-1634025149363] ERROR com.vmware.mangle.task.framework.helpers.CommandInfoExecutionHelper.verifyExpectedFailures (137) - Execution of Command: cd /tmp//infra_agent;./infra_submit  --operation inject --faultname dbConnectionLeakFault_cassandra --dbName CASSANDRA --userName ****** --password ****** --port 9042 --sslEnabled false --timeout 60000 --faultId dbConnectionLeakFault_cassandra failed. errorCode: 1 output: Exception:Prerequisite failed:Provided db connection properties are not valid!
```

#### Mangle unable to inject the fault with an error **FAIL 1 sudo: sorry, you must have a tty to run sudo**

&#x20;Please ensure that /etc/sudoers file has been updated to have the following entry "`Defaults !requiretty`**".**
