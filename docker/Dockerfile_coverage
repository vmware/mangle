FROM vmware/photon
MAINTAINER Gautam Kumar <kumargautam@vmware.com>
LABEL Description="This image is used for VMware mangle web services."

# Install libraries and required components
RUN tdnf -y install openjdk procps-ng.x86_64 tar gzip

# Install kubernetes
RUN curl -o /usr/bin/kubectl https://storage.googleapis.com/kubernetes-release/release/$(curl -s https://storage.googleapis.com/kubernetes-release/release/stable.txt)/bin/linux/amd64/kubectl
RUN chmod -R 700 /usr/bin/kubectl

# Making tomcat directories
ENV TOMCAT_DIR=/var/opt/mangle-tomcat
ENV OPT_DIR=/var/opt
RUN mkdir -p $TOMCAT_DIR
RUN mkdir -p $TOMCAT_DIR/config/
RUN mkdir -p $OPT_DIR/vmware/mangle/cert/
RUN mkdir -p $TOMCAT_DIR/logs/

# Copy script and jar file
COPY docker/start_with_javaagent.sh $TOMCAT_DIR/
COPY docker/generateCert.sh $OPT_DIR/vmware/mangle/cert/
COPY mangle-services/target/mangle-services.jar $TOMCAT_DIR/mangle-services.jar
COPY mangle-default-plugin/target/mangle-default-plugin-1.0.0.zip $TOMCAT_DIR/plugins/mangle-default-plugin-1.0.0.zip
COPY docker/jacocoagent.jar $OPT_DIR/
COPY docker/jacococli.jar $OPT_DIR/

# Setting working directory for web service
WORKDIR $TOMCAT_DIR

ENTRYPOINT sh $TOMCAT_DIR/start_with_javaagent.sh