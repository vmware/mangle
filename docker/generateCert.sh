#!/bin/sh

public='/home/mangle/var/opt/vmware/mangle/cert/server.jks'
cn=localhost
country='US'
state='CA'
locality_name='Palo Alto'
organization='VMware'
organization_unit='mangle'
certExpirationDays=1825
password=vmware

keytool -genkey -alias server -keypass $password -storepass $password -storetype JKS -keyalg RSA -keysize 2048 -keystore $public -validity $certExpirationDays -dname "C=${country},ST=${state},L=${locality_name},O=${organization},OU=${organizational_unit},CN=${cn}"

exit 0