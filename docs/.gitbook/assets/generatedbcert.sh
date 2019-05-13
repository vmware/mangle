#!/bin/bash
KEY_STORE_PATH="/cassandra/certs"
mkdir -p "$KEY_STORE_PATH"
KEY_STORE="$KEY_STORE_PATH/cassandra.keystore"
PKS_KEY_STORE="$KEY_STORE_PATH/cassandra.pks12.keystore"
TRUST_STORE="$KEY_STORE_PATH/cassandra.truststore"
CLUSTER_NAME=fiaascocassandra
CLUSTER_PUBLIC_CERT="$KEY_STORE_PATH/CLUSTER_${CLUSTER_NAME}_PUBLIC.cer"
CLIENT_PUBLIC_CERT="$KEY_STORE_PATH/CLIENT_${CLUSTER_NAME}_PUBLIC.cer"

cn=localhost
country='US'
state='CA'
locality_name='Palo Alto'
organization='VMware'
organization_unit='fiaasco'
certExpirationDays=36500
PASSWORD=vmware

### Cluster key setup.
# Create the cluster key for cluster communication.
keytool -genkey -keyalg RSA -alias "${CLUSTER_NAME}cluster" -keystore "$KEY_STORE" -storepass "$PASSWORD" -keypass "$PASSWORD" \
-dname "CN=${cn}, OU=${organization_unit}, O=${organization}, L=${locality_name}, ST=${state}, C=${country}" \
-validity $certExpirationDays

# Create the public key for the cluster which is used to identify nodes.
keytool -export -alias "${CLUSTER_NAME}cluster" -file "$CLUSTER_PUBLIC_CERT" -keystore "$KEY_STORE" \
-storepass "$PASSWORD" -keypass "$PASSWORD" -noprompt
# Import the identity of the cluster public cluster key into the trust store so that nodes can identify each other.
keytool -import -v -trustcacerts -alias "${CLUSTER_NAME}cluster" -file "$CLUSTER_PUBLIC_CERT" -keystore "$TRUST_STORE" \
-storepass "$PASSWORD" -keypass "$PASSWORD" -noprompt

### Client key setup.
# Create the client key for CQL.
keytool -genkey -keyalg RSA -alias "${CLUSTER_NAME}client" -keystore "$KEY_STORE" -storepass "$PASSWORD" -keypass "$PASSWORD" \
-dname "CN=${cn}, OU=${organization_unit}, O=${organization}, L=${locality_name}, ST=${state}, C=${country}" \
-validity $certExpirationDays

# Create the public key for the client to identify itself.
keytool -export -alias "${CLUSTER_NAME}client" -file "$CLIENT_PUBLIC_CERT" -keystore "$KEY_STORE" \
-storepass "$PASSWORD" -keypass "$PASSWORD" -noprompt

# Import the identity of the client pub  key into the trust store so nodes can identify this client.
keytool -importcert -v -trustcacerts -alias "${CLUSTER_NAME}client" -file "$CLIENT_PUBLIC_CERT" -keystore "$TRUST_STORE" \
-storepass "$PASSWORD" -keypass "$PASSWORD" -noprompt

keytool -importkeystore -srckeystore "$KEY_STORE" -destkeystore "$PKS_KEY_STORE" -deststoretype PKCS12 \
-srcstorepass "$PASSWORD" -deststorepass "$PASSWORD"

openssl pkcs12 -in "$PKS_KEY_STORE" -nokeys -out "$KEY_STORE_PATH/${CLUSTER_NAME}_CLIENT.cer.pem" -passin pass:$PASSWORD
openssl pkcs12 -in "$PKS_KEY_STORE" -nodes -nocerts -out "$KEY_STORE_PATH/${CLUSTER_NAME}_CLIENT.key.pem" -passin pass:$PASSWORD