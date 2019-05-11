# Advanced Cassandra Configuration

### To enable authentication

Open **/etc/cassandra/cassandra.yaml** and modify **authenticator**: from **AllowAllAuthenticator** to **PasswordAuthenticator**, so Cassandra will create a default user cassandra/cassandra.

**To create own user** : create dir **/docker-entrypoint-initdb.d/** and create cql file **init-query.cql** with content \(CREATE USER IF NOT EXISTS admin WITH PASSWORD 'vmware' SUPERUSER;\) so it will create a user admin/vmware.

To execute the init-query.cql file on db startup, need to modify the **docker-entrypoint.sh** file, add the below content right before **exec "$@"**

`for f in docker-entrypoint-initdb.d/*; do  
case "$f" in  
*.sh) echo "$0: running $f"; . "$f" ;;  
*.cql) echo "$0: running $f" && until cqlsh --ssl -u cassandra -p cassandra -f "$f"; do >&2 echo "Cassandra is unavailable - sleeping"; sleep 2; done & ;;  
*) echo "$0: ignoring $f" ;;  
esac  
echo  
done`

Here, **cqlsh --ssl -u cassandra -p cassandra** used to run \*.cql file \(if ssl is not enabled then remove --ssl option\)

Modify the **start\_rpc: true** in **/etc/cassandra/cassandra.yaml** file.

**To enable the SSL** : generate the self sign certificate\(Run **generateDbCert.sh** file inside container\) and modify the  **/etc/cassandra/cassandra.yaml** file with below content

```text
server_encryption_options:
internode_encryption: all
keystore: /cassandra/certs/cassandra.keystore
keystore_password: vmware
truststore: /cassandra/certs/cassandra.truststore
truststore_password: vmware

# More advanced defaults below:
protocol: TLS
algorithm: SunX509
store_type: JKS
cipher_suites: [TLS_RSA_WITH_AES_128_CBC_SHA,TLS_RSA_WITH_AES_256_CBC_SHA,TLS_DHE_RSA_WITH_AES_128_CBC_SHA,TLS_DHE_RSA_WITH_AES_256_CBC_SHA,TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA,TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA]
require_client_auth: false
# require_endpoint_verification: false
# enable or disable client/server encryption
client_encryption_options:
enabled: true
# If enabled and optional is set to true encrypted and unencrypted connections are handled.
optional: false
keystore: /cassandra/certs/cassandra.keystore
keystore_password: vmware
require_client_auth: false

# Set trustore and truststore_password if require_client_auth is true
truststore: /cassandra/certs/cassandra.truststore
truststore_password: vmware

# More advanced defaults below:
protocol: TLS
algorithm: SunX509
store_type: JKS
cipher_suites: [TLS_RSA_WITH_AES_128_CBC_SHA,TLS_RSA_WITH_AES_256_CBC_SHA,TLS_DHE_RSA_WITH_AES_128_CBC_SHA,TLS_DHE_RSA_WITH_AES_256_CBC_SHA,TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA,TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA]
```

To login cqlsh client : need to create a **cqlshrc** file and copy in **/root/.cassandra/** and **/home/cassandra/.cassandra/** folder 

```text
[authentication]
username = cassandra
password = cassandra

[connection]
hostname = 127.0.0.1
port = 9042
factory = cqlshlib.ssl.ssl_transport_factory

[ssl]
certfile = /cassandra/certs/fiaascocassandra_CLIENT.cer.pem
# Optional, true by default
validate = false
# Next 2 lines must be provided when require_client_auth = true in the cassandra.yaml file
# userkey = /cassandra/certs/fiaascocassandra_CLIENT.key.pem
# usercert = /cassandra/certs/fiaascocassandra_CLIENT.cer.pem
```

* Exit from the running container and restart the container.
* Login : **cqlsh --ssl -u cassandra -p cassandra** .
* See logs : **/var/log/cassandra** .
* Attaching the required file which help to enable the authentication and ssl in Cassandra base image.
* [![](https://confluence.eng.vmware.com/s/en_GB/7502/03f55dd28656fecb49cdf79d0ae052058a20a5d2/3.0.1/_/download/resources/com.atlassian.confluence.plugins.confluence-view-file-macro:view-file-macro-resources/images/placeholder-medium-file.png)docker-entrypoint.sh](https://confluence.eng.vmware.com/download/attachments/376298391/docker-entrypoint.sh?version=1&modificationDate=1548139119000&api=v2)[![](https://confluence.eng.vmware.com/s/en_GB/7502/03f55dd28656fecb49cdf79d0ae052058a20a5d2/3.0.1/_/download/resources/com.atlassian.confluence.plugins.confluence-view-file-macro:view-file-macro-resources/images/placeholder-medium-file.png)cqlshrc](https://confluence.eng.vmware.com/download/attachments/376298391/cqlshrc?version=1&modificationDate=1548139118000&api=v2)[![](https://confluence.eng.vmware.com/s/en_GB/7502/03f55dd28656fecb49cdf79d0ae052058a20a5d2/3.0.1/_/download/resources/com.atlassian.confluence.plugins.confluence-view-file-macro:view-file-macro-resources/images/placeholder-medium-file.png)init-query.cql](https://confluence.eng.vmware.com/download/attachments/376298391/init-query.cql?version=1&modificationDate=1548139117000&api=v2)[![](https://confluence.eng.vmware.com/s/en_GB/7502/03f55dd28656fecb49cdf79d0ae052058a20a5d2/3.0.1/_/download/resources/com.atlassian.confluence.plugins.confluence-view-file-macro:view-file-macro-resources/images/placeholder-medium-file.png)cassandra.yaml](https://confluence.eng.vmware.com/download/attachments/376298391/cassandra.yaml?version=1&modificationDate=1548139117000&api=v2)[![](https://confluence.eng.vmware.com/s/en_GB/7502/03f55dd28656fecb49cdf79d0ae052058a20a5d2/3.0.1/_/download/resources/com.atlassian.confluence.plugins.confluence-view-file-macro:view-file-macro-resources/images/placeholder-medium-file.png)generateDbCert.sh](https://confluence.eng.vmware.com/download/attachments/376298391/generateDbCert.sh?version=1&modificationDate=1548139116000&api=v2) 

To download the Cassandra client as DevCenter form [DevCenter](https://academy.datastax.com/downloads).

**To Create Multi-Node Cassandra cluster :**Create seed Node : `docker run --name mangle-cassandra -v /cassandra/storage/:/var/lib/cassandra -p 9042:9042 -d -e CASSANDRA_DC="DC1" -e CASSANDRA_RACK="rack1" -e CASSANDRA_ENDPOINT_SNITCH="GossipingPropertyFileSnitch"`  [`<`_`location of image`_`>/cassandradb:1.0`](http://es-fault-injection-docker-local.artifactory.eng.vmware.com/fiaasco/cassandradb:1.0)`" .Join the Other Node in Seed Node : "docker run --name mangle-cassandra-n1 -v /cassandra/n1storage/:/var/lib/cassandra -p 9043:9042 -d -e CASSANDRA_SEEDS="$(docker inspect --format '{{ .NetworkSettings.IPAddress }}' mangle-cassandra)" -e CASSANDRA_DC="DC1" -e CASSANDRA_RACK="rack1" -e CASSANDRA_ENDPOINT_SNITCH="GossipingPropertyFileSnitch"`  [`<`_`location of image`_`>/cassandradb:1.0`](http://es-fault-injection-docker-local.artifactory.eng.vmware.com/fiaasco/cassandradb:1.0)\`\`

