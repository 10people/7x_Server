添加protobuf协议需要如下几步：
1 在protobuf添加.proto协议描述文件,所有描述文件包只能是qxmobile.protobuf；
2 在build.xml 的 protobuf的target 添加 antcall protobufhandle 命令；
3 qxmobile.protobuf 里面的内容不得修改
4 运行 build.xml 的 protobuf target： ant protobuf
