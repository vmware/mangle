# Adding Endpoints

## Supported Endpoints

Endpoint in Mangle refers to an infrastructure component that will be the primary target for your chaos engineering experiments. For version 1.0, Mangle supports four types of endpoints:

1. Kubernetes
2. Docker
3. VMware vCenter
4. Remote Machine

#### Kubernetes Endpoint

Mangle supports k8s clusters as endpoints or targets for injection. It needs a kubeconfig file to connect to the cluster and run the supported faults.

| Supported Versions of K8s |
| :--- |
|  |

**Steps to follow:** 

1. Login as an user with read and write privileges to Mangle.
2. Navigate to Endpoint tab.
3. Click on ![](../.gitbook/assets/k8sclusterbutton.png).
4. Enter a name, credential \(kubeconfig file\), namespace \(if not specified the default namespace in k8s will be taken\), tags \(refers to additional tags that should be send to the enabled metric provider to uniquely identify faults against that endpoint\) and click on **Test Connection**.
5. If **Test Connection** succeeds click on **Submit**.
6. A success message is displayed and the table for Endpoints will be updated with the new entry.
7. Click on ![](../.gitbook/assets/supportedactionsbutton.png) against a table entry to see the supported operations.

#### Docker Endpoint

Mangle supports docker hosts as endpoints or targets for injection. It needs the IP/Hostname, port details and certificate details \(if TLS is enabled for the docker host\) to connect to the docker host and run the supported faults.

| Supported Versions of Docker |
| :--- |
|  |

**Steps to follow:** 

1. Login as an user with read and write privileges to Mangle.
2. Navigate to Endpoint tab.
3. Click on ![](../.gitbook/assets/dockerbutton.png).
4. Enter a name, IP/Hostname, port details and certificate details \(if TLS is enabled for the docker host\)and click on **Test Connection**.
5. If **Test Connection** succeeds click on **Submit**.
6. A success message is displayed and the table for Endpoints will be updated with the new entry.
7. Click on ![](../.gitbook/assets/supportedactionsbutton.png) against a table entry to see the supported operations.

