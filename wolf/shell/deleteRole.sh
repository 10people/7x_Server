#!/bin/bash
# 最低等级，最高等级，每次最多删号的数量，最大未登录时间
MIN_LEVEL=0
MAX_LEVEL=1
MAX_NUM=10000
MAX_DAYS=10
CONF_FILE="$TX_CONF_PATH/conf.sh"


DB_IP_NAME=`cat $TX_CONF_PATH/conf.properties | grep mysql.dbServer | cut -d"=" -f2`
DB_USER=`cat $TX_CONF_PATH/conf.properties | grep mysql.user | cut -d"=" -f2`
DB_PASSWD=`cat $TX_CONF_PATH/conf.properties | grep mysql.passwd | cut -d"=" -f2`

DB_IP=`echo $DB_IP_NAME | cut -d"/" -f1`
DB_NAME=`echo $DB_IP_NAME | cut -d"/" -f2`

echo "db params: $DB_IP $DB_NAME $DB_USER $DB_PASSWD"

if [ -e $CONF_FILE ];then
        source $CONF_FILE
fi

SQL="call filterAndDelDirtyUser()"

echo -e "\033[32;31;1;2m 开始进行角色脏数据清理!! \033[m";
echo -e "\033[32;31;1;2m 10秒后开始执行!! \033[m";
COUNT=10
i=0
while [ $i -lt $COUNT ]
do
        printf "\r$i s remain!!"
        sleep 1
        i=$((i + 1))
done
echo "\n"
mysql -u$DB_USER -p$DB_PASSWD -h$DB_IP $DB_NAME -e "$SQL"

echo -e "\033[32;31;1;2m filterAndDelDirtyUser success!! \033[m";


T="User"
SQL="call moveAllRecordToBackFirstOneStep($MIN_LEVEL, $MAX_LEVEL, $MAX_DAYS)"

echo -e "\033[32;31;1;2m 开始进行角色删除,此删除为永久删除，删除参数为：最低等级-$MIN_LEVEL, 最高等级-$MAX_LEVEL, 未登录天数-$MAX_DAYS!! \033[m";
echo -e "\033[32;31;1;2m 10秒后开始执行!! \033[m";
COUNT=10
i=0
while [ $i -lt $COUNT ]
do
        printf "\r$i s remain!!"
        sleep 1
        i=$((i + 1))
done
echo "\n"
mysql -u$DB_USER -p$DB_PASSWD -h$DB_IP $DB_NAME -e "$SQL"

echo -e "\033[32;31;1;2m deleteRole for 0-1 success!! \033[m";



SQL="call moveAllRecordToBackFirstOneStep(2, 9,30)"

echo -e "\033[32;31;1;2m 开始进行角色删除,此删除为永久删除，删除参数为：最低等级-2, 最高等级-9, 未登录天数-30!! \033[m";
echo -e "\033[32;31;1;2m 10秒后开始执行!! \033[m";
COUNT=10
i=0
while [ $i -lt $COUNT ]
do
        printf "\r$i s remain!!"
        sleep 1
        i=$((i + 1))
done
echo "\n"
mysql -u$DB_USER -p$DB_PASSWD -h$DB_IP $DB_NAME -e "$SQL"

echo -e "\033[32;31;1;2m deleteRolefor 2-9 success!! \033[m";



SQL="call moveAllRecordToBackFirstTwoStep(10, 39,60)"

echo -e "\033[32;31;1;2m 开始进行角色删除，此删除为直接流亡，删除参数为：最低等级-10, 最高等级-39, 未登录天数-60!! \033[m";
echo -e "\033[32;31;1;2m 10秒后开始执行!! \033[m";
COUNT=10
i=0
while [ $i -lt $COUNT ]
do
        printf "\r$i s remain!!"
        sleep 1
        i=$((i + 1))
done
echo "\n"
mysql -u$DB_USER -p$DB_PASSWD -h$DB_IP $DB_NAME -e "$SQL"

echo -e "\033[32;31;1;2m deleteRole for 10-39 success!! \033[m";


SQL="call moveAllRecordToBackFirstTwoStep(40, 69,90)"

echo -e "\033[32;31;1;2m 开始进行角色删除，此删除为直接流亡，删除参数为：最低等级-40, 最高等级-69, 未登录天数-90!! \033[m";
echo -e "\033[32;31;1;2m 10秒后开始执行!! \033[m";
COUNT=10
i=0
while [ $i -lt $COUNT ]
do
        printf "\r$i s remain!!"
        sleep 1
        i=$((i + 1))
done
echo "\n"
mysql -u$DB_USER -p$DB_PASSWD -h$DB_IP $DB_NAME -e "$SQL"

echo -e "\033[32;31;1;2m deleteRole for 40-69 success!! \033[m";
