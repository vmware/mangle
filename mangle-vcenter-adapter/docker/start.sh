#!/bin/sh
echo "generating ssl certificate..................."
OPT_DIR=/var/opt
chmod +x $OPT_DIR/vmware/mangle/cert/generateCert.sh
$OPT_DIR/vmware/mangle/cert/generateCert.sh
cp $OPT_DIR/vmware/mangle/cert/server.* $OPT_DIR/mangle-vc-adapter-tomcat/config/

echo "starting tomcat service...................."

java -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/var/opt/mangle-tomcat/logs -Xms512m -Xmx4G -jar $OPT_DIR/mangle-vc-adapter-tomcat/app.jar
