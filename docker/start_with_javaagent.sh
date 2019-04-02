#!/bin/sh
echo "generating ssl certificate..................."
VAR_OPT=/var/opt
OPT_VMWARE_MANGLE=$VAR_OPT/vmware/mangle
chmod +x /var/opt/vmware/mangle/cert/generateCert.sh
$OPT_VMWARE_MANGLE/cert/generateCert.sh
cp $OPT_VMWARE_MANGLE/cert/server.* $VAR_OPT/mangle-tomcat/config/

echo "starting tomcat service...................."
#Possible CLUSTER_OPTIONS="-DhazelcastPublicAddress=<public-ip> -DhazelcastMembers=<ip1, ip2> -DhazelcastValidationToken=<unique-string-for-cluster>"
java -Xms512m -Xmx4G $JAVA_OPTS $DB_OPTIONS $CLUSTER_OPTIONS -javaagent:$VAR_OPT/jacocoagent.jar=port=36320,destfile=jacoco-it.exec,output=tcpserver -jar $VAR_OPT/mangle-tomcat/mangle-services.jar