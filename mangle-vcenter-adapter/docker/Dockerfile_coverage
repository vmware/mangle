FROM vmware/photon
MAINTAINER Chethan C <chetanc@vmware.com>
LABEL Description="This image is used by VMware Mangle web services for VC fault injection."

# Install libraries and required components
RUN tdnf -y install openjre

# Making tomcat directories
ENV TOMCAT_DIR=/var/opt/mangle-vc-adapter-tomcat
ENV OPT_DIR=/var/opt
RUN echo $TOMCAT_DIR
RUN echo $OPT_DIR
RUN mkdir -p $TOMCAT_DIR
RUN mkdir -p $TOMCAT_DIR/config/
RUN mkdir -p $OPT_DIR/vmware/mangle/cert/
RUN mkdir -p $TOMCAT_DIR/logs/

# Copy script and jar file
COPY docker/start_with_javaagent.sh $TOMCAT_DIR
COPY docker/generateCert.sh $OPT_DIR/vmware/mangle/cert/
COPY target/mangle-vcenter-adapter-1.0.0.jar $TOMCAT_DIR/app.jar
COPY docker/jacocoagent.jar $OPT_DIR/
COPY docker/jacococli.jar $OPT_DIR/

# Setting working directory for web service
WORKDIR $TOMCAT_DIR

EXPOSE 8080 8443 36320
ENTRYPOINT sh $TOMCAT_DIR/start_with_javaagent.sh