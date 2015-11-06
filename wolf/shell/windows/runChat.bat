cd ..

copy ..\bin\com\youxigu\boot\ChatRoot.class com\youxigu\boot\ChatRoot.class;
copy ..\bin\com\youxigu\boot\GameLoader.class com\youxigu\boot\GameLoader.class;

copy ..\conf\server.test.properties ..\conf\server.properties
copy ..\conf\log4j.router.properties ..\conf\log4j.properties;
rem copy ..\conf\spring.router.xml ..\conf\spring.xml;

java -classpath .;../conf;../lib/slf4j-api-1.4.2.jar;../lib/slf4j-log4j12-1.4.2.jar;../lib/log4j-1.2.14.jar; com.youxigu.boot.ChatRoot
cd windows
