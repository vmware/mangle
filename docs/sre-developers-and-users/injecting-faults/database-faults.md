# Database Faults

From **version 3.0**, Mangle supports the following types of database faults: 

1. Connection Leak \(For Postgres, Mongo and Cassandra\)
2. Transaction Error \(For Postgres\)
3. Transaction Latency \(For Postgres\)

## ![](../../.gitbook/assets/new_logo.png) Connection Leak

Connection Leak fault enables you to simulate scenarios where the client borrows connections from the database pool iteratively but doesn't return it thus causing the pool to run out of connections.

{% hint style="warning" %}
The database user specified in DB credentials should have Connect privileges for the target database.
{% endhint %}

**Steps to follow:** 

1. Login as a user with read and write privileges to Mangle.
2. Navigate to Fault Execution tab ---&gt; Database Faults ---&gt; Connection Leak.
3. Select a Database Endpoint.
4. Provide "Injection Home Dir" only if you would like Mangle to push the script files needed to simulate the fault to a specific location on the endpoint. Else the default temp location will be used.
5. Provide a "Timeout" value in milliseconds. For eg: if you need the Database Connection Leak to be sustained for a duration of 1 hour then you should provide the timeout value as 3600000 \(1 hour = 3600000 ms\). After this duration, Mangle will ensure remediation of the fault without any manual intervention.
6. Schedule options are required only if the fault needs to be re-executed at regular intervals against an endpoint.
7. Tags are key value pairs that will be send to the active monitoring tool under Mangle Admin settings ---&gt; Metric Providers at the time of publishing events for fault injection and remediation. They are not mandatory.
8. Supported notifiers include Slack channels that are configured under Mangle Admin settings ---&gt; Integrations ---&gt; Notifiers. This will enable Mangle to automatically publish status of fault injections to the appropriate Slack channels for monitoring purposes. They are optional and you can choose to exclude this while running faults.
9. Click on Run Fault.
10. The user will be re-directed to the Processed Requests section under Requests & Reports tab.
11. If Mangle was able to successfully trigger the fault, the status of the task will change to "INJECTED". The fault will continue to run at the endpoint until the timeout expires or a remediation request is triggered. The option to trigger a remediation request at anytime can be found on clicking the ![](../../.gitbook/assets/actions_button.png) button against the task in the Processed Requests table. The task will be updated to "COMPLETED" once the task is auto remediated or manually remediated before the fault timeout. 
12. For monitoring purposes, log into either Wavefront or Datadog once it is configured as an active Metric provider in Mangle and refer to the Events section. Events similar to the screenshots provided below will be available on the monitoring tool for tracking purposes.

## ![](../../.gitbook/assets/new_logo.png) Transaction Error

Transaction Error fault enables you to simulate scenarios where a specified percentage of database transactions on a table fails to execute or do not complete.

{% hint style="warning" %}
The database user specified in DB credentials should have privileges to create a trigger on DB tables for this fault to run. This fault is applicable only to INSERT, UPDATE, DELETE operations.
{% endhint %}

**Steps to follow:** 

1. Login as a user with read and write privileges to Mangle.
2. Navigate to Fault Execution tab ---&gt; Database Faults ---&gt; Transaction Error.
3. Select a Database Endpoint.
4. Provide a Database table name.
5. Provide a value to indicate what percentage of the database transactions would need to fail as part of running the fault.
6. Select an code to specify what kind of errors the transactions should fail with.
7. Provide "Injection Home Dir" only if you would like Mangle to push the script files needed to simulate the fault to a specific location on the endpoint. Else the default temp location will be used.
8. Provide a "Timeout" value in milliseconds. For eg: if you need the Database Transaction Error to be sustained for a duration of 1 hour then you should provide the timeout value as 3600000 \(1 hour = 3600000 ms\). After this duration, Mangle will ensure remediation of the fault without any manual intervention.
9. Schedule options are required only if the fault needs to be re-executed at regular intervals against an endpoint.
10. Tags are key value pairs that will be send to the active monitoring tool under Mangle Admin settings ---&gt; Metric Providers at the time of publishing events for fault injection and remediation. They are not mandatory.
11. Supported notifiers include Slack channels that are configured under Mangle Admin settings ---&gt; Integrations ---&gt; Notifiers. This will enable Mangle to automatically publish status of fault injections to the appropriate Slack channels for monitoring purposes. They are optional and you can choose to exclude this while running faults.
12. Click on Run Fault.
13. The user will be re-directed to the Processed Requests section under Requests & Reports tab.
14. If Mangle was able to successfully trigger the fault, the status of the task will change to "INJECTED". The fault will continue to run at the endpoint until the timeout expires or a remediation request is triggered. The option to trigger a remediation request at anytime can be found on clicking the ![](../../.gitbook/assets/actions_button.png) button against the task in the Processed Requests table. The task will be updated to "COMPLETED" once the task is auto remediated or manually remediated before the fault timeout. 
15. For monitoring purposes, log into either Wavefront or Datadog once it is configured as an active Metric provider in Mangle and refer to the Events section. Events similar to the screenshots provided below will be available on the monitoring tool for tracking purposes.

## Transaction Latency

Transaction Latency fault enables you to simulate scenarios where a specified percentage of database transactions on a table respond slow. How slow the transactions need to be can be specified in milliseconds.

{% hint style="warning" %}
The database user specified in DB credentials should have privileges to create a trigger on DB tables for this fault to run. This fault is applicable only to INSERT, UPDATE, DELETE operations.
{% endhint %}

**Steps to follow:** 

1. Login as a user with read and write privileges to Mangle.
2. Navigate to Fault Execution tab ---&gt; Database Faults ---&gt; Transaction Latency.
3. Select a Database Endpoint.
4. Provide a Database table name.
5. Provide a value to indicate what percentage of the database transactions would need to be slow as part of running the fault.
6. Provide a latency value in milliseconds to specify how slow the transactions need to be. The response from the database will be delayed by this value.
7. Provide "Injection Home Dir" only if you would like Mangle to push the script files needed to simulate the fault to a specific location on the endpoint. Else the default temp location will be used.
8. Provide a "Timeout" value in milliseconds. For eg: if you need the Database Transaction Error to be sustained for a duration of 1 hour then you should provide the timeout value as 3600000 \(1 hour = 3600000 ms\). After this duration, Mangle will ensure remediation of the fault without any manual intervention.
9. Schedule options are required only if the fault needs to be re-executed at regular intervals against an endpoint.
10. Tags are key value pairs that will be send to the active monitoring tool under Mangle Admin settings ---&gt; Metric Providers at the time of publishing events for fault injection and remediation. They are not mandatory.
11. Supported notifiers include Slack channels that are configured under Mangle Admin settings ---&gt; Integrations ---&gt; Notifiers. This will enable Mangle to automatically publish status of fault injections to the appropriate Slack channels for monitoring purposes. They are optional and you can choose to exclude this while running faults.
12. Click on Run Fault.
13. The user will be re-directed to the Processed Requests section under Requests & Reports tab.
14. If Mangle was able to successfully trigger the fault, the status of the task will change to "INJECTED". The fault will continue to run at the endpoint until the timeout expires or a remediation request is triggered. The option to trigger a remediation request at anytime can be found on clicking the ![](../../.gitbook/assets/actions_button.png) button against the task in the Processed Requests table. The task will be updated to "COMPLETED" once the task is auto remediated or manually remediated before the fault timeout. 
15. For monitoring purposes, log into either Wavefront or Datadog once it is configured as an active Metric provider in Mangle and refer to the Events section. Events similar to the screenshots provided below will be available on the monitoring tool for tracking purposes.

