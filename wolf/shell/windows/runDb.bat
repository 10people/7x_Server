cd ..
copy ..\bin\com\youxigu\boot\Root.class com\youxigu\boot\Root.class;
copy ..\bin\com\youxigu\boot\GameLoader.class com\youxigu\boot\GameLoader.class;

copy ..\conf\synDb\server.properties ..\conf\server.properties
copy ..\conf\synDb\log4j.properties ..\conf\log4j.properties

java -classpath .;../conf;../lib/slf4j-api-1.4.2.jar;../lib/slf4j-log4j12-1.4.2.jar;../lib/log4j-1.2.14.jar; com.youxigu.boot.Root

cd windows
