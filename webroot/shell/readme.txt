activitiesConfig.properties 
标识是否起活动

catalina.sh server.xml 
是为gameServer准备的配置文件

catalina2.sh server2.xml 
是为活动服务器准备的配置文件

dynasty.xml 用来关联tomcat和游戏安装包

dynasty.sh 启动游戏的命令脚本有3个参数：

dynasty.sh configure [activity] 对游戏进行配置，如果带上了activity表示这个服务器同时也是活动服务器
dynasty.sh start 启动游戏
dynasty.sh stop 关闭游戏

install db:
		========the sql source is in dynasty/bin/sql/========!!
		step1: source dynasty.sql; 
		step2: source quartz.sql; 
		step3: source mapcell.sql; 
		step4: source dynasty.sql; 
		
进入dynasty目录
./dynasty.sh load; // 进行数据导入
./dynasty.sh configure [activity] // 进行tomcat配置
./dynasty.sh start|stop // 启动或者关闭游戏
		
		