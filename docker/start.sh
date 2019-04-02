#!/bin/sh
echo "generating ssl certificate..................."
OPT_DIR=/var/opt
chmod +x $OPT_DIR/vmware/mangle/cert/generateCert.sh
$OPT_DIR/vmware/mangle/cert/generateCert.sh
cp $OPT_DIR/vmware/mangle/cert/server.* $OPT_DIR/mangle-tomcat/config/

echo "starting tomcat service...................."

#Possible CLUSTER_OPTIONS="-DhazelcastPublicAddress=<public-ip> -DhazelcastMembers=<ip1, ip2> -DhazelcastValidationToken=<unique-string-for-cluster>"
java -Xms512m -Xmx4G $JAVA_OPTS $DB_OPTIONS $CLUSTER_OPTIONS -jar $OPT_DIR/mangle-tomcat/mangle-services.jar
