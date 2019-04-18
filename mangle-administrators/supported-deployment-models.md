# Supported Deployment Models

## Deploying a Mangle Single Node Instance

For a quick POC we recommend that you deploy a single node instance of Mangle using the OVA file that we have made available [here](https://dl.bintray.com/vmware/photon/3.0/GA/ova/photon-hw11-3.0-26156e2.ova). 

Using the OVA is a fast and easy way to create a Mangle VM on VMware vSphere.

### Importing the OVA for Mangle

After you have downloaded the OVA, log in to your vSphere environment and perform the following steps:

1. Start the Import Process

   From the Actions pull-down menu, choose **Create/Register VM**.

   ![Create/Register VM](../.gitbook/assets/ova-new-vm.png)

   In the Select creation type window, choose **Deploy a virtual machine from an OVF or OVA file**.

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

   Read through the Photon OS License Agreement, and then choose **I Agree**.

   ![License](../.gitbook/assets/ova-license.png)

   Choose **Next**.

5. Select Deployment Options

   Photon OS is provisioned with a maximum disk size. By default, Photon OS uses only the portion of disk space that it needs, usually much less that the entire disk size \( **Thin** client\). If you want to pre-allocate the entire disk size \(reserving it entirely for Photon OS instead\), select **Thick** instead.

   ![Deployment Options](../.gitbook/assets/ova-deployment-options.png)

   Choose **Next**.

6. Verify Deployment Settings

```text
Click **Finish**. vSphere uploads and validates your OVA. Depending on bandwidth, this operation might take a while.

When finished, vShield powers up a new VM based on your selections.
```

After the VM is booted, open the command window. vSphere prompts you to log in.

**Note**: Because of limitations within OVA support on vSphere, it was necessary to specify a default password for the OVA option. However, all Photon OS instances that are created by importing the OVA require an immediate password change upon login. The default account credentials are:

* Username: `root`
* Password: `changeme`

  After you provide these credentials, vSphere prompts you to create a new password and type it a second time to verify it.

  **Note:** For security, Photon OS forbids common dictionary words for the root password.

  Once logged in, you will see the shell prompt.

  Once complete, proceed to [Admin Settings](https://app.gitbook.com/@vmware-1/s/workspace/~/edit/drafts/-LcZkqkSHlxAweetM4Ds/mangle-administrators/admin-settings).

### Deploying the Mangle Container

#### Pre-Requisites

1. Mangle required a Docker host machine/VM to be up and running. 
2. Mangle requires a Cassandra DB container to be up and running for persistence. To run a Cassandra container without enabling authentication and ssl, use the command provided below:

```text
docker run --name mangle-cassandra -v /cassandra/storage/:/var/lib/cassandra -p 9042:9042 -d -e CASSANDRA_CLUSTER_NAME="manglecassandracluster" -e CASSANDRA_DC="DC1" -e CASSANDRA_RACK="rack1" -e CASSANDRA_ENDPOINT_SNITCH="GossipingPropertyFileSnitch"  cassandra:3.11
```

{% hint style="info" %}
To enable authentication for Cassandra: 

Open **/etc/cassandra/cassandra.yaml** and modify **authenticator**: from **AllowAllAuthenticator** to **PasswordAuthenticator.** Restart container, Cassandra will create a default user cassandra/cassandra

For more details on Secured Cassandra refer to document [Secured Cassandra](https://confluence.eng.vmware.com/display/ES/Secured+Cassandra)
{% endhint %}

#### Running Mangle and the vSphere Adapter as containers

After deploying the Cassandra DB container, follow one of the instrauctions below to run the Mangle and vSphere Adapter containers.

If the Cassandra container doesn't have authentication and ssl enabled then run:

```text
docker run --name mangle -d -e DB_OPTIONS="-DcassandraContactPoints=$(docker inspect --format '{{ .NetworkSettings.IPAddress }}' mangle-cassandra) -DcassandraUsername= -DcassandraSslEnabled=false" -e CLUSTER_OPTIONS="-DhazelcastValidationToken=mangle" -p 8080:8080 -p 8443:8443 es-fault-injection-docker-local.artifactory.eng.vmware.com/mangle:$MANGLE_BUILD
```

## Deploying a Mangle Multi-Node Instance

