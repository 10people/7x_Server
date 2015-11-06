CREATE TABLE PvpRecord(
	battleId int not null primary key auto_increment comment '战斗id，主键',
	userId int not null comment '攻方id',
	defendUserId int not null comment '守方id',
	msgData Blob not null comment '战斗匹配玩家数据',
	dataLen int comment '战斗重放数据长度',
	battleData Blob comment '战斗重放数据',
	isWin int default 0 comment '战斗结果',
	atkTime Timestamp not null comment '战斗时间',
	awardData Blob comment '奖励数据'
)engine = innodb default charSet=utf8;

CREATE TABLE PvpAwardFactor(
	id int not null primary key auto_increment comment '主键',
	leftNum int not null comment '剩余单位数',
	type int not null comment '类型 1-荣誉 2-经验',
	factor float(4,2) not null comment '系数'
)engine = innodb default charSet=utf8;


CREATE TABLE PvpTrialsTemplet(
	id int(10) primary key comment '主键，列数',
	minNum int(2) not null comment '随机玩家最少数',
	maxNum int(2) not null comment '当前列最大的玩家数',
	awardId1 int(10) not null comment '当前列默认的奖励',
	awardId2 int(10) not null comment '当期列强化的奖励'
)ENGINE = INNODB DEFAULT CHARSET=UTF8;


CREATE TABLE PvpTrialsRecord(
	userId int not null comment '玩家id',
	col int not null comment '列数',
	awardId int(10) not null default '0' comment '奖励id',
	enermyIdStr varchar(100) comment '敌人id',
	primary key (userId, col)
)ENGINE = INNODB DEFAULT CHARSET=UTF8;


CREATE TABLE ZoneKingRecord(
	userId int(10) primary key comment '玩家id',
	zoneId int(2) not null comment '区域id',
	isKing int(2) not null default '0' comment '是否是霸主',
	defWinTimes int(3) not null default '0' comment '防御成功次数',
	fightTimes int(3) not null default '0' comment '挑战次数',
	beKingTime Timestamp comment '最早当上霸主的时间'
)ENGINE = INNODB DEFAULT CHARSET=UTF8; 





