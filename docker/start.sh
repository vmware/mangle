#!/bin/sh
echo "generating ssl certificate..................."
OPT_DIR=/home/mangle/var/opt
/bin/sh $OPT_DIR/vmware/mangle/cert/generateCert.sh
cp $OPT_DIR/vmware/mangle/cert/server.* $OPT_DIR/mangle-tomcat/config/

echo "starting tomcat service...................."

#Possible CLUSTER_OPTIONS="-DpublicAddress=<public-ip> -DclusterMembers=<ip1, ip2> -DclusterValidationToken=<unique-string-for-cluster> -DclusterName=<cluster-name>"
if [ ! -z $SERVER_HTTP_PORT ]; then
    JAVA_OPTS="${JAVA_OPTS:-}  -Dserver.port.http=$SERVER_HTTP_PORT"
fi
if [ ! -z $SERVER_HTTPS_PORT ]; then
    JAVA_OPTS="${JAVA_OPTS:-}  -Dserver.port.https=$SERVER_HTTPS_PORT"
fi

java -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/home/mangle/var/opt/mangle-tomcat/logs -Xms512m -Xmx4G $JAVA_OPTS $DB_OPTIONS $CLUSTER_OPTIONS -jar $OPT_DIR/mangle-tomcat/mangle-services.jar