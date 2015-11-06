CREATE TABLE User(
	userId int not null auto_increment primary key comment '玩家id',
	userName varchar(48) not null comment '角色名称',
	userLevel int not null comment '角色等级',
	cash int not null comment '元宝',
	motivation int not null comment '活力值',
	curExp int not null comment '当前经验值',
	honor int not null comment '荣誉值',
	lastLoginDttm timestamp comment '最后登录时间',
	lastLogoutDttm timestamp comment '最后一次离线时间',
	headIcon varchar(45) not null default '1' comment '玩家头像',
	accId varchar(45) not null comment '平台账号'
)ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE UserTechTemplet(
	tempId int not null comment '模板Id',
	userLevel int not null comment '君主等级',
	techId int not null comment '君主技能Id',
	techLevel int not null comment '君主技能等级',
	techMaxValue int comment '君主最多能拥有的技能个数',
	needCopper int comment '升级技能需要的铜币',
	PRIMARY KEY  (tempId) 
)ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE UserTechLevelTemplet(
	id int not null comment '君主技能Id',
	name varchar(48) not null comment '技能名',
	description varchar(100) comment '说明',
	pic varchar(48) not null comment '图片路径',
	level int not null comment '技能等级',
	affectType int(11) not null comment '效果类型',
	value1 float not null comment '效果数值1',
	value2 int(11) not null comment '效果数值2',
	targetType int(11) not null comment '作用对象',
	coolDown int(11) not null comment '冷却时间',
	duration int(11) not null COMMENT '持续回合数',
	PRIMARY KEY (id, level)
)ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE UserLevelTemplet(
	tempId int not null comment '模板Id',
	userLevel int not null comment '君主等级',
	maxExp int not null comment '等级最大经验值',
	PRIMARY KEY  (tempId) 
)ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE Account(
	accId varchar(45) not null primary key comment '平台账号',
	accName varchar(45) not null unique comment '玩家名称'
)ENGINE = INNODB DEFAULT CHARSET=UTF8;