#!/bin/sh
cd /var/opt/mangle-ui
if [ -z "${MANGLE_CORE_IP}" ]
then
    export MANGLE_CORE_IP=localhost
else
    echo "MANGLE_CORE_IP is ${MANGLE_CORE_IP}"
fi

if [ -z "${MANGLE_CORE_PORT}" ]
then
    export MANGLE_CORE_PORT=8443
else
    echo "MANGLE_CORE_PORT is ${MANGLE_CORE_PORT}"
fi

echo "mangle-ui is now pointing to mangle-core URL=https://${MANGLE_CORE_IP}:${MANGLE_CORE_PORT}/mangle-services/"
sed -i "s/localhost/${MANGLE_CORE_IP}/g" /var/opt/mangle-ui/proxy.config.json
sed -i "s/8443/${MANGLE_CORE_PORT}/g" /var/opt/mangle-ui/proxy.config.json

echo "Installing angular npm packages dependencies..."
npm install

echo "Starting mangle-ui app..."
# npm start
npm install -g @angular/cli@7.1.1
alias ng="/node/node-v10.15.1-linux-x64/lib/node_modules/@angular/cli/bin/ng"
ng serve --proxy-config proxy.config.json --ssl true --host 0.0.0.0