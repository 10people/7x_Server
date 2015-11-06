#!/bin/bash
# start the wolf:
# runRouter.sh  start start the wolf
#               stop  stop the wolf
PRO_DIR=`dirname $0`;
PRO_DIR=`cd $PRO_DIR/..;pwd`;
TIME_SUFFIX=`date +%Y%m%d%H`;

if [ -z $TX_CONF_PATH ]; then
    echo "TX_CONF_PATH not configure!! ~";
    echo "\033[33;31;1;2;4m PLZ reboot or source /etc/profile!!! and close other ssh!!\033[m";
    exit 0;
fi
case $1 in
 start)
 	if [ `ps -ef | grep SMSRoot | grep -v grep | wc -l` -ne 0 ]; then
		echo "start SMSRoot failed!! because already exist!!"
		exit 0
	fi
	
 	cd $PRO_DIR/shell && mkdir -p com && cd com && mkdir -p youxigu && cd youxigu && mkdir -p boot && cd $PRO_DIR;
	cp $PRO_DIR/bin/com/youxigu/boot/SMSRoot.class $PRO_DIR/shell/com/youxigu/boot/SMSRoot.class;
	cp $PRO_DIR/bin/com/youxigu/boot/GameLoader.class $PRO_DIR/shell/com/youxigu/boot/GameLoader.class;
	
	cp $PRO_DIR/conf/sms/server.properties $PRO_DIR/conf/server.properties;
	cp $PRO_DIR/conf/sms/log4j.properties $PRO_DIR/conf/log4j.properties;
	
	nohup java -server -Xms4096m -Xmx4096m -classpath $PRO_DIR/shell:$PRO_DIR/conf:$PRO_DIR/lib/slf4j-api-1.4.2.jar:$PRO_DIR/lib/slf4j-log4j12-1.4.2.jar:$PRO_DIR/lib/log4j-1.2.14.jar com.youxigu.boot.SMSRoot \
	|/usr/local/sbin/cronolog $GAME_LOG_PATH/sms/wolf.out.%Y%m%d%H >>/dev/null 2>&1 &
	#echo $[$! - 1] > $PRO_DIR/sms.pid;
	jps |awk '{print $2,$1}' |grep -E '^SMSRoot' |awk '{print $2}' > $PRO_DIR/sms.pid;
	echo -e "\033[32;31;1;2m start sms success!! \033[m";
	#touch $GAME_LOG_PATH/wolf/wolf.out.$TIME_SUFFIX;
	#tail -f $GAME_LOG_PATH/wolf/wolf.out.$TIME_SUFFIX;
	;;
  
  restart)
		$PRO_DIR/shell/runSms.sh stop;
		sleep 30;
		kill -9 `jps | grep SMSRoot | cut -d' ' -f1`;
		$PRO_DIR/shell/runSms.sh start;
	;;
	
  stop)
  	kill `cat $PRO_DIR/sms.pid`;
  	echo -e "\033[32;31;1;2m stop sms success!! \033[m";
  	;;
     *)
   		echo "start: start the sms server!!";
   		echo " stop: stop the sms server!!";
    ;;	 
 esac	