# Supported Deployment Models

## Single Node Deployments

### Deploying the Mangle Virtual Appliance

Using the OVA is a fast and easy way to create a Mangle VM on VMware vSphere.

After you have downloaded the OVA, log in to your vSphere environment and perform the following steps:

1. Start the Import Process

   From the Actions pull-down menu, choose **Create/Register VM**.

   ![Create/Register VM](../.gitbook/assets/ova-new-vm.png)

   In the Select creation type window, choose **Deploy a virtual machine from an OVF or OVA file**.

   \(images/vs-ova-new-vm-ova.png\)

   Choose **Next**.

2. Select the OVA File

   Enter a name for the virtual machine, and select the OVA file.

   ![OVA file](../.gitbook/assets/ova-name-selected.png)

   Choose **Next**.

3. Specify the Target Datastore

   From the Select storage screen, select the target datastore for your VM.

   ![Target datastore](../.gitbook/assets/ova-storage.png)

   Choose **Next**.

4. Accept the License Agreement

   Read through the Mangle License Agreement, and then choose **I Agree**.

   ![License](../.gitbook/assets/ova-license.png)

   Choose **Next**.

5. Select Deployment Options

   Mangle is provisioned with a maximum disk size. By default, Mangle uses only the portion of disk space that it needs, usually much less that the entire disk size \( **Thin** client\). If you want to pre-allocate the entire disk size \(reserving it entirely for Mangle instead\), select **Thick** instead.

   ![Deployment Options](../.gitbook/assets/ova-deployment-options.png)

   Choose **Next**.

6. Verify Deployment Settings

```text
Click **Finish**. vSphere uploads and validates your OVA. Depending on bandwidth, this operation might take a while.

When finished, vSphere powers up a new VM based on your selections.
```

After the VM is booted, open the command window. vSphere prompts you to log in.

**Note**: Because of limitations within OVA support on vSphere, it was necessary to specify a default password for the OVA option. However, all Mangle instances that are created by importing the OVA require an immediate password change upon login. The default account credentials are:

* Username: `root`
* Password: `changeme`

  After you provide these credentials, vSphere prompts you to create a new password and type it a second time to verify it.

  **Note:** For security, Mangle forbids common dictionary words for the root password.

  Once logged in, you will see the shell prompt.

Export the VM as a Template \(Optional\)

Consider converting this imported VM into a template \(from the Actions menu, choose **Export** \) so that you have a master Mangle instance that can be combined with vSphere Guest Customization to enable rapid provisioning of Mangle instances.

### Deploying the Mangle Containers

#### Prerequisites

Before creating the Mangle container a Cassandra DB container should be made available on a Docker host. You can setup a docker host by following the instructions [here](https://docs.docker.com/install/).

To deploy a Cassandra DB container without enabling authentication or ssl run the docker command below on the docker host.

```text
docker run --name mangle-cassandra -v /cassandra/storage/:/var/lib/cassandra -p 9042:9042 -d -e CASSANDRA_CLUSTER_NAME="manglecassandracluster" -e CASSANDRA_DC="DC1" -e CASSANDRA_RACK="rack1" -e CASSANDRA_ENDPOINT_SNITCH="GossipingPropertyFileSnitch"  cassandra:3.11
```

To enable authentication or clustering on Cassandra refer to the Cassandra Advanced Configuration.

To deploy the Mangle container run the docker command below on the docker host.

```text
docker run --name mangle -d -e DB_OPTIONS="-DcassandraContactPoints=$(docker inspect --format '{{ .NetworkSettings.IPAddress }}' mangle-cassandra) -DcassandraUsername= -DcassandraSslEnabled=false" -e CLUSTER_OPTIONS="-DhazelcastValidationToken=mangle" -p 8080:8080 -p 8443:8443 https://vmware.bintray.com/mangle:$MANGLE_BUILD
```

