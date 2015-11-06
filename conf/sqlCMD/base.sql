####系统常量表
CREATE TABLE Enumer(
	enumId varchar(48) not null primary key comment '主键',
	enumGroup varchar(48) comment '分组',
	enumValue varchar(48) not null comment '值',
	orderBy int(10) not null comment '排序',
	enumDesc varchar(48) not null comment '描述'
)ENGINE=INNODB DEFAULT CHARSET=utf8;


