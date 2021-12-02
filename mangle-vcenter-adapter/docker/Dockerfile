FROM photon:latest
MAINTAINER Chethan C <chetanc@vmware.com>
LABEL Description="This image is used by VMware Mangle web services for VC fault injection."

# Install libraries and required components
RUN tdnf -y install openjre8 shadow.x86_64

# Creating local user and group named "mangle"
RUN useradd -ms /bin/bash mangle \
&& groupadd mangle \
&& usermod -aG mangle mangle

# Making tomcat directories
ENV TOMCAT_DIR=/home/mangle/var/opt/mangle-vc-adapter-tomcat
ENV OPT_DIR=/home/mangle/var/opt

RUN echo $TOMCAT_DIR \
&& echo $OPT_DIR \
&& mkdir -p $TOMCAT_DIR \
&& mkdir -p $TOMCAT_DIR/config/ \
&& mkdir -p $OPT_DIR/vmware/mangle/cert/ \
&& mkdir -p $TOMCAT_DIR/logs/

# Copy script and jar file
COPY docker/start.sh $TOMCAT_DIR
COPY docker/generateCert.sh $OPT_DIR/vmware/mangle/cert/
COPY target/mangle-vcenter-adapter-3.5.0.jar $TOMCAT_DIR/app.jar

# Changing ownership of all files\directories of OPT_DIR
RUN chown -R mangle:mangle $OPT_DIR
USER mangle

# Setting working directory for web service
WORKDIR $TOMCAT_DIR

EXPOSE 8080 8443
ENTRYPOINT sh $TOMCAT_DIR/start.sh