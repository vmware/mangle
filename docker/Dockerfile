FROM photon:latest
MAINTAINER Gautam Kumar <kumargautam@vmware.com>
LABEL Description="This image is used for VMware Mangle web services."

# Install libraries and required components
RUN tdnf -y install shadow.x86_64 openjdk8 procps-ng.x86_64 tar gzip

# Install kubernetes
RUN curl -o /usr/bin/kubectl https://storage.googleapis.com/kubernetes-release/release/$(curl -s https://storage.googleapis.com/kubernetes-release/release/stable.txt)/bin/linux/amd64/kubectl \
&& chmod -R 755 /usr/bin/kubectl

# Install redis-cli client for Redis
COPY docker/redis-cli /usr/local/bin/
RUN chmod -R 755 /usr/local/bin/redis-cli

# Creating local user and group named "mangle"
RUN useradd -ms /bin/bash mangle \
&& groupadd mangle \
&& usermod -aG mangle mangle

# Making tomcat directories
ENV TOMCAT_DIR=/home/mangle/var/opt/mangle-tomcat
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
COPY mangle-services/target/mangle-services.jar $TOMCAT_DIR/mangle-services.jar
COPY mangle-default-plugin/target/mangle-default-plugin-3.5.0.zip $TOMCAT_DIR/plugins/mangle-default-plugin-3.5.0.zip

# Changing ownership of all files\directories of OPT_DIR
RUN chown -R mangle:mangle $OPT_DIR
USER mangle

# Setting working directory for web service
WORKDIR $TOMCAT_DIR

ENTRYPOINT sh $TOMCAT_DIR/start.sh
