# Advanced Cassandra Configuration

## To enable authentication

Open **/etc/cassandra/cassandra.yaml** and modify **authenticator**: from **AllowAllAuthenticator** to **PasswordAuthenticator**, so Cassandra will create a default user cassandra/cassandra.

**To create own user** : create dir **/docker-entrypoint-initdb.d/** and create cql file **init-query.cql** with content (CREATE USER IF NOT EXISTS admin WITH PASSWORD 'vmware' SUPERUSER;) so it will create a user admin/vmware.

To execute the init-query.cql file on db startup, need to modify the **docker-entrypoint.sh** file, add the below content right before **exec "$@"**

`for f in docker-entrypoint-initdb.d/*; do  `\
`case "$f" in  `\
`*.sh) echo "$0: running $f"; . "$f" ;;  `\
`*.cql) echo "$0: running $f" && until cqlsh --ssl -u cassandra -p cassandra -f "$f"; do >&2 echo "Cassandra is unavailable - sleeping"; sleep 2; done & ;;  `\
`*) echo "$0: ignoring $f" ;;  `\
`esac  `\
`echo  `\
`done`

Here, **cqlsh --ssl -u cassandra -p cassandra** used to run \*.cql file (if ssl is not enabled then remove --ssl option)

Modify the **start\_rpc: true** in **/etc/cassandra/cassandra.yaml** file.

**To enable the SSL** : generate the self sign certificate(Run **generateDbCert.sh** file inside container) and modify the **/etc/cassandra/cassandra.yaml** file with below content

```
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

```
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

Exit from the running container and restart the container.

Login : **cqlsh --ssl -u cassandra -p cassandra** .

See logs : **/var/log/cassandra** .

Attaching the required files which help to enable the authentication and ssl in Cassandra base image.

{% file src="../../../.gitbook/assets/docker-entrypoint (1).sh" %}
Docker Entry Point Script
{% endfile %}

{% file src="../../../.gitbook/assets/cqlshrc (1).txt" %}
cqlshrc Config File
{% endfile %}

{% file src="../../../.gitbook/assets/init-query (1).cql" %}
Init Query cql File
{% endfile %}

{% file src="../../../.gitbook/assets/cassandra (1).yaml" %}
Cassandra YAML File
{% endfile %}

{% file src="../../../.gitbook/assets/generatedbcert (1).sh" %}
Certificate Generation Script
{% endfile %}

To download the Cassandra client as DevCenter from [DevCenter](https://academy.datastax.com/downloads).

**To Create Multi-Node Cassandra cluster**

Create seed Node :

```
docker run --name mangle-cassandradb -v /cassandra/storage/:/var/lib/cassandra -p 9042:9042 -p 7000:7000 -p 7001:7001 -d -e CASSANDRA_BROADCAST_ADDRESS=<Node IP> -e CASSANDRA_SEEDS=<Node IP> -e CASSANDRA_CLUSTER_NAME="manglecassandracluster" -e CASSANDRA_DC="DC1" -e CASSANDRA_RACK="rack1" -e CASSANDRA_ENDPOINT_SNITCH="GossipingPropertyFileSnitch"  mangleuser/mangle_cassandradb:1.0
```

Join the Other Node to Seed Node :

```
docker run --name mangle-cassandradb -v /cassandra/storage/:/var/lib/cassandra -p 9042:9042 -p 7000:7000 -p 7001:7001 -d -e CASSANDRA_BROADCAST_ADDRESS=<Node IP> -e CASSANDRA_SEEDS=<Seed Node IP> -e CASSANDRA_CLUSTER_NAME="manglecassandracluster" -e CASSANDRA_DC="DC1" -e CASSANDRA_RACK="rack1" -e CASSANDRA_ENDPOINT_SNITCH="GossipingPropertyFileSnitch"  mangleuser/mangle_cassandradb:1.0
```
