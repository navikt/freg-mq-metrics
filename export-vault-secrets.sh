#!/usr/bin/env sh

if test -f /secrets/serviceuser/srvfregmqmetrics/username;
then
    echo "Setting serviceuser_username"
    export  serviceuser_username=$(cat /secrets/serviceuser/srvfregmqmetrics/username)
fi
if test -f /secrets/serviceuser/srvfregmqmetrics/password;
then
    echo "Setting serviceuser_password"
    export  serviceuser_password=$(cat /secrets/serviceuser/srvfregmqmetrics/password)
fi

if test -f /var/run/secrets/nais.io/srvmqinquire/username;
then
    echo "Setting MQADMIN_USERNAME"
    export MQADMIN_USERNAME=$(cat /var/run/secrets/nais.io/srvmqinquire/username)
fi
mqadmin
if test -f /var/run/secrets/nais.io/srvmqinquire/password;
then
    echo "Setting MQADMIN_PASSWORD"
    export MQADMIN_PASSWORD=$(cat /var/run/secrets/nais.io/srvmqinquire/password)
fi