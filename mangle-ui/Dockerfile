FROM photon:latest

MAINTAINER Shashi Ranjan <ranjans@vmware.com>

RUN tdnf install -y tar sed gzip xz \
&& curl -O https://nodejs.org/dist/v10.15.1/node-v10.15.1-linux-x64.tar.xz \
&& mkdir -p /node/ \
&& tar -xJf node-v10.15.1-linux-x64.tar.xz -C /node \
&& ln -sf /node/node-v10.15.1-linux-x64/bin/node /usr/bin/node \
&& ln -sf /node/node-v10.15.1-linux-x64/bin/npm /usr/bin/npm \
&& rm -rf node-v10.15.1-linux-x64.tar.xz \
&& mkdir -p /home/mangle/var/opt/mangle-ui/

COPY . /home/mangle/var/opt/mangle-ui/

WORKDIR /home/mangle/var/opt/mangle-ui

EXPOSE 4200

ENTRYPOINT sh mangleEntry.sh