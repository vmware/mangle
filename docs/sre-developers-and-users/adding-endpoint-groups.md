# Adding Endpoint Groups

## Why should I add Endpoint Groups?

If you need Mangle to consider a set of Endpoints as a single group at the time of fault execution especially in cases where the product is deployed as a cluster of nodes for high availability, then you would have to create an Endpoint group and use it at the time of fault execution or scheduling.

This will allow you to 

1. Randomly select one of the nodes in the endpoint cluster during fault execution OR
2. Run the same fault simultaneously on all the nodes in the endpoint cluster

## Endpoint Groups

**Steps to follow:** 

1. Login as an user with read and write privileges to Mangle.
2. Navigate to Endpoint tab ---&gt; Endpoints ---&gt; Endpoint Groups.
3. Click on ![](../.gitbook/assets/add_button%20%281%29.png) .
4. Enter a name, group type \(only remote machines are currently supported\), a list or two or more existing endpoints, tags \(refers to additional tags that should be send to the enabled metric provider to uniquely identify the endpoint group\) and click on **Test Connection**.
5. If **Test Connection** succeeds click on **Submit**.
6. A success message is displayed and the table for Endpoints will be updated with the new entry.
7. Edit, Delete, Enable and Disable actions are available for all added Endpoint groups.

Now you will have an option to select an endpoint group instead of an endpoint at the time of fault execution.

