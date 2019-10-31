#!/bin/sh
echo "generating ssl certificate..................."
VAR_OPT=/home/mangle/var/opt
OPT_VMWARE_MANGLE=$VAR_OPT/vmware/mangle
/bin/sh $OPT_VMWARE_MANGLE/cert/generateCert.sh
cp $OPT_VMWARE_MANGLE/cert/server.* $VAR_OPT/mangle-tomcat/config/

echo "starting tomcat service...................."
#Possible CLUSTER_OPTIONS="-DpublicAddress=<public-ip> -DclusterMembers=<ip1, ip2> -DclusterValidationToken=<unique-string-for-cluster> -DclusterName=<cluster-name>"
java -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/home/mangle/var/opt/mangle-tomcat/logs -Xms512m -Xmx4G $JAVA_OPTS $DB_OPTIONS $CLUSTER_OPTIONS -javaagent:$VAR_OPT/jacocoagent.jar=port=36320,destfile=jacoco-it.exec,output=tcpserver -jar $VAR_OPT/mangle-tomcat/mangle-services.jar