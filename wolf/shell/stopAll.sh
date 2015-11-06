#!/bin/bash

chmod +x *.sh;
./runDb.sh stop;
sleep 3;

./runRouter.sh stop;
sleep 3;

./runChat.sh stop;
sleep 3;

./runFriend.sh stop;
sleep 3;

