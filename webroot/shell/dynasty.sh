#!/bin/bash
#
#start as game server 
# the shell will write the default server.xml catalina.sh to the tomcat.
#
#
#

if [ -z $GAME_HOME ]; then
   echo -e '\033[33;31;5;1;2;4m the GAME_HOME not config \033[m';
   exit 0;
fi

WORKING_DIR=$GAME_HOME;
WORKING_CONF_DIR=$WORKING_DIR/conf;
WORKING_BIN_DIR=$WORKING_DIR/bin;
WORKING_CONTEXT_DIR=$WORKING_DIR/conf/Catalina/localhost;

echo -e "GAME_HOME: \033[33;31;1;2;4m $GAME_HOME \033[m";

		
PRO_DIR=`dirname "$0"`;
PRO_DIR=`cd "$PRO_DIR/.." ; pwd`;

echo -e "project dir: \033[33;31;1;2;4m $PRO_DIR \033[m";

if [ -z $TX_CONF_PATH ]; then
     echo "\033[33;31;1;2;4m TX_CONF_PATH not configure!! the file will be place ~\033[m";
     echo "\033[33;31;1;2;4m PLZ reboot or source /etc/profile!!! and close other ssh!!\033[m";
     exit 0;
fi

if [ -z $TX_CONF_PATH ]; then
    echo "TX_CONF_PATH not configure!! the file will be place ~";
fi
		
case $1 in
	configure)
		#cp -f $PRO_DIR/shell/log4j.tomcat.properties $WORKING_DIR/lib/log4j.properties;
		#cp -f $PRO_DIR/shell/tomcatlib/tomcat-juli-adapters.jar $WORKING_DIR/lib;
		#cp -f $PRO_DIR/WEB-INF/lib/log4j-1.2.14.jar $WORKING_DIR/lib;
		#cp -f $PRO_DIR/shell/tomcatlib/tomcat-juli.jar $WORKING_DIR/bin/;
		
		echo -e "\033[33;31;1;2;4m start configure the tomcat: will override the conf/server.xml; \033[m";
		echo -e "\033[33;31;1;2;4m start configure the tomcat: will override the bin/catalina.sh; \033[m";
		
		cp -f $PRO_DIR/shell/tomcatlib/index.htm $WORKING_DIR;
		cp -f $PRO_DIR/shell/server.xml $WORKING_CONF_DIR;
		cp -f $PRO_DIR/shell/context.xml $WORKING_CONF_DIR;
		cp -f $PRO_DIR/shell/catalina.sh $WORKING_BIN_DIR;
		
		cp -f $PRO_DIR/shell/activitiesConfig.properties $WORKING_BIN_DIR;
		if [[ -n $2 && $2 == "activity" ]]; then
			echo "startActivities=true" > $WORKING_BIN_DIR/activitiesConfig.properties;
			echo "configure game as activity server!!";
		else
			echo "startActivities=false" > $WORKING_BIN_DIR/activitiesConfig.properties;
			echo "configure game as game server!!";
		fi
		
		cd $WORKING_CONF_DIR;
		mkdir -p Catalina && cd Catalina && mkdir -p localhost;
		
		cp -f $PRO_DIR/shell/dynasty.xml $WORKING_CONTEXT_DIR;
		sed -i -e s#HOME#$PRO_DIR#g $WORKING_CONTEXT_DIR/dynasty.xml;
		
		#conf the tomcat listen ip
		internalIp=`cat $TX_CONF_PATH/conf.properties | grep internalIp | cut -d= -f2`;
		sed -i -e s#internalIp#$internalIp#g $WORKING_CONF_DIR/server.xml;
		
		echo -e "\033[33;31;5;1;2;4m configure success!! \033[m";
	;;
	
	start)
		#conf the tomcat listen ip
		rm $TX_CONF_PATH/NOMONITOR;
		
		#copy the dbspring to the dynasty
		isAct=`cat $GAME_HOME/bin/activitiesConfig.properties | grep startActivities | cut -d= -f2`;
        if [ $isAct == 'true' ]; then
           cp $PRO_DIR/shell/applicationContext_activity.xml $PRO_DIR/WEB-INF/classes/applicationContext_activity.xml
        else
           cp $PRO_DIR/shell/applicationContext_empty.xml $PRO_DIR/WEB-INF/classes/applicationContext_activity.xml
        fi
		
		internalIp=`cat $TX_CONF_PATH/conf.properties | grep internalIp | cut -d= -f2`;
		sed -i -e s#internalIp#$internalIp#g $WORKING_CONF_DIR/server.xml;
		export LD_PRELOAD_64=$JAVA_HOME/jre/lib/amd64/libjsig.so;
		rm -rf $WORKING_DIR/lib/log4j.properties;
		cp -rf $PRO_DIR/bin/sql/db.properties $PRO_DIR/WEB-INF/classes/spring.properties;
		cp -f $PRO_DIR/shell/catalina.sh $WORKING_BIN_DIR;
		echo -e "\033[33;31;5;1;2;4m start the tomcat!!! \033[m";
		rm -rf $WORKING_DIR/work/Catalina/localhost/dynasty;
		cd $WORKING_BIN_DIR;
		./startup.sh 1>/dev/null;
		
		#TIME_SUFFIX=`date +%Y%m%d%H`;
		#touch $GAME_LOG_PATH/tomcat/catalina.out.$TIME_SUFFIX;
		#tail -f $GAME_LOG_PATH/tomcat/catalina.out.$TIME_SUFFIX;
	;;
	
	stop)
		touch $TX_CONF_PATH/NOMONITOR;
		$WORKING_BIN_DIR/shutdown.sh 1>/dev/null;
		echo -e "\033[33;31;5;1;2;4m stop the tomcat!!! \033[m";
		sleep 30;
		kill -9 `jps | grep Bootstrap | cut -d' ' -f1`;
	;;
	
	restart)
		$PRO_DIR/shell/dynasty.sh stop;
		sleep 30;
		kill -9 `jps | grep Bootstrap | cut -d' ' -f1`;
		$PRO_DIR/shell/dynasty.sh start;
	;;
	
	load)
		cd $PRO_DIR && ant load;
	;;
	
	install)
		echo "plz create the db on the db machine!! this will be safe!!";
		echo "========the sql source is in dynasty/bin/sql/========!!";
		echo "step1: source dynasty.sql; !!";
		echo "step2: source quartz.sql; !!";
		echo "step3: source mapcell.sql; !!";
		echo "step4: source dynasty.sql; !!";
	;;
	
	update)
		echo "will update the game!!";
	;;
	
	*)
		echo -e "\033[33;31;5;1;2;4m configure\033[m : configure the game";	
		echo -e "    \033[33;31;5;1;2;4m start\033[m : start the game";	
		echo -e "     \033[33;31;5;1;2;4m stop\033[m : configure the game";	
	;;		

# end the case
esac


