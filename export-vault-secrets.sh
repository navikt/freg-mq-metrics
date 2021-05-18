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
