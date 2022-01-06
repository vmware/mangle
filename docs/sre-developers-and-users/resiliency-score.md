# Resiliency Score

## Why do you need Resiliency Score?

Do you have a hard time answering any or all of the following questions?

1. How is my service reacting to a fault when it is injected?
2. Is it silently failing or does it create cascading failures?
3. How do I know if my system was in a stable state before the fault ran every time; especially when I have scheduled them at regular intervals?&#x20;
4. Is the resiliency for the service improving over time?
5. Are SLAs affected due to failures in a subset of services?

If your answer is yes, then you should give this feature a try!

Resiliency Score is a compelling new feature that enables you to quantify the resiliency or fault tolerance capacity of the application or system under test. The score is calculated by retrieving application metrics of your choice from the monitoring tool that you use, during fault injection. The score is then pushed back to the monitoring tool as a metric that can be monitored and tracked over a period of time.

Before you can use this feature, please ensure that the configuration is in place as described in section [Resiliency Score Metric Configuration](../mangle-administration/admin-settings.md#resiliency-score-metric-configuration) under the Mangle Admin Settings.

### Adding Queries

**Steps to follow:**&#x20;

1. Login as an user with read and write privileges to Mangle.
2. Navigate to Resiliency Score ---> Query.
3. Click on ![](<../.gitbook/assets/add\_button (1).png>).
4. Provide a Query Name, Weightage (A percentage value that indicates the impact of the query on the overall resiliency score for a service.) and a Query Condition (A query conditions must always evaluate to a Boolean value; 0 or 1. It is recommended that you use an existing query that is configured as Alerts in the monitoring system to avoid providing an erroneous query to Mangle.)
5. Click on **Submit**.
6. A success message is displayed and the table for Queries will be updated with the new entry.
7. Edit and Delete operations are supported for all saved queries.

### Adding Services

**Steps to follow:**&#x20;

1. Login as an user with read and write privileges to Mangle.
2. Navigate to Resiliency Score ---> Service.
3. Click on ![](<../.gitbook/assets/add\_button (1).png>) .&#x20;
4. Provide a Service Name (A name used to uniquely identify a service definition and the set of queries that needs to be evaluated to come up with a resiliency score for a particular service), select the appropriate queries (A query or a list of queries when evaluated proves to a holistic measure for a service's ability to withstand failures.) and Tags (A set of tags which will enable Mangle to shortlist fault events specific to a service from the monitoring system.)
5. Click on **Submit**.
6. A success message is displayed and the table for Services will be updated with the new entry.
7. Edit and Delete operations are supported for all saved queries.

### Calculating the Score

**Steps to follow:**&#x20;

1. Login as an user with read and write privileges to Mangle.
2. Navigate to Resiliency Score ---> Calculate Score.
3. Select a service for which Resiliency Score needs to be calculated.
4. Schedule options are required only if the score needs to calculated and pushed to the monitoring system at regular intervals.
5. Click on **Submit**.
6. The user will be re-directed to the Processed Requests section under Requests & Reports tab.
7. A task of type RESILIENCY\_SCORE will be created and the status will change to "Completed" as soon the score is generated and send to the monitoring system.

{% hint style="info" %}
**PLEASE NOTE:** _This feature is still under evaluation and is supported only **VMware Wavefront**. If you need Mangle to provide support for other monitoring systems, please raise a feature request under_ [_Mangle Github_](https://github.com/vmware/mangle/issues)_._
{% endhint %}

{% hint style="info" %}
**Relevant API List**

**For access to Swagger documentation, please traverse to link** ![](../.gitbook/assets/help.png) -----> API Documentation from the Mangle UI or access _https://\<Mangle IP or Hostname>/mangle-services/swagger-ui.html#/resiliency-score-controller_
{% endhint %}
