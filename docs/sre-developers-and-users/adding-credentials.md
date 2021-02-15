# Adding Credentials

## Why should I add credentials?

Mangle uses the credentials to connect to any endpoint where you would like to run faults. So you will have a credential type for each endpoint that Mangle supports and without the credentials, Mangle cannot run any faults.

Credentials are encrypted before they are stored in Mangle and hence is secure.

Credentials can be reused across endpoints if it is configured accordingly for multiple endpoints of the same type.

## Credential Types

The following endpoints supported in Mangle would require a corresponding credential to be provided.

1. Remote Machine - Accepts an SSH enabled username, password or private key file
2. Kubernetes \(K8s\) Cluster - Accepts a K8s kubeconfig file
3. VMware vCenter - Accepts a username and password
4. AWS - Accepts an Access key ID and Secret Key
5. Azure - Accepts a Client Application ID and Client Application Secret Kay
6. Database - Accepts a Database type, Database name, username, password and port

**Steps to follow:** 

1. Login as an user with read and write privileges to Mangle.
2. Navigate to Endpoint tab ---&gt; Credentials ---&gt; Endpoint Credentials
3. Click on ![](../.gitbook/assets/add_credentials.png) . Select the appropriate Endpoint type.
4. Enter the appropriate details and click on **Submit**.
5. A success message is displayed and the table for Credentials will be updated with the new entry.
6. Edit and Delete operations are supported for all saved credentials.

