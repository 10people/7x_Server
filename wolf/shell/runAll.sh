#!/bin/bash

chmod +x *.sh;
./runDb.sh start;
sleep 3;

./runRouter.sh start;
sleep 3;

./runFriend.sh start;
sleep 3;

