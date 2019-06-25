# Admin Settings

## Managing Authentication

### Adding additional Authentication sources

Mangle supports using Active Directory as an additional authentication source.

**Steps to follow:**

1. Login as an admin user to Mangle.
2. Navigate to ![](../.gitbook/assets/settings%20%281%29.png) -----&gt; Auth Management  -----&gt; Auth Source .
3. Click on ![](../.gitbook/assets/authsourcebutton.png).
4. Enter URL, Domain and click on **Submit**.
5. A success message is displayed and the table for Auth sources will be updated with the new entry.
6. Click on ![](../.gitbook/assets/supportedactionsbutton.png) against a table entry to see the supported operations.

{% hint style="info" %}
**Relevant API List**

**For access to Swagger documentation, please traverse to link** ![](../.gitbook/assets/help.png) -----&gt; API Documentation from the Mangle UI or access [https://&lt;Mangle](https://<Mangle) _IP or Hostname&gt;/mangle-services/swagger-ui.html\#/auth-provider-controller_

![](../.gitbook/assets/auth-provider-controller.png)
{% endhint %}

### Adding/Importing Users

Mangle supports adding new local user or importing users from Active Directory sources added as additional authentication sources.

**Steps to follow:**

1. Login as an admin user to Mangle.
2. Navigate to ![](../.gitbook/assets/settings%20%282%29.png) -----&gt; Auth Management  -----&gt; Users .
3. Click on ![](../.gitbook/assets/adduserbutton.png).
4. Enter User Name, Auth Source, Password if the Auth Source selected is "mangle.local", an appropriate role and click on **Submit**.
5. A success message is displayed and the table for Users will be updated with the new entry.
6. Click on ![](../.gitbook/assets/supportedactionsbutton.png) against a table entry to see the supported operations.

{% hint style="info" %}
**Relevant API List**

**For access to Swagger documentation, please traverse to link** ![](../.gitbook/assets/help.png) -----&gt; API Documentation from the Mangle UI or access [https://&lt;Mangle](https://<Mangle) _IP or Hostname&gt;/mangle-services/swagger-ui.html\#/user-management-controller_

![](../.gitbook/assets/usermanagementcontroller.png)
{% endhint %}

### Default and Custom Roles

Mangle has the following default Roles and Privileges.

| Default Role | Default Privileges | Allowed Operations |
| :--- | :--- | :--- |
| ROLE\_READONLY | READONLY |  |
| ROLE\_ADMIN | ADMIN\_READ\_WRITE, USER\_READ\_WRITE |  |
| ROLE\_USER | ADMIN\_READ, USER\_READ\_WRITE |  |

{% hint style="warning" %}
Edit and Delete operations are supported only for custom roles. It is forbidden for default roles.
{% endhint %}

Mangle supports creation of custom roles from the default privileges that are available.

**Steps to follow:**

1. Login as an admin user to Mangle.
2. Navigate to ![](../.gitbook/assets/settings%20%283%29.png) -----&gt; Auth Management  -----&gt; Roles.
3. Click on ![](../.gitbook/assets/customrolebutton.png).
4. Enter Role Name, Privileges and click on **Submit**.
5. A success message is displayed and the table for Roles will be updated with the new entry.
6. Click on ![](../.gitbook/assets/supportedactionsbutton.png) against a table entry to see the supported operations.

{% hint style="info" %}
**Relevant API List**

**For access to Swagger documentation, please traverse to link** ![](../.gitbook/assets/help.png) -----&gt; API Documentation from the Mangle UI or access [https://&lt;Mangle](https://<Mangle) _IP or Hostname&gt;/mangle-services/swagger-ui.html\#/role-controller_

![](../.gitbook/assets/rolecontroller.png)
{% endhint %}

## Loggers

### Log Levels

Mangle supports modifying log levels for the application.

**Steps to follow:**

1. Login as an admin user to Mangle.
2. Navigate to ![](../.gitbook/assets/settings%20%284%29.png) -----&gt; Loggers  -----&gt; Log Levels .
3. Click on ![](../.gitbook/assets/loggerbutton.png).
4. Enter Logger name, Configured Level, Effective Level and click on **Submit**.
5. A success message is displayed and the table for Log levels will be updated with the new entry.
6. Click on ![](../.gitbook/assets/supportedactionsbutton.png) against a table entry to see the supported operations.

{% hint style="info" %}
**Relevant API List**

**For access to Swagger documentation, please traverse to link** ![](../.gitbook/assets/help.png) -----&gt; API Documentation from the Mangle UI or access [https://&lt;Mangle](https://<Mangle) _IP or Hostname&gt;/mangle-services/swagger-ui.html\#/operation-handler_

![](../.gitbook/assets/operationhandlercontroller.png)
{% endhint %}

## Integrations

### Metric Providers

Mangle supports addition of either Wavefront or Datadog as metric providers. This enables the information about fault injection and remediation to be published to these tools as events thus making it easier to monitor them.

**Steps to follow:**

1. Login as an admin user to Mangle.
2. Navigate to ![](../.gitbook/assets/settings.png) -----&gt; Integrations  -----&gt; Metric Providers .
3. Click on ![](../.gitbook/assets/monitoringtoolbutton.png).
4. Choose Wavefront or Datadog, provide credentials and click on **Submit**.
5. A success message is displayed and the table for Monitoring tools will be updated with the new entry.
6. Click on ![](../.gitbook/assets/supportedactionsbutton.png) against a table entry to see the supported operations.

On adding a metric provider, Mangle will send events automatically to the enabled provider for every fault injected and remediated. If the requirement is to monitor Mangle as an application by looking at its metrics, then click on the ![](../.gitbook/assets/sendmetricsbutton.png) button to enable sending of Mangle application metrics to the corresponding metric provider.

{% hint style="info" %}
**Relevant API List**

**For access to Swagger documentation, please traverse to link** ![](../.gitbook/assets/help.png) -----&gt; API Documentation from the Mangle UI or access [https://&lt;Mangle](https://<Mangle) _IP or Hostname&gt;/mangle-services/swagger-ui.html\#_/operation-handler

![](../.gitbook/assets/operationhandlercontroller%20%281%29.png)
{% endhint %}

