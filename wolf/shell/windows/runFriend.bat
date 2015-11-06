cd ..
copy ..\bin\com\youxigu\boot\FriendRoot.class com\youxigu\boot\FriendRoot.class;
copy ..\bin\com\youxigu\boot\GameLoader.class com\youxigu\boot\GameLoader.class;

copy ..\conf\friend\server.properties ..\conf\server.properties
copy ..\conf\friend\log4j.properties ..\conf\log4j.properties;
copy ..\conf\friend\spring.xml ..\conf\spring.xml;

java -classpath .;../conf;../lib/slf4j-api-1.4.2.jar;../lib/slf4j-log4j12-1.4.2.jar;../lib/log4j-1.2.14.jar; com.youxigu.boot.FriendRoot
cd windows
