# Admin Settings

## Managing Authentication

#### Adding additional Authentication sources

Mangle supports using Active Directory as an additional authentication source. 

**Steps to follow:** 

1. Login as an admin user to Mangle.
2. Navigate to  -----&gt; Auth Management  -----&gt; Auth Source .
3. Click on .
4. Enter URL, Domain and click on **Submit**.
5. A success message is displayed and the table for Auth sources will be updated with the new entry.
6. Click on  against a table entry to see the supported operations.

{% hint style="info" %}
**Relevant API List**

**For access to Swagger documentation, please traverse to link**  -----&gt; API Documentation from the Mangle UI or access _https://&lt;Mangle IP or Hostname&gt;/mangle-services/swagger-ui.html_

  
{% endhint %}

#### Adding/Importing Users

Mangle supports adding new local user or importing users from Active Directory sources added as additional authentication sources. 

**Steps to follow:** 

1. Login as an admin user to Mangle.
2. Navigate to  -----&gt; Auth Management  -----&gt; Users .
3. Click on .
4. Enter User Name, Auth Source, Password if the Auth Source selected is "mangle.local", an appropriate role and click on **Submit**.
5. A success message is displayed and the table for Users will be updated with the new entry.
6. Click on  against a table entry to see the supported operations.

{% hint style="info" %}
**Relevant API List**

**For access to Swagger documentation, please traverse to link**  -----&gt; API Documentation from the Mangle UI or access _https://&lt;Mangle IP or Hostname&gt;/mangle-services/swagger-ui.html_

  
{% endhint %}

#### Default and Custom Roles

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
2. Navigate to  -----&gt; Auth Management  -----&gt; Roles.
3. Click on .
4. Enter Role Name, Privileges and click on **Submit**.
5. A success message is displayed and the table for Roles will be updated with the new entry.
6. Click on  against a table entry to see the supported operations.

{% hint style="info" %}
**Relevant API List**

**For access to Swagger documentation, please traverse to link**  -----&gt; API Documentation from the Mangle UI or access _https://&lt;Mangle IP or Hostname&gt;/mangle-services/swagger-ui.html_

  
{% endhint %}

