CREATE TABLE MailContent_0(
 mailId int primary key auto_increment comment '主键',
 userId int not null comment '玩家id',
 sendUserId int not null comment '发送方id',
 receiveUserId int not null comment '接收方id',
 sendDttm Timestamp not null default '0000-00-00 00:00:00' comment '发送日期、时间',
 readDttm Timestamp not null default '0000-00-00 00:00:00' comment '读取日期、时间',
 content text not null comment '消息内容',
 title varchar(40) not null comment '消息标题',
 mailReceiveType int not null comment '消息状态：接收方 0：删除的消息 1：读了的消息 2：未读的消息',
 mailType int not null comment '消息种类： 1-系统邮件 2-玩家邮件 3-发送邮件',
 appendixFlag int not null default 0 comment '是否含附件: 0:否 1:是 2:已领取',
 entityIds varchar(30) comment '附件道具id1001；2201；3001',
 itemNums varchar(30) comment '附件数量1；2；3'
)ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE MailContent_1(
 mailId int primary key auto_increment comment '主键',
 userId int not null comment '玩家id',
 sendUserId int not null comment '发送方id',
 receiveUserId int not null comment '接收方id',
 sendDttm Timestamp  not null default '0000-00-00 00:00:00' comment '发送日期、时间',
  readDttm Timestamp not null default '0000-00-00 00:00:00' comment '读取日期、时间',
 content text not null comment '消息内容',
 title varchar(40) not null comment '消息标题',
 mailReceiveType int not null comment '消息状态：接收方 0：删除的消息 1：读了的消息 2：未读的消息',
 mailType int not null comment '消息种类： 1-系统邮件 2-玩家邮件 3-发送邮件',
 appendixFlag int not null default 0 comment '是否含附件: 0:否 1:是 2:已领取',
 entityIds varchar(30) comment '附件道具id1001；2201；3001',
 itemNums varchar(30) comment '附件数量1；2；3'
)ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE MailContent_2(
 mailId int primary key auto_increment comment '主键',
 userId int not null comment '玩家id',
 sendUserId int not null comment '发送方id',
 receiveUserId int not null comment '接收方id',
 sendDttm Timestamp  not null default '0000-00-00 00:00:00' comment '发送日期、时间',
  readDttm Timestamp not null default '0000-00-00 00:00:00' comment '读取日期、时间',
 content text not null comment '消息内容',
 title varchar(40) not null comment '消息标题',
 mailReceiveType int not null comment '消息状态：接收方 0：删除的消息 1：读了的消息 2：未读的消息',
 mailType int not null comment '消息种类： 1-系统邮件 2-玩家邮件 3-发送邮件',
 appendixFlag int not null default 0 comment '是否含附件: 0:否 1:是 2:已领取',
 entityIds varchar(30) comment '附件道具id1001；2201；3001',
 itemNums varchar(30) comment '附件数量1；2；3'
)ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE MailContent_3(
 mailId int primary key auto_increment comment '主键',
 userId int not null comment '玩家id',
 sendUserId int not null comment '发送方id',
 receiveUserId int not null comment '接收方id',
 sendDttm Timestamp  not null default '0000-00-00 00:00:00' comment '发送日期、时间',
  readDttm Timestamp not null default '0000-00-00 00:00:00' comment '读取日期、时间',
 content text not null comment '消息内容',
 title varchar(40) not null comment '消息标题',
 mailReceiveType int not null comment '消息状态：接收方 0：删除的消息 1：读了的消息 2：未读的消息',
 mailType int not null comment '消息种类： 1-系统邮件 2-玩家邮件 3-发送邮件',
 appendixFlag int not null default 0 comment '是否含附件: 0:否 1:是 2:已领取',
 entityIds varchar(30) comment '附件道具id1001；2201；3001',
 itemNums varchar(30) comment '附件数量1；2；3'
)ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE MailContent_4(
 mailId int primary key auto_increment comment '主键',
 userId int not null comment '玩家id',
 sendUserId int not null comment '发送方id',
 receiveUserId int not null comment '接收方id',
 sendDttm Timestamp  not null default '0000-00-00 00:00:00' comment '发送日期、时间',
  readDttm Timestamp not null default '0000-00-00 00:00:00' comment '读取日期、时间',
 content text not null comment '消息内容',
 title varchar(40) not null comment '消息标题',
 mailReceiveType int not null comment '消息状态：接收方 0：删除的消息 1：读了的消息 2：未读的消息',
 mailType int not null comment '消息种类： 1-系统邮件 2-玩家邮件 3-发送邮件',
 appendixFlag int not null default 0 comment '是否含附件: 0:否 1:是 2:已领取',
 entityIds varchar(30) comment '附件道具id1001；2201；3001',
 itemNums varchar(30) comment '附件数量1；2；3'
)ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE MailContent_5(
 mailId int primary key auto_increment comment '主键',
 userId int not null comment '玩家id',
 sendUserId int not null comment '发送方id',
 receiveUserId int not null comment '接收方id',
 sendDttm Timestamp  not null default '0000-00-00 00:00:00' comment '发送日期、时间',
  readDttm Timestamp not null default '0000-00-00 00:00:00' comment '读取日期、时间',
 content text not null comment '消息内容',
 title varchar(40) not null comment '消息标题',
 mailReceiveType int not null comment '消息状态：接收方 0：删除的消息 1：读了的消息 2：未读的消息',
 mailType int not null comment '消息种类： 1-系统邮件 2-玩家邮件 3-发送邮件',
 appendixFlag int not null default 0 comment '是否含附件: 0:否 1:是 2:已领取',
 entityIds varchar(30) comment '附件道具id1001；2201；3001',
 itemNums varchar(30) comment '附件数量1；2；3'
)ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE MailContent_6(
 mailId int primary key auto_increment comment '主键',
 userId int not null comment '玩家id',
 sendUserId int not null comment '发送方id',
 receiveUserId int not null comment '接收方id',
 sendDttm Timestamp  not null default '0000-00-00 00:00:00' comment '发送日期、时间',
  readDttm Timestamp not null default '0000-00-00 00:00:00' comment '读取日期、时间',
 content text not null comment '消息内容',
 title varchar(40) not null comment '消息标题',
 mailReceiveType int not null comment '消息状态：接收方 0：删除的消息 1：读了的消息 2：未读的消息',
 mailType int not null comment '消息种类： 1-系统邮件 2-玩家邮件 3-发送邮件',
 appendixFlag int not null default 0 comment '是否含附件: 0:否 1:是 2:已领取',
 entityIds varchar(30) comment '附件道具id1001；2201；3001',
 itemNums varchar(30) comment '附件数量1；2；3'
)ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE MailContent_7(
 mailId int primary key auto_increment comment '主键',
 userId int not null comment '玩家id',
 sendUserId int not null comment '发送方id',
 receiveUserId int not null comment '接收方id',
 sendDttm Timestamp  not null default '0000-00-00 00:00:00' comment '发送日期、时间',
  readDttm Timestamp not null default '0000-00-00 00:00:00' comment '读取日期、时间',
 content text not null comment '消息内容',
 title varchar(40) not null comment '消息标题',
 mailReceiveType int not null comment '消息状态：接收方 0：删除的消息 1：读了的消息 2：未读的消息',
 mailType int not null comment '消息种类： 1-系统邮件 2-玩家邮件 3-发送邮件',
 appendixFlag int not null default 0 comment '是否含附件: 0:否 1:是 2:已领取',
 entityIds varchar(30) comment '附件道具id1001；2201；3001',
 itemNums varchar(30) comment '附件数量1；2；3'
)ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE MailContent_8(
 mailId int primary key auto_increment comment '主键',
 userId int not null comment '玩家id',
 sendUserId int not null comment '发送方id',
 receiveUserId int not null comment '接收方id',
 sendDttm Timestamp  not null default '0000-00-00 00:00:00' comment '发送日期、时间',
  readDttm Timestamp not null default '0000-00-00 00:00:00' comment '读取日期、时间',
 content text not null comment '消息内容',
 title varchar(40) not null comment '消息标题',
 mailReceiveType int not null comment '消息状态：接收方 0：删除的消息 1：读了的消息 2：未读的消息',
 mailType int not null comment '消息种类： 1-系统邮件 2-玩家邮件 3-发送邮件',
 appendixFlag int not null default 0 comment '是否含附件: 0:否 1:是 2:已领取',
 entityIds varchar(30) comment '附件道具id1001；2201；3001',
 itemNums varchar(30) comment '附件数量1；2；3'
)ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE MailContent_9(
 mailId int primary key auto_increment comment '主键',
 userId int not null comment '玩家id',
 sendUserId int not null comment '发送方id',
 receiveUserId int not null comment '接收方id',
 sendDttm Timestamp  not null default '0000-00-00 00:00:00' comment '发送日期、时间',
  readDttm Timestamp not null default '0000-00-00 00:00:00' comment '读取日期、时间',
 content text not null comment '消息内容',
 title varchar(40) not null comment '消息标题',
 mailReceiveType int not null comment '消息状态：接收方 0：删除的消息 1：读了的消息 2：未读的消息',
 mailType int not null comment '消息种类： 1-系统邮件 2-玩家邮件 3-发送邮件',
 appendixFlag int not null default 0 comment '是否含附件: 0:否 1:是 2:已领取',
 entityIds varchar(30) comment '附件道具id1001；2201；3001',
 itemNums varchar(30) comment '附件数量1；2；3'
)ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE MailContent_10(
 mailId int primary key auto_increment comment '主键',
 userId int not null comment '玩家id',
 sendUserId int not null comment '发送方id',
 receiveUserId int not null comment '接收方id',
 sendDttm Timestamp  not null default '0000-00-00 00:00:00' comment '发送日期、时间',
  readDttm Timestamp not null default '0000-00-00 00:00:00' comment '读取日期、时间',
 content text not null comment '消息内容',
 title varchar(40) not null comment '消息标题',
 mailReceiveType int not null comment '消息状态：接收方 0：删除的消息 1：读了的消息 2：未读的消息',
 mailType int not null comment '消息种类： 1-系统邮件 2-玩家邮件 3-发送邮件',
 appendixFlag int not null default 0 comment '是否含附件: 0:否 1:是 2:已领取',
 entityIds varchar(30) comment '附件道具id1001；2201；3001',
 itemNums varchar(30) comment '附件数量1；2；3'
)ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE MailContent_11(
 mailId int primary key auto_increment comment '主键',
 userId int not null comment '玩家id',
 sendUserId int not null comment '发送方id',
 receiveUserId int not null comment '接收方id',
 sendDttm Timestamp  not null default '0000-00-00 00:00:00' comment '发送日期、时间',
  readDttm Timestamp not null default '0000-00-00 00:00:00' comment '读取日期、时间',
 content text not null comment '消息内容',
 title varchar(40) not null comment '消息标题',
 mailReceiveType int not null comment '消息状态：接收方 0：删除的消息 1：读了的消息 2：未读的消息',
 mailType int not null comment '消息种类： 1-系统邮件 2-玩家邮件 3-发送邮件',
 appendixFlag int not null default 0 comment '是否含附件: 0:否 1:是 2:已领取',
 entityIds varchar(30) comment '附件道具id1001；2201；3001',
 itemNums varchar(30) comment '附件数量1；2；3'
)ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE MailContent_12(
 mailId int primary key auto_increment comment '主键',
 userId int not null comment '玩家id',
 sendUserId int not null comment '发送方id',
 receiveUserId int not null comment '接收方id',
 sendDttm Timestamp  not null default '0000-00-00 00:00:00' comment '发送日期、时间',
  readDttm Timestamp not null default '0000-00-00 00:00:00' comment '读取日期、时间',
 content text not null comment '消息内容',
 title varchar(40) not null comment '消息标题',
 mailReceiveType int not null comment '消息状态：接收方 0：删除的消息 1：读了的消息 2：未读的消息',
 mailType int not null comment '消息种类： 1-系统邮件 2-玩家邮件 3-发送邮件',
 appendixFlag int not null default 0 comment '是否含附件: 0:否 1:是 2:已领取',
 entityIds varchar(30) comment '附件道具id1001；2201；3001',
 itemNums varchar(30) comment '附件数量1；2；3'
)ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE MailContent_13(
 mailId int primary key auto_increment comment '主键',
 userId int not null comment '玩家id',
 sendUserId int not null comment '发送方id',
 receiveUserId int not null comment '接收方id',
 sendDttm Timestamp  not null default '0000-00-00 00:00:00' comment '发送日期、时间',
  readDttm Timestamp not null default '0000-00-00 00:00:00' comment '读取日期、时间',
 content text not null comment '消息内容',
 title varchar(40) not null comment '消息标题',
 mailReceiveType int not null comment '消息状态：接收方 0：删除的消息 1：读了的消息 2：未读的消息',
 mailType int not null comment '消息种类： 1-系统邮件 2-玩家邮件 3-发送邮件',
 appendixFlag int not null default 0 comment '是否含附件: 0:否 1:是 2:已领取',
 entityIds varchar(30) comment '附件道具id1001；2201；3001',
 itemNums varchar(30) comment '附件数量1；2；3'
)ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE MailContent_14(
 mailId int primary key auto_increment comment '主键',
 userId int not null comment '玩家id',
 sendUserId int not null comment '发送方id',
 receiveUserId int not null comment '接收方id',
 sendDttm Timestamp  not null default '0000-00-00 00:00:00' comment '发送日期、时间',
  readDttm Timestamp not null default '0000-00-00 00:00:00' comment '读取日期、时间',
 content text not null comment '消息内容',
 title varchar(40) not null comment '消息标题',
 mailReceiveType int not null comment '消息状态：接收方 0：删除的消息 1：读了的消息 2：未读的消息',
 mailType int not null comment '消息种类： 1-系统邮件 2-玩家邮件 3-发送邮件',
 appendixFlag int not null default 0 comment '是否含附件: 0:否 1:是 2:已领取',
 entityIds varchar(30) comment '附件道具id1001；2201；3001',
 itemNums varchar(30) comment '附件数量1；2；3'
)ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE MailContent_15(
 mailId int primary key auto_increment comment '主键',
 userId int not null comment '玩家id',
 sendUserId int not null comment '发送方id',
 receiveUserId int not null comment '接收方id',
 sendDttm Timestamp  not null default '0000-00-00 00:00:00' comment '发送日期、时间',
  readDttm Timestamp not null default '0000-00-00 00:00:00' comment '读取日期、时间',
 content text not null comment '消息内容',
 title varchar(40) not null comment '消息标题',
 mailReceiveType int not null comment '消息状态：接收方 0：删除的消息 1：读了的消息 2：未读的消息',
 mailType int not null comment '消息种类： 1-系统邮件 2-玩家邮件 3-发送邮件',
 appendixFlag int not null default 0 comment '是否含附件: 0:否 1:是 2:已领取',
 entityIds varchar(30) comment '附件道具id1001；2201；3001',
 itemNums varchar(30) comment '附件数量1；2；3'
)ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE MailContent_16(
 mailId int primary key auto_increment comment '主键',
 userId int not null comment '玩家id',
 sendUserId int not null comment '发送方id',
 receiveUserId int not null comment '接收方id',
 sendDttm Timestamp  not null default '0000-00-00 00:00:00' comment '发送日期、时间',
  readDttm Timestamp not null default '0000-00-00 00:00:00' comment '读取日期、时间',
 content text not null comment '消息内容',
 title varchar(40) not null comment '消息标题',
 mailReceiveType int not null comment '消息状态：接收方 0：删除的消息 1：读了的消息 2：未读的消息',
 mailType int not null comment '消息种类： 1-系统邮件 2-玩家邮件 3-发送邮件',
 appendixFlag int not null default 0 comment '是否含附件: 0:否 1:是 2:已领取',
 entityIds varchar(30) comment '附件道具id1001；2201；3001',
 itemNums varchar(30) comment '附件数量1；2；3'
)ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE MailContent_17(
 mailId int primary key auto_increment comment '主键',
 userId int not null comment '玩家id',
 sendUserId int not null comment '发送方id',
 receiveUserId int not null comment '接收方id',
 sendDttm Timestamp  not null default '0000-00-00 00:00:00' comment '发送日期、时间',
  readDttm Timestamp not null default '0000-00-00 00:00:00' comment '读取日期、时间',
 content text not null comment '消息内容',
 title varchar(40) not null comment '消息标题',
 mailReceiveType int not null comment '消息状态：接收方 0：删除的消息 1：读了的消息 2：未读的消息',
 mailType int not null comment '消息种类： 1-系统邮件 2-玩家邮件 3-发送邮件',
 appendixFlag int not null default 0 comment '是否含附件: 0:否 1:是 2:已领取',
 entityIds varchar(30) comment '附件道具id1001；2201；3001',
 itemNums varchar(30) comment '附件数量1；2；3'
)ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE MailContent_18(
 mailId int primary key auto_increment comment '主键',
 userId int not null comment '玩家id',
 sendUserId int not null comment '发送方id',
 receiveUserId int not null comment '接收方id',
 sendDttm Timestamp  not null default '0000-00-00 00:00:00' comment '发送日期、时间',
  readDttm Timestamp not null default '0000-00-00 00:00:00' comment '读取日期、时间',
 content text not null comment '消息内容',
 title varchar(40) not null comment '消息标题',
 mailReceiveType int not null comment '消息状态：接收方 0：删除的消息 1：读了的消息 2：未读的消息',
 mailType int not null comment '消息种类： 1-系统邮件 2-玩家邮件 3-发送邮件',
 appendixFlag int not null default 0 comment '是否含附件: 0:否 1:是 2:已领取',
 entityIds varchar(30) comment '附件道具id1001；2201；3001',
 itemNums varchar(30) comment '附件数量1；2；3'
)ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE MailContent_19(
 mailId int primary key auto_increment comment '主键',
 userId int not null comment '玩家id',
 sendUserId int not null comment '发送方id',
 receiveUserId int not null comment '接收方id',
 sendDttm Timestamp  not null default '0000-00-00 00:00:00' comment '发送日期、时间',
  readDttm Timestamp not null default '0000-00-00 00:00:00' comment '读取日期、时间',
 content text not null comment '消息内容',
 title varchar(40) not null comment '消息标题',
 mailReceiveType int not null comment '消息状态：接收方 0：删除的消息 1：读了的消息 2：未读的消息',
 mailType int not null comment '消息种类： 1-系统邮件 2-玩家邮件 3-发送邮件',
 appendixFlag int not null default 0 comment '是否含附件: 0:否 1:是 2:已领取',
 entityIds varchar(30) comment '附件道具id1001；2201；3001',
 itemNums varchar(30) comment '附件数量1；2；3'
)ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE MailContent_20(
 mailId int primary key auto_increment comment '主键',
 userId int not null comment '玩家id',
 sendUserId int not null comment '发送方id',
 receiveUserId int not null comment '接收方id',
 sendDttm Timestamp  not null default '0000-00-00 00:00:00' comment '发送日期、时间',
  readDttm Timestamp not null default '0000-00-00 00:00:00' comment '读取日期、时间',
 content text not null comment '消息内容',
 title varchar(40) not null comment '消息标题',
 mailReceiveType int not null comment '消息状态：接收方 0：删除的消息 1：读了的消息 2：未读的消息',
 mailType int not null comment '消息种类： 1-系统邮件 2-玩家邮件 3-发送邮件',
 appendixFlag int not null default 0 comment '是否含附件: 0:否 1:是 2:已领取',
 entityIds varchar(30) comment '附件道具id1001；2201；3001',
 itemNums varchar(30) comment '附件数量1；2；3'
)ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE MailContent_21(
 mailId int primary key auto_increment comment '主键',
 userId int not null comment '玩家id',
 sendUserId int not null comment '发送方id',
 receiveUserId int not null comment '接收方id',
 sendDttm Timestamp  not null default '0000-00-00 00:00:00' comment '发送日期、时间',
  readDttm Timestamp not null default '0000-00-00 00:00:00' comment '读取日期、时间',
 content text not null comment '消息内容',
 title varchar(40) not null comment '消息标题',
 mailReceiveType int not null comment '消息状态：接收方 0：删除的消息 1：读了的消息 2：未读的消息',
 mailType int not null comment '消息种类： 1-系统邮件 2-玩家邮件 3-发送邮件',
 appendixFlag int not null default 0 comment '是否含附件: 0:否 1:是 2:已领取',
 entityIds varchar(30) comment '附件道具id1001；2201；3001',
 itemNums varchar(30) comment '附件数量1；2；3'
)ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE MailContent_22(
 mailId int primary key auto_increment comment '主键',
 userId int not null comment '玩家id',
 sendUserId int not null comment '发送方id',
 receiveUserId int not null comment '接收方id',
 sendDttm Timestamp  not null default '0000-00-00 00:00:00' comment '发送日期、时间',
  readDttm Timestamp not null default '0000-00-00 00:00:00' comment '读取日期、时间',
 content text not null comment '消息内容',
 title varchar(40) not null comment '消息标题',
 mailReceiveType int not null comment '消息状态：接收方 0：删除的消息 1：读了的消息 2：未读的消息',
 mailType int not null comment '消息种类： 1-系统邮件 2-玩家邮件 3-发送邮件',
 appendixFlag int not null default 0 comment '是否含附件: 0:否 1:是 2:已领取',
 entityIds varchar(30) comment '附件道具id1001；2201；3001',
 itemNums varchar(30) comment '附件数量1；2；3'
)ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE MailContent_23(
 mailId int primary key auto_increment comment '主键',
 userId int not null comment '玩家id',
 sendUserId int not null comment '发送方id',
 receiveUserId int not null comment '接收方id',
 sendDttm Timestamp  not null default '0000-00-00 00:00:00' comment '发送日期、时间',
  readDttm Timestamp not null default '0000-00-00 00:00:00' comment '读取日期、时间',
 content text not null comment '消息内容',
 title varchar(40) not null comment '消息标题',
 mailReceiveType int not null comment '消息状态：接收方 0：删除的消息 1：读了的消息 2：未读的消息',
 mailType int not null comment '消息种类： 1-系统邮件 2-玩家邮件 3-发送邮件',
 appendixFlag int not null default 0 comment '是否含附件: 0:否 1:是 2:已领取',
 entityIds varchar(30) comment '附件道具id1001；2201；3001',
 itemNums varchar(30) comment '附件数量1；2；3'
)ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE MailContent_24(
 mailId int primary key auto_increment comment '主键',
 userId int not null comment '玩家id',
 sendUserId int not null comment '发送方id',
 receiveUserId int not null comment '接收方id',
 sendDttm Timestamp  not null default '0000-00-00 00:00:00' comment '发送日期、时间',
  readDttm Timestamp not null default '0000-00-00 00:00:00' comment '读取日期、时间',
 content text not null comment '消息内容',
 title varchar(40) not null comment '消息标题',
 mailReceiveType int not null comment '消息状态：接收方 0：删除的消息 1：读了的消息 2：未读的消息',
 mailType int not null comment '消息种类： 1-系统邮件 2-玩家邮件 3-发送邮件',
 appendixFlag int not null default 0 comment '是否含附件: 0:否 1:是 2:已领取',
 entityIds varchar(30) comment '附件道具id1001；2201；3001',
 itemNums varchar(30) comment '附件数量1；2；3'
)ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE MailContent_25(
 mailId int primary key auto_increment comment '主键',
 userId int not null comment '玩家id',
 sendUserId int not null comment '发送方id',
 receiveUserId int not null comment '接收方id',
 sendDttm Timestamp  not null default '0000-00-00 00:00:00' comment '发送日期、时间',
  readDttm Timestamp not null default '0000-00-00 00:00:00' comment '读取日期、时间',
 content text not null comment '消息内容',
 title varchar(40) not null comment '消息标题',
 mailReceiveType int not null comment '消息状态：接收方 0：删除的消息 1：读了的消息 2：未读的消息',
 mailType int not null comment '消息种类： 1-系统邮件 2-玩家邮件 3-发送邮件',
 appendixFlag int not null default 0 comment '是否含附件: 0:否 1:是 2:已领取',
 entityIds varchar(30) comment '附件道具id1001；2201；3001',
 itemNums varchar(30) comment '附件数量1；2；3'
)ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE MailContent_26(
 mailId int primary key auto_increment comment '主键',
 userId int not null comment '玩家id',
 sendUserId int not null comment '发送方id',
 receiveUserId int not null comment '接收方id',
 sendDttm Timestamp  not null default '0000-00-00 00:00:00' comment '发送日期、时间',
  readDttm Timestamp not null default '0000-00-00 00:00:00' comment '读取日期、时间',
 content text not null comment '消息内容',
 title varchar(40) not null comment '消息标题',
 mailReceiveType int not null comment '消息状态：接收方 0：删除的消息 1：读了的消息 2：未读的消息',
 mailType int not null comment '消息种类： 1-系统邮件 2-玩家邮件 3-发送邮件',
 appendixFlag int not null default 0 comment '是否含附件: 0:否 1:是 2:已领取',
 entityIds varchar(30) comment '附件道具id1001；2201；3001',
 itemNums varchar(30) comment '附件数量1；2；3'
)ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE MailContent_27(
 mailId int primary key auto_increment comment '主键',
 userId int not null comment '玩家id',
 sendUserId int not null comment '发送方id',
 receiveUserId int not null comment '接收方id',
 sendDttm Timestamp  not null default '0000-00-00 00:00:00' comment '发送日期、时间',
  readDttm Timestamp not null default '0000-00-00 00:00:00' comment '读取日期、时间',
 content text not null comment '消息内容',
 title varchar(40) not null comment '消息标题',
 mailReceiveType int not null comment '消息状态：接收方 0：删除的消息 1：读了的消息 2：未读的消息',
 mailType int not null comment '消息种类： 1-系统邮件 2-玩家邮件 3-发送邮件',
 appendixFlag int not null default 0 comment '是否含附件: 0:否 1:是 2:已领取',
 entityIds varchar(30) comment '附件道具id1001；2201；3001',
 itemNums varchar(30) comment '附件数量1；2；3'
)ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE MailContent_28(
 mailId int primary key auto_increment comment '主键',
 userId int not null comment '玩家id',
 sendUserId int not null comment '发送方id',
 receiveUserId int not null comment '接收方id',
 sendDttm Timestamp  not null default '0000-00-00 00:00:00' comment '发送日期、时间',
  readDttm Timestamp not null default '0000-00-00 00:00:00' comment '读取日期、时间',
 content text not null comment '消息内容',
 title varchar(40) not null comment '消息标题',
 mailReceiveType int not null comment '消息状态：接收方 0：删除的消息 1：读了的消息 2：未读的消息',
 mailType int not null comment '消息种类： 1-系统邮件 2-玩家邮件 3-发送邮件',
 appendixFlag int not null default 0 comment '是否含附件: 0:否 1:是 2:已领取',
 entityIds varchar(30) comment '附件道具id1001；2201；3001',
 itemNums varchar(30) comment '附件数量1；2；3'
)ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE MailContent_29(
 mailId int primary key auto_increment comment '主键',
 userId int not null comment '玩家id',
 sendUserId int not null comment '发送方id',
 receiveUserId int not null comment '接收方id',
 sendDttm Timestamp  not null default '0000-00-00 00:00:00' comment '发送日期、时间',
  readDttm Timestamp not null default '0000-00-00 00:00:00' comment '读取日期、时间',
 content text not null comment '消息内容',
 title varchar(40) not null comment '消息标题',
 mailReceiveType int not null comment '消息状态：接收方 0：删除的消息 1：读了的消息 2：未读的消息',
 mailType int not null comment '消息种类： 1-系统邮件 2-玩家邮件 3-发送邮件',
 appendixFlag int not null default 0 comment '是否含附件: 0:否 1:是 2:已领取',
 entityIds varchar(30) comment '附件道具id1001；2201；3001',
 itemNums varchar(30) comment '附件数量1；2；3'
)ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE MailContent_30(
 mailId int primary key auto_increment comment '主键',
 userId int not null comment '玩家id',
 sendUserId int not null comment '发送方id',
 receiveUserId int not null comment '接收方id',
 sendDttm Timestamp  not null default '0000-00-00 00:00:00' comment '发送日期、时间',
  readDttm Timestamp not null default '0000-00-00 00:00:00' comment '读取日期、时间',
 content text not null comment '消息内容',
 title varchar(40) not null comment '消息标题',
 mailReceiveType int not null comment '消息状态：接收方 0：删除的消息 1：读了的消息 2：未读的消息',
 mailType int not null comment '消息种类： 1-系统邮件 2-玩家邮件 3-发送邮件',
 appendixFlag int not null default 0 comment '是否含附件: 0:否 1:是 2:已领取',
 entityIds varchar(30) comment '附件道具id1001；2201；3001',
 itemNums varchar(30) comment '附件数量1；2；3'
)ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE MailContent_31(
 mailId int primary key auto_increment comment '主键',
 userId int not null comment '玩家id',
 sendUserId int not null comment '发送方id',
 receiveUserId int not null comment '接收方id',
 sendDttm Timestamp  not null default '0000-00-00 00:00:00' comment '发送日期、时间',
  readDttm Timestamp not null default '0000-00-00 00:00:00' comment '读取日期、时间',
 content text not null comment '消息内容',
 title varchar(40) not null comment '消息标题',
 mailReceiveType int not null comment '消息状态：接收方 0：删除的消息 1：读了的消息 2：未读的消息',
 mailType int not null comment '消息种类： 1-系统邮件 2-玩家邮件 3-发送邮件',
 appendixFlag int not null default 0 comment '是否含附件: 0:否 1:是 2:已领取',
 entityIds varchar(30) comment '附件道具id1001；2201；3001',
 itemNums varchar(30) comment '附件数量1；2；3'
)ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE MailContent_32(
 mailId int primary key auto_increment comment '主键',
 userId int not null comment '玩家id',
 sendUserId int not null comment '发送方id',
 receiveUserId int not null comment '接收方id',
 sendDttm Timestamp  not null default '0000-00-00 00:00:00' comment '发送日期、时间',
  readDttm Timestamp not null default '0000-00-00 00:00:00' comment '读取日期、时间',
 content text not null comment '消息内容',
 title varchar(40) not null comment '消息标题',
 mailReceiveType int not null comment '消息状态：接收方 0：删除的消息 1：读了的消息 2：未读的消息',
 mailType int not null comment '消息种类： 1-系统邮件 2-玩家邮件 3-发送邮件',
 appendixFlag int not null default 0 comment '是否含附件: 0:否 1:是 2:已领取',
 entityIds varchar(30) comment '附件道具id1001；2201；3001',
 itemNums varchar(30) comment '附件数量1；2；3'
)ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE MailContent_33(
 mailId int primary key auto_increment comment '主键',
 userId int not null comment '玩家id',
 sendUserId int not null comment '发送方id',
 receiveUserId int not null comment '接收方id',
 sendDttm Timestamp  not null default '0000-00-00 00:00:00' comment '发送日期、时间',
  readDttm Timestamp not null default '0000-00-00 00:00:00' comment '读取日期、时间',
 content text not null comment '消息内容',
 title varchar(40) not null comment '消息标题',
 mailReceiveType int not null comment '消息状态：接收方 0：删除的消息 1：读了的消息 2：未读的消息',
 mailType int not null comment '消息种类： 1-系统邮件 2-玩家邮件 3-发送邮件',
 appendixFlag int not null default 0 comment '是否含附件: 0:否 1:是 2:已领取',
 entityIds varchar(30) comment '附件道具id1001；2201；3001',
 itemNums varchar(30) comment '附件数量1；2；3'
)ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE MailContent_34(
 mailId int primary key auto_increment comment '主键',
 userId int not null comment '玩家id',
 sendUserId int not null comment '发送方id',
 receiveUserId int not null comment '接收方id',
 sendDttm Timestamp  not null default '0000-00-00 00:00:00' comment '发送日期、时间',
  readDttm Timestamp not null default '0000-00-00 00:00:00' comment '读取日期、时间',
 content text not null comment '消息内容',
 title varchar(40) not null comment '消息标题',
 mailReceiveType int not null comment '消息状态：接收方 0：删除的消息 1：读了的消息 2：未读的消息',
 mailType int not null comment '消息种类： 1-系统邮件 2-玩家邮件 3-发送邮件',
 appendixFlag int not null default 0 comment '是否含附件: 0:否 1:是 2:已领取',
 entityIds varchar(30) comment '附件道具id1001；2201；3001',
 itemNums varchar(30) comment '附件数量1；2；3'
)ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE MailContent_35(
 mailId int primary key auto_increment comment '主键',
 userId int not null comment '玩家id',
 sendUserId int not null comment '发送方id',
 receiveUserId int not null comment '接收方id',
 sendDttm Timestamp  not null default '0000-00-00 00:00:00' comment '发送日期、时间',
  readDttm Timestamp not null default '0000-00-00 00:00:00' comment '读取日期、时间',
 content text not null comment '消息内容',
 title varchar(40) not null comment '消息标题',
 mailReceiveType int not null comment '消息状态：接收方 0：删除的消息 1：读了的消息 2：未读的消息',
 mailType int not null comment '消息种类： 1-系统邮件 2-玩家邮件 3-发送邮件',
 appendixFlag int not null default 0 comment '是否含附件: 0:否 1:是 2:已领取',
 entityIds varchar(30) comment '附件道具id1001；2201；3001',
 itemNums varchar(30) comment '附件数量1；2；3'
)ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE MailContent_36(
 mailId int primary key auto_increment comment '主键',
 userId int not null comment '玩家id',
 sendUserId int not null comment '发送方id',
 receiveUserId int not null comment '接收方id',
 sendDttm Timestamp  not null default '0000-00-00 00:00:00' comment '发送日期、时间',
  readDttm Timestamp not null default '0000-00-00 00:00:00' comment '读取日期、时间',
 content text not null comment '消息内容',
 title varchar(40) not null comment '消息标题',
 mailReceiveType int not null comment '消息状态：接收方 0：删除的消息 1：读了的消息 2：未读的消息',
 mailType int not null comment '消息种类： 1-系统邮件 2-玩家邮件 3-发送邮件',
 appendixFlag int not null default 0 comment '是否含附件: 0:否 1:是 2:已领取',
 entityIds varchar(30) comment '附件道具id1001；2201；3001',
 itemNums varchar(30) comment '附件数量1；2；3'
)ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE MailContent_37(
 mailId int primary key auto_increment comment '主键',
 userId int not null comment '玩家id',
 sendUserId int not null comment '发送方id',
 receiveUserId int not null comment '接收方id',
 sendDttm Timestamp  not null default '0000-00-00 00:00:00' comment '发送日期、时间',
  readDttm Timestamp not null default '0000-00-00 00:00:00' comment '读取日期、时间',
 content text not null comment '消息内容',
 title varchar(40) not null comment '消息标题',
 mailReceiveType int not null comment '消息状态：接收方 0：删除的消息 1：读了的消息 2：未读的消息',
 mailType int not null comment '消息种类： 1-系统邮件 2-玩家邮件 3-发送邮件',
 appendixFlag int not null default 0 comment '是否含附件: 0:否 1:是 2:已领取',
 entityIds varchar(30) comment '附件道具id1001；2201；3001',
 itemNums varchar(30) comment '附件数量1；2；3'
)ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE MailContent_38(
 mailId int primary key auto_increment comment '主键',
 userId int not null comment '玩家id',
 sendUserId int not null comment '发送方id',
 receiveUserId int not null comment '接收方id',
 sendDttm Timestamp  not null default '0000-00-00 00:00:00' comment '发送日期、时间',
  readDttm Timestamp not null default '0000-00-00 00:00:00' comment '读取日期、时间',
 content text not null comment '消息内容',
 title varchar(40) not null comment '消息标题',
 mailReceiveType int not null comment '消息状态：接收方 0：删除的消息 1：读了的消息 2：未读的消息',
 mailType int not null comment '消息种类： 1-系统邮件 2-玩家邮件 3-发送邮件',
 appendixFlag int not null default 0 comment '是否含附件: 0:否 1:是 2:已领取',
 entityIds varchar(30) comment '附件道具id1001；2201；3001',
 itemNums varchar(30) comment '附件数量1；2；3'
)ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE MailContent_39(
 mailId int primary key auto_increment comment '主键',
 userId int not null comment '玩家id',
 sendUserId int not null comment '发送方id',
 receiveUserId int not null comment '接收方id',
 sendDttm Timestamp  not null default '0000-00-00 00:00:00' comment '发送日期、时间',
  readDttm Timestamp not null default '0000-00-00 00:00:00' comment '读取日期、时间',
 content text not null comment '消息内容',
 title varchar(40) not null comment '消息标题',
 mailReceiveType int not null comment '消息状态：接收方 0：删除的消息 1：读了的消息 2：未读的消息',
 mailType int not null comment '消息种类： 1-系统邮件 2-玩家邮件 3-发送邮件',
 appendixFlag int not null default 0 comment '是否含附件: 0:否 1:是 2:已领取',
 entityIds varchar(30) comment '附件道具id1001；2201；3001',
 itemNums varchar(30) comment '附件数量1；2；3'
)ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE MailContent_40(
 mailId int primary key auto_increment comment '主键',
 userId int not null comment '玩家id',
 sendUserId int not null comment '发送方id',
 receiveUserId int not null comment '接收方id',
 sendDttm Timestamp  not null default '0000-00-00 00:00:00' comment '发送日期、时间',
  readDttm Timestamp not null default '0000-00-00 00:00:00' comment '读取日期、时间',
 content text not null comment '消息内容',
 title varchar(40) not null comment '消息标题',
 mailReceiveType int not null comment '消息状态：接收方 0：删除的消息 1：读了的消息 2：未读的消息',
 mailType int not null comment '消息种类： 1-系统邮件 2-玩家邮件 3-发送邮件',
 appendixFlag int not null default 0 comment '是否含附件: 0:否 1:是 2:已领取',
 entityIds varchar(30) comment '附件道具id1001；2201；3001',
 itemNums varchar(30) comment '附件数量1；2；3'
)ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE MailContent_41(
 mailId int primary key auto_increment comment '主键',
 userId int not null comment '玩家id',
 sendUserId int not null comment '发送方id',
 receiveUserId int not null comment '接收方id',
 sendDttm Timestamp  not null default '0000-00-00 00:00:00' comment '发送日期、时间',
  readDttm Timestamp not null default '0000-00-00 00:00:00' comment '读取日期、时间',
 content text not null comment '消息内容',
 title varchar(40) not null comment '消息标题',
 mailReceiveType int not null comment '消息状态：接收方 0：删除的消息 1：读了的消息 2：未读的消息',
 mailType int not null comment '消息种类： 1-系统邮件 2-玩家邮件 3-发送邮件',
 appendixFlag int not null default 0 comment '是否含附件: 0:否 1:是 2:已领取',
 entityIds varchar(30) comment '附件道具id1001；2201；3001',
 itemNums varchar(30) comment '附件数量1；2；3'
)ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE MailContent_42(
 mailId int primary key auto_increment comment '主键',
 userId int not null comment '玩家id',
 sendUserId int not null comment '发送方id',
 receiveUserId int not null comment '接收方id',
 sendDttm Timestamp  not null default '0000-00-00 00:00:00' comment '发送日期、时间',
  readDttm Timestamp not null default '0000-00-00 00:00:00' comment '读取日期、时间',
 content text not null comment '消息内容',
 title varchar(40) not null comment '消息标题',
 mailReceiveType int not null comment '消息状态：接收方 0：删除的消息 1：读了的消息 2：未读的消息',
 mailType int not null comment '消息种类： 1-系统邮件 2-玩家邮件 3-发送邮件',
 appendixFlag int not null default 0 comment '是否含附件: 0:否 1:是 2:已领取',
 entityIds varchar(30) comment '附件道具id1001；2201；3001',
 itemNums varchar(30) comment '附件数量1；2；3'
)ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE MailContent_43(
 mailId int primary key auto_increment comment '主键',
 userId int not null comment '玩家id',
 sendUserId int not null comment '发送方id',
 receiveUserId int not null comment '接收方id',
 sendDttm Timestamp  not null default '0000-00-00 00:00:00' comment '发送日期、时间',
  readDttm Timestamp not null default '0000-00-00 00:00:00' comment '读取日期、时间',
 content text not null comment '消息内容',
 title varchar(40) not null comment '消息标题',
 mailReceiveType int not null comment '消息状态：接收方 0：删除的消息 1：读了的消息 2：未读的消息',
 mailType int not null comment '消息种类： 1-系统邮件 2-玩家邮件 3-发送邮件',
 appendixFlag int not null default 0 comment '是否含附件: 0:否 1:是 2:已领取',
 entityIds varchar(30) comment '附件道具id1001；2201；3001',
 itemNums varchar(30) comment '附件数量1；2；3'
)ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE MailContent_44(
 mailId int primary key auto_increment comment '主键',
 userId int not null comment '玩家id',
 sendUserId int not null comment '发送方id',
 receiveUserId int not null comment '接收方id',
 sendDttm Timestamp  not null default '0000-00-00 00:00:00' comment '发送日期、时间',
  readDttm Timestamp not null default '0000-00-00 00:00:00' comment '读取日期、时间',
 content text not null comment '消息内容',
 title varchar(40) not null comment '消息标题',
 mailReceiveType int not null comment '消息状态：接收方 0：删除的消息 1：读了的消息 2：未读的消息',
 mailType int not null comment '消息种类： 1-系统邮件 2-玩家邮件 3-发送邮件',
 appendixFlag int not null default 0 comment '是否含附件: 0:否 1:是 2:已领取',
 entityIds varchar(30) comment '附件道具id1001；2201；3001',
 itemNums varchar(30) comment '附件数量1；2；3'
)ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE MailContent_45(
 mailId int primary key auto_increment comment '主键',
 userId int not null comment '玩家id',
 sendUserId int not null comment '发送方id',
 receiveUserId int not null comment '接收方id',
 sendDttm Timestamp  not null default '0000-00-00 00:00:00' comment '发送日期、时间',
  readDttm Timestamp not null default '0000-00-00 00:00:00' comment '读取日期、时间',
 content text not null comment '消息内容',
 title varchar(40) not null comment '消息标题',
 mailReceiveType int not null comment '消息状态：接收方 0：删除的消息 1：读了的消息 2：未读的消息',
 mailType int not null comment '消息种类： 1-系统邮件 2-玩家邮件 3-发送邮件',
 appendixFlag int not null default 0 comment '是否含附件: 0:否 1:是 2:已领取',
 entityIds varchar(30) comment '附件道具id1001；2201；3001',
 itemNums varchar(30) comment '附件数量1；2；3'
)ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE MailContent_46(
 mailId int primary key auto_increment comment '主键',
 userId int not null comment '玩家id',
 sendUserId int not null comment '发送方id',
 receiveUserId int not null comment '接收方id',
 sendDttm Timestamp  not null default '0000-00-00 00:00:00' comment '发送日期、时间',
  readDttm Timestamp not null default '0000-00-00 00:00:00' comment '读取日期、时间',
 content text not null comment '消息内容',
 title varchar(40) not null comment '消息标题',
 mailReceiveType int not null comment '消息状态：接收方 0：删除的消息 1：读了的消息 2：未读的消息',
 mailType int not null comment '消息种类： 1-系统邮件 2-玩家邮件 3-发送邮件',
 appendixFlag int not null default 0 comment '是否含附件: 0:否 1:是 2:已领取',
 entityIds varchar(30) comment '附件道具id1001；2201；3001',
 itemNums varchar(30) comment '附件数量1；2；3'
)ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE MailContent_47(
 mailId int primary key auto_increment comment '主键',
 userId int not null comment '玩家id',
 sendUserId int not null comment '发送方id',
 receiveUserId int not null comment '接收方id',
 sendDttm Timestamp  not null default '0000-00-00 00:00:00' comment '发送日期、时间',
  readDttm Timestamp not null default '0000-00-00 00:00:00' comment '读取日期、时间',
 content text not null comment '消息内容',
 title varchar(40) not null comment '消息标题',
 mailReceiveType int not null comment '消息状态：接收方 0：删除的消息 1：读了的消息 2：未读的消息',
 mailType int not null comment '消息种类： 1-系统邮件 2-玩家邮件 3-发送邮件',
 appendixFlag int not null default 0 comment '是否含附件: 0:否 1:是 2:已领取',
 entityIds varchar(30) comment '附件道具id1001；2201；3001',
 itemNums varchar(30) comment '附件数量1；2；3'
)ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE MailContent_48(
 mailId int primary key auto_increment comment '主键',
 userId int not null comment '玩家id',
 sendUserId int not null comment '发送方id',
 receiveUserId int not null comment '接收方id',
 sendDttm Timestamp  not null default '0000-00-00 00:00:00' comment '发送日期、时间',
  readDttm Timestamp not null default '0000-00-00 00:00:00' comment '读取日期、时间',
 content text not null comment '消息内容',
 title varchar(40) not null comment '消息标题',
 mailReceiveType int not null comment '消息状态：接收方 0：删除的消息 1：读了的消息 2：未读的消息',
 mailType int not null comment '消息种类： 1-系统邮件 2-玩家邮件 3-发送邮件',
 appendixFlag int not null default 0 comment '是否含附件: 0:否 1:是 2:已领取',
 entityIds varchar(30) comment '附件道具id1001；2201；3001',
 itemNums varchar(30) comment '附件数量1；2；3'
)ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE MailContent_49(
 mailId int primary key auto_increment comment '主键',
 userId int not null comment '玩家id',
 sendUserId int not null comment '发送方id',
 receiveUserId int not null comment '接收方id',
 sendDttm Timestamp  not null default '0000-00-00 00:00:00' comment '发送日期、时间',
  readDttm Timestamp not null default '0000-00-00 00:00:00' comment '读取日期、时间',
 content text not null comment '消息内容',
 title varchar(40) not null comment '消息标题',
 mailReceiveType int not null comment '消息状态：接收方 0：删除的消息 1：读了的消息 2：未读的消息',
 mailType int not null comment '消息种类： 1-系统邮件 2-玩家邮件 3-发送邮件',
 appendixFlag int not null default 0 comment '是否含附件: 0:否 1:是 2:已领取',
 entityIds varchar(30) comment '附件道具id1001；2201；3001',
 itemNums varchar(30) comment '附件数量1；2；3'
)ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE MailContent_50(
 mailId int primary key auto_increment comment '主键',
 userId int not null comment '玩家id',
 sendUserId int not null comment '发送方id',
 receiveUserId int not null comment '接收方id',
 sendDttm Timestamp  not null default '0000-00-00 00:00:00' comment '发送日期、时间',
  readDttm Timestamp not null default '0000-00-00 00:00:00' comment '读取日期、时间',
 content text not null comment '消息内容',
 title varchar(40) not null comment '消息标题',
 mailReceiveType int not null comment '消息状态：接收方 0：删除的消息 1：读了的消息 2：未读的消息',
 mailType int not null comment '消息种类： 1-系统邮件 2-玩家邮件 3-发送邮件',
 appendixFlag int not null default 0 comment '是否含附件: 0:否 1:是 2:已领取',
 entityIds varchar(30) comment '附件道具id1001；2201；3001',
 itemNums varchar(30) comment '附件数量1；2；3'
)ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE MailContent_51(
 mailId int primary key auto_increment comment '主键',
 userId int not null comment '玩家id',
 sendUserId int not null comment '发送方id',
 receiveUserId int not null comment '接收方id',
 sendDttm Timestamp  not null default '0000-00-00 00:00:00' comment '发送日期、时间',
  readDttm Timestamp not null default '0000-00-00 00:00:00' comment '读取日期、时间',
 content text not null comment '消息内容',
 title varchar(40) not null comment '消息标题',
 mailReceiveType int not null comment '消息状态：接收方 0：删除的消息 1：读了的消息 2：未读的消息',
 mailType int not null comment '消息种类： 1-系统邮件 2-玩家邮件 3-发送邮件',
 appendixFlag int not null default 0 comment '是否含附件: 0:否 1:是 2:已领取',
 entityIds varchar(30) comment '附件道具id1001；2201；3001',
 itemNums varchar(30) comment '附件数量1；2；3'
)ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE MailContent_52(
 mailId int primary key auto_increment comment '主键',
 userId int not null comment '玩家id',
 sendUserId int not null comment '发送方id',
 receiveUserId int not null comment '接收方id',
 sendDttm Timestamp  not null default '0000-00-00 00:00:00' comment '发送日期、时间',
  readDttm Timestamp not null default '0000-00-00 00:00:00' comment '读取日期、时间',
 content text not null comment '消息内容',
 title varchar(40) not null comment '消息标题',
 mailReceiveType int not null comment '消息状态：接收方 0：删除的消息 1：读了的消息 2：未读的消息',
 mailType int not null comment '消息种类： 1-系统邮件 2-玩家邮件 3-发送邮件',
 appendixFlag int not null default 0 comment '是否含附件: 0:否 1:是 2:已领取',
 entityIds varchar(30) comment '附件道具id1001；2201；3001',
 itemNums varchar(30) comment '附件数量1；2；3'
)ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE MailContent_53(
 mailId int primary key auto_increment comment '主键',
 userId int not null comment '玩家id',
 sendUserId int not null comment '发送方id',
 receiveUserId int not null comment '接收方id',
 sendDttm Timestamp  not null default '0000-00-00 00:00:00' comment '发送日期、时间',
  readDttm Timestamp not null default '0000-00-00 00:00:00' comment '读取日期、时间',
 content text not null comment '消息内容',
 title varchar(40) not null comment '消息标题',
 mailReceiveType int not null comment '消息状态：接收方 0：删除的消息 1：读了的消息 2：未读的消息',
 mailType int not null comment '消息种类： 1-系统邮件 2-玩家邮件 3-发送邮件',
 appendixFlag int not null default 0 comment '是否含附件: 0:否 1:是 2:已领取',
 entityIds varchar(30) comment '附件道具id1001；2201；3001',
 itemNums varchar(30) comment '附件数量1；2；3'
)ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE MailContent_54(
 mailId int primary key auto_increment comment '主键',
 userId int not null comment '玩家id',
 sendUserId int not null comment '发送方id',
 receiveUserId int not null comment '接收方id',
 sendDttm Timestamp  not null default '0000-00-00 00:00:00' comment '发送日期、时间',
  readDttm Timestamp not null default '0000-00-00 00:00:00' comment '读取日期、时间',
 content text not null comment '消息内容',
 title varchar(40) not null comment '消息标题',
 mailReceiveType int not null comment '消息状态：接收方 0：删除的消息 1：读了的消息 2：未读的消息',
 mailType int not null comment '消息种类： 1-系统邮件 2-玩家邮件 3-发送邮件',
 appendixFlag int not null default 0 comment '是否含附件: 0:否 1:是 2:已领取',
 entityIds varchar(30) comment '附件道具id1001；2201；3001',
 itemNums varchar(30) comment '附件数量1；2；3'
)ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE MailContent_55(
 mailId int primary key auto_increment comment '主键',
 userId int not null comment '玩家id',
 sendUserId int not null comment '发送方id',
 receiveUserId int not null comment '接收方id',
 sendDttm Timestamp  not null default '0000-00-00 00:00:00' comment '发送日期、时间',
  readDttm Timestamp not null default '0000-00-00 00:00:00' comment '读取日期、时间',
 content text not null comment '消息内容',
 title varchar(40) not null comment '消息标题',
 mailReceiveType int not null comment '消息状态：接收方 0：删除的消息 1：读了的消息 2：未读的消息',
 mailType int not null comment '消息种类： 1-系统邮件 2-玩家邮件 3-发送邮件',
 appendixFlag int not null default 0 comment '是否含附件: 0:否 1:是 2:已领取',
 entityIds varchar(30) comment '附件道具id1001；2201；3001',
 itemNums varchar(30) comment '附件数量1；2；3'
)ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE MailContent_56(
 mailId int primary key auto_increment comment '主键',
 userId int not null comment '玩家id',
 sendUserId int not null comment '发送方id',
 receiveUserId int not null comment '接收方id',
 sendDttm Timestamp  not null default '0000-00-00 00:00:00' comment '发送日期、时间',
  readDttm Timestamp not null default '0000-00-00 00:00:00' comment '读取日期、时间',
 content text not null comment '消息内容',
 title varchar(40) not null comment '消息标题',
 mailReceiveType int not null comment '消息状态：接收方 0：删除的消息 1：读了的消息 2：未读的消息',
 mailType int not null comment '消息种类： 1-系统邮件 2-玩家邮件 3-发送邮件',
 appendixFlag int not null default 0 comment '是否含附件: 0:否 1:是 2:已领取',
 entityIds varchar(30) comment '附件道具id1001；2201；3001',
 itemNums varchar(30) comment '附件数量1；2；3'
)ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE MailContent_57(
 mailId int primary key auto_increment comment '主键',
 userId int not null comment '玩家id',
 sendUserId int not null comment '发送方id',
 receiveUserId int not null comment '接收方id',
 sendDttm Timestamp  not null default '0000-00-00 00:00:00' comment '发送日期、时间',
  readDttm Timestamp not null default '0000-00-00 00:00:00' comment '读取日期、时间',
 content text not null comment '消息内容',
 title varchar(40) not null comment '消息标题',
 mailReceiveType int not null comment '消息状态：接收方 0：删除的消息 1：读了的消息 2：未读的消息',
 mailType int not null comment '消息种类： 1-系统邮件 2-玩家邮件 3-发送邮件',
 appendixFlag int not null default 0 comment '是否含附件: 0:否 1:是 2:已领取',
 entityIds varchar(30) comment '附件道具id1001；2201；3001',
 itemNums varchar(30) comment '附件数量1；2；3'
)ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE MailContent_58(
 mailId int primary key auto_increment comment '主键',
 userId int not null comment '玩家id',
 sendUserId int not null comment '发送方id',
 receiveUserId int not null comment '接收方id',
 sendDttm Timestamp  not null default '0000-00-00 00:00:00' comment '发送日期、时间',
  readDttm Timestamp not null default '0000-00-00 00:00:00' comment '读取日期、时间',
 content text not null comment '消息内容',
 title varchar(40) not null comment '消息标题',
 mailReceiveType int not null comment '消息状态：接收方 0：删除的消息 1：读了的消息 2：未读的消息',
 mailType int not null comment '消息种类： 1-系统邮件 2-玩家邮件 3-发送邮件',
 appendixFlag int not null default 0 comment '是否含附件: 0:否 1:是 2:已领取',
 entityIds varchar(30) comment '附件道具id1001；2201；3001',
 itemNums varchar(30) comment '附件数量1；2；3'
)ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE MailContent_59(
 mailId int primary key auto_increment comment '主键',
 userId int not null comment '玩家id',
 sendUserId int not null comment '发送方id',
 receiveUserId int not null comment '接收方id',
 sendDttm Timestamp  not null default '0000-00-00 00:00:00' comment '发送日期、时间',
  readDttm Timestamp not null default '0000-00-00 00:00:00' comment '读取日期、时间',
 content text not null comment '消息内容',
 title varchar(40) not null comment '消息标题',
 mailReceiveType int not null comment '消息状态：接收方 0：删除的消息 1：读了的消息 2：未读的消息',
 mailType int not null comment '消息种类： 1-系统邮件 2-玩家邮件 3-发送邮件',
 appendixFlag int not null default 0 comment '是否含附件: 0:否 1:是 2:已领取',
 entityIds varchar(30) comment '附件道具id1001；2201；3001',
 itemNums varchar(30) comment '附件数量1；2；3'
)ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE MailContent_60(
 mailId int primary key auto_increment comment '主键',
 userId int not null comment '玩家id',
 sendUserId int not null comment '发送方id',
 receiveUserId int not null comment '接收方id',
 sendDttm Timestamp  not null default '0000-00-00 00:00:00' comment '发送日期、时间',
  readDttm Timestamp not null default '0000-00-00 00:00:00' comment '读取日期、时间',
 content text not null comment '消息内容',
 title varchar(40) not null comment '消息标题',
 mailReceiveType int not null comment '消息状态：接收方 0：删除的消息 1：读了的消息 2：未读的消息',
 mailType int not null comment '消息种类： 1-系统邮件 2-玩家邮件 3-发送邮件',
 appendixFlag int not null default 0 comment '是否含附件: 0:否 1:是 2:已领取',
 entityIds varchar(30) comment '附件道具id1001；2201；3001',
 itemNums varchar(30) comment '附件数量1；2；3'
)ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE MailContent_61(
 mailId int primary key auto_increment comment '主键',
 userId int not null comment '玩家id',
 sendUserId int not null comment '发送方id',
 receiveUserId int not null comment '接收方id',
 sendDttm Timestamp  not null default '0000-00-00 00:00:00' comment '发送日期、时间',
  readDttm Timestamp not null default '0000-00-00 00:00:00' comment '读取日期、时间',
 content text not null comment '消息内容',
 title varchar(40) not null comment '消息标题',
 mailReceiveType int not null comment '消息状态：接收方 0：删除的消息 1：读了的消息 2：未读的消息',
 mailType int not null comment '消息种类： 1-系统邮件 2-玩家邮件 3-发送邮件',
 appendixFlag int not null default 0 comment '是否含附件: 0:否 1:是 2:已领取',
 entityIds varchar(30) comment '附件道具id1001；2201；3001',
 itemNums varchar(30) comment '附件数量1；2；3'
)ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE MailContent_62(
 mailId int primary key auto_increment comment '主键',
 userId int not null comment '玩家id',
 sendUserId int not null comment '发送方id',
 receiveUserId int not null comment '接收方id',
 sendDttm Timestamp  not null default '0000-00-00 00:00:00' comment '发送日期、时间',
  readDttm Timestamp not null default '0000-00-00 00:00:00' comment '读取日期、时间',
 content text not null comment '消息内容',
 title varchar(40) not null comment '消息标题',
 mailReceiveType int not null comment '消息状态：接收方 0：删除的消息 1：读了的消息 2：未读的消息',
 mailType int not null comment '消息种类： 1-系统邮件 2-玩家邮件 3-发送邮件',
 appendixFlag int not null default 0 comment '是否含附件: 0:否 1:是 2:已领取',
 entityIds varchar(30) comment '附件道具id1001；2201；3001',
 itemNums varchar(30) comment '附件数量1；2；3'
)ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE MailContent_63(
 mailId int primary key auto_increment comment '主键',
 userId int not null comment '玩家id',
 sendUserId int not null comment '发送方id',
 receiveUserId int not null comment '接收方id',
 sendDttm Timestamp  not null default '0000-00-00 00:00:00' comment '发送日期、时间',
  readDttm Timestamp not null default '0000-00-00 00:00:00' comment '读取日期、时间',
 content text not null comment '消息内容',
 title varchar(40) not null comment '消息标题',
 mailReceiveType int not null comment '消息状态：接收方 0：删除的消息 1：读了的消息 2：未读的消息',
 mailType int not null comment '消息种类： 1-系统邮件 2-玩家邮件 3-发送邮件',
 appendixFlag int not null default 0 comment '是否含附件: 0:否 1:是 2:已领取',
 entityIds varchar(30) comment '附件道具id1001；2201；3001',
 itemNums varchar(30) comment '附件数量1；2；3'
)ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE MailContent_64(
 mailId int primary key auto_increment comment '主键',
 userId int not null comment '玩家id',
 sendUserId int not null comment '发送方id',
 receiveUserId int not null comment '接收方id',
 sendDttm Timestamp  not null default '0000-00-00 00:00:00' comment '发送日期、时间',
  readDttm Timestamp not null default '0000-00-00 00:00:00' comment '读取日期、时间',
 content text not null comment '消息内容',
 title varchar(40) not null comment '消息标题',
 mailReceiveType int not null comment '消息状态：接收方 0：删除的消息 1：读了的消息 2：未读的消息',
 mailType int not null comment '消息种类： 1-系统邮件 2-玩家邮件 3-发送邮件',
 appendixFlag int not null default 0 comment '是否含附件: 0:否 1:是 2:已领取',
 entityIds varchar(30) comment '附件道具id1001；2201；3001',
 itemNums varchar(30) comment '附件数量1；2；3'
)ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE MailContent_65(
 mailId int primary key auto_increment comment '主键',
 userId int not null comment '玩家id',
 sendUserId int not null comment '发送方id',
 receiveUserId int not null comment '接收方id',
 sendDttm Timestamp  not null default '0000-00-00 00:00:00' comment '发送日期、时间',
  readDttm Timestamp not null default '0000-00-00 00:00:00' comment '读取日期、时间',
 content text not null comment '消息内容',
 title varchar(40) not null comment '消息标题',
 mailReceiveType int not null comment '消息状态：接收方 0：删除的消息 1：读了的消息 2：未读的消息',
 mailType int not null comment '消息种类： 1-系统邮件 2-玩家邮件 3-发送邮件',
 appendixFlag int not null default 0 comment '是否含附件: 0:否 1:是 2:已领取',
 entityIds varchar(30) comment '附件道具id1001；2201；3001',
 itemNums varchar(30) comment '附件数量1；2；3'
)ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE MailContent_66(
 mailId int primary key auto_increment comment '主键',
 userId int not null comment '玩家id',
 sendUserId int not null comment '发送方id',
 receiveUserId int not null comment '接收方id',
 sendDttm Timestamp  not null default '0000-00-00 00:00:00' comment '发送日期、时间',
  readDttm Timestamp not null default '0000-00-00 00:00:00' comment '读取日期、时间',
 content text not null comment '消息内容',
 title varchar(40) not null comment '消息标题',
 mailReceiveType int not null comment '消息状态：接收方 0：删除的消息 1：读了的消息 2：未读的消息',
 mailType int not null comment '消息种类： 1-系统邮件 2-玩家邮件 3-发送邮件',
 appendixFlag int not null default 0 comment '是否含附件: 0:否 1:是 2:已领取',
 entityIds varchar(30) comment '附件道具id1001；2201；3001',
 itemNums varchar(30) comment '附件数量1；2；3'
)ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE MailContent_67(
 mailId int primary key auto_increment comment '主键',
 userId int not null comment '玩家id',
 sendUserId int not null comment '发送方id',
 receiveUserId int not null comment '接收方id',
 sendDttm Timestamp  not null default '0000-00-00 00:00:00' comment '发送日期、时间',
  readDttm Timestamp not null default '0000-00-00 00:00:00' comment '读取日期、时间',
 content text not null comment '消息内容',
 title varchar(40) not null comment '消息标题',
 mailReceiveType int not null comment '消息状态：接收方 0：删除的消息 1：读了的消息 2：未读的消息',
 mailType int not null comment '消息种类： 1-系统邮件 2-玩家邮件 3-发送邮件',
 appendixFlag int not null default 0 comment '是否含附件: 0:否 1:是 2:已领取',
 entityIds varchar(30) comment '附件道具id1001；2201；3001',
 itemNums varchar(30) comment '附件数量1；2；3'
)ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE MailContent_68(
 mailId int primary key auto_increment comment '主键',
 userId int not null comment '玩家id',
 sendUserId int not null comment '发送方id',
 receiveUserId int not null comment '接收方id',
 sendDttm Timestamp  not null default '0000-00-00 00:00:00' comment '发送日期、时间',
  readDttm Timestamp not null default '0000-00-00 00:00:00' comment '读取日期、时间',
 content text not null comment '消息内容',
 title varchar(40) not null comment '消息标题',
 mailReceiveType int not null comment '消息状态：接收方 0：删除的消息 1：读了的消息 2：未读的消息',
 mailType int not null comment '消息种类： 1-系统邮件 2-玩家邮件 3-发送邮件',
 appendixFlag int not null default 0 comment '是否含附件: 0:否 1:是 2:已领取',
 entityIds varchar(30) comment '附件道具id1001；2201；3001',
 itemNums varchar(30) comment '附件数量1；2；3'
)ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE MailContent_69(
 mailId int primary key auto_increment comment '主键',
 userId int not null comment '玩家id',
 sendUserId int not null comment '发送方id',
 receiveUserId int not null comment '接收方id',
 sendDttm Timestamp  not null default '0000-00-00 00:00:00' comment '发送日期、时间',
  readDttm Timestamp not null default '0000-00-00 00:00:00' comment '读取日期、时间',
 content text not null comment '消息内容',
 title varchar(40) not null comment '消息标题',
 mailReceiveType int not null comment '消息状态：接收方 0：删除的消息 1：读了的消息 2：未读的消息',
 mailType int not null comment '消息种类： 1-系统邮件 2-玩家邮件 3-发送邮件',
 appendixFlag int not null default 0 comment '是否含附件: 0:否 1:是 2:已领取',
 entityIds varchar(30) comment '附件道具id1001；2201；3001',
 itemNums varchar(30) comment '附件数量1；2；3'
)ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE MailContent_70(
 mailId int primary key auto_increment comment '主键',
 userId int not null comment '玩家id',
 sendUserId int not null comment '发送方id',
 receiveUserId int not null comment '接收方id',
 sendDttm Timestamp  not null default '0000-00-00 00:00:00' comment '发送日期、时间',
  readDttm Timestamp not null default '0000-00-00 00:00:00' comment '读取日期、时间',
 content text not null comment '消息内容',
 title varchar(40) not null comment '消息标题',
 mailReceiveType int not null comment '消息状态：接收方 0：删除的消息 1：读了的消息 2：未读的消息',
 mailType int not null comment '消息种类： 1-系统邮件 2-玩家邮件 3-发送邮件',
 appendixFlag int not null default 0 comment '是否含附件: 0:否 1:是 2:已领取',
 entityIds varchar(30) comment '附件道具id1001；2201；3001',
 itemNums varchar(30) comment '附件数量1；2；3'
)ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE MailContent_71(
 mailId int primary key auto_increment comment '主键',
 userId int not null comment '玩家id',
 sendUserId int not null comment '发送方id',
 receiveUserId int not null comment '接收方id',
 sendDttm Timestamp  not null default '0000-00-00 00:00:00' comment '发送日期、时间',
  readDttm Timestamp not null default '0000-00-00 00:00:00' comment '读取日期、时间',
 content text not null comment '消息内容',
 title varchar(40) not null comment '消息标题',
 mailReceiveType int not null comment '消息状态：接收方 0：删除的消息 1：读了的消息 2：未读的消息',
 mailType int not null comment '消息种类： 1-系统邮件 2-玩家邮件 3-发送邮件',
 appendixFlag int not null default 0 comment '是否含附件: 0:否 1:是 2:已领取',
 entityIds varchar(30) comment '附件道具id1001；2201；3001',
 itemNums varchar(30) comment '附件数量1；2；3'
)ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE MailContent_72(
 mailId int primary key auto_increment comment '主键',
 userId int not null comment '玩家id',
 sendUserId int not null comment '发送方id',
 receiveUserId int not null comment '接收方id',
 sendDttm Timestamp  not null default '0000-00-00 00:00:00' comment '发送日期、时间',
  readDttm Timestamp not null default '0000-00-00 00:00:00' comment '读取日期、时间',
 content text not null comment '消息内容',
 title varchar(40) not null comment '消息标题',
 mailReceiveType int not null comment '消息状态：接收方 0：删除的消息 1：读了的消息 2：未读的消息',
 mailType int not null comment '消息种类： 1-系统邮件 2-玩家邮件 3-发送邮件',
 appendixFlag int not null default 0 comment '是否含附件: 0:否 1:是 2:已领取',
 entityIds varchar(30) comment '附件道具id1001；2201；3001',
 itemNums varchar(30) comment '附件数量1；2；3'
)ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE MailContent_73(
 mailId int primary key auto_increment comment '主键',
 userId int not null comment '玩家id',
 sendUserId int not null comment '发送方id',
 receiveUserId int not null comment '接收方id',
 sendDttm Timestamp  not null default '0000-00-00 00:00:00' comment '发送日期、时间',
  readDttm Timestamp not null default '0000-00-00 00:00:00' comment '读取日期、时间',
 content text not null comment '消息内容',
 title varchar(40) not null comment '消息标题',
 mailReceiveType int not null comment '消息状态：接收方 0：删除的消息 1：读了的消息 2：未读的消息',
 mailType int not null comment '消息种类： 1-系统邮件 2-玩家邮件 3-发送邮件',
 appendixFlag int not null default 0 comment '是否含附件: 0:否 1:是 2:已领取',
 entityIds varchar(30) comment '附件道具id1001；2201；3001',
 itemNums varchar(30) comment '附件数量1；2；3'
)ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE MailContent_74(
 mailId int primary key auto_increment comment '主键',
 userId int not null comment '玩家id',
 sendUserId int not null comment '发送方id',
 receiveUserId int not null comment '接收方id',
 sendDttm Timestamp  not null default '0000-00-00 00:00:00' comment '发送日期、时间',
  readDttm Timestamp not null default '0000-00-00 00:00:00' comment '读取日期、时间',
 content text not null comment '消息内容',
 title varchar(40) not null comment '消息标题',
 mailReceiveType int not null comment '消息状态：接收方 0：删除的消息 1：读了的消息 2：未读的消息',
 mailType int not null comment '消息种类： 1-系统邮件 2-玩家邮件 3-发送邮件',
 appendixFlag int not null default 0 comment '是否含附件: 0:否 1:是 2:已领取',
 entityIds varchar(30) comment '附件道具id1001；2201；3001',
 itemNums varchar(30) comment '附件数量1；2；3'
)ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE MailContent_75(
 mailId int primary key auto_increment comment '主键',
 userId int not null comment '玩家id',
 sendUserId int not null comment '发送方id',
 receiveUserId int not null comment '接收方id',
 sendDttm Timestamp  not null default '0000-00-00 00:00:00' comment '发送日期、时间',
  readDttm Timestamp not null default '0000-00-00 00:00:00' comment '读取日期、时间',
 content text not null comment '消息内容',
 title varchar(40) not null comment '消息标题',
 mailReceiveType int not null comment '消息状态：接收方 0：删除的消息 1：读了的消息 2：未读的消息',
 mailType int not null comment '消息种类： 1-系统邮件 2-玩家邮件 3-发送邮件',
 appendixFlag int not null default 0 comment '是否含附件: 0:否 1:是 2:已领取',
 entityIds varchar(30) comment '附件道具id1001；2201；3001',
 itemNums varchar(30) comment '附件数量1；2；3'
)ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE MailContent_76(
 mailId int primary key auto_increment comment '主键',
 userId int not null comment '玩家id',
 sendUserId int not null comment '发送方id',
 receiveUserId int not null comment '接收方id',
 sendDttm Timestamp  not null default '0000-00-00 00:00:00' comment '发送日期、时间',
  readDttm Timestamp not null default '0000-00-00 00:00:00' comment '读取日期、时间',
 content text not null comment '消息内容',
 title varchar(40) not null comment '消息标题',
 mailReceiveType int not null comment '消息状态：接收方 0：删除的消息 1：读了的消息 2：未读的消息',
 mailType int not null comment '消息种类： 1-系统邮件 2-玩家邮件 3-发送邮件',
 appendixFlag int not null default 0 comment '是否含附件: 0:否 1:是 2:已领取',
 entityIds varchar(30) comment '附件道具id1001；2201；3001',
 itemNums varchar(30) comment '附件数量1；2；3'
)ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE MailContent_77(
 mailId int primary key auto_increment comment '主键',
 userId int not null comment '玩家id',
 sendUserId int not null comment '发送方id',
 receiveUserId int not null comment '接收方id',
 sendDttm Timestamp  not null default '0000-00-00 00:00:00' comment '发送日期、时间',
  readDttm Timestamp not null default '0000-00-00 00:00:00' comment '读取日期、时间',
 content text not null comment '消息内容',
 title varchar(40) not null comment '消息标题',
 mailReceiveType int not null comment '消息状态：接收方 0：删除的消息 1：读了的消息 2：未读的消息',
 mailType int not null comment '消息种类： 1-系统邮件 2-玩家邮件 3-发送邮件',
 appendixFlag int not null default 0 comment '是否含附件: 0:否 1:是 2:已领取',
 entityIds varchar(30) comment '附件道具id1001；2201；3001',
 itemNums varchar(30) comment '附件数量1；2；3'
)ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE MailContent_78(
 mailId int primary key auto_increment comment '主键',
 userId int not null comment '玩家id',
 sendUserId int not null comment '发送方id',
 receiveUserId int not null comment '接收方id',
 sendDttm Timestamp  not null default '0000-00-00 00:00:00' comment '发送日期、时间',
  readDttm Timestamp not null default '0000-00-00 00:00:00' comment '读取日期、时间',
 content text not null comment '消息内容',
 title varchar(40) not null comment '消息标题',
 mailReceiveType int not null comment '消息状态：接收方 0：删除的消息 1：读了的消息 2：未读的消息',
 mailType int not null comment '消息种类： 1-系统邮件 2-玩家邮件 3-发送邮件',
 appendixFlag int not null default 0 comment '是否含附件: 0:否 1:是 2:已领取',
 entityIds varchar(30) comment '附件道具id1001；2201；3001',
 itemNums varchar(30) comment '附件数量1；2；3'
)ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE MailContent_79(
 mailId int primary key auto_increment comment '主键',
 userId int not null comment '玩家id',
 sendUserId int not null comment '发送方id',
 receiveUserId int not null comment '接收方id',
 sendDttm Timestamp  not null default '0000-00-00 00:00:00' comment '发送日期、时间',
  readDttm Timestamp not null default '0000-00-00 00:00:00' comment '读取日期、时间',
 content text not null comment '消息内容',
 title varchar(40) not null comment '消息标题',
 mailReceiveType int not null comment '消息状态：接收方 0：删除的消息 1：读了的消息 2：未读的消息',
 mailType int not null comment '消息种类： 1-系统邮件 2-玩家邮件 3-发送邮件',
 appendixFlag int not null default 0 comment '是否含附件: 0:否 1:是 2:已领取',
 entityIds varchar(30) comment '附件道具id1001；2201；3001',
 itemNums varchar(30) comment '附件数量1；2；3'
)ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE MailContent_80(
 mailId int primary key auto_increment comment '主键',
 userId int not null comment '玩家id',
 sendUserId int not null comment '发送方id',
 receiveUserId int not null comment '接收方id',
 sendDttm Timestamp  not null default '0000-00-00 00:00:00' comment '发送日期、时间',
  readDttm Timestamp not null default '0000-00-00 00:00:00' comment '读取日期、时间',
 content text not null comment '消息内容',
 title varchar(40) not null comment '消息标题',
 mailReceiveType int not null comment '消息状态：接收方 0：删除的消息 1：读了的消息 2：未读的消息',
 mailType int not null comment '消息种类： 1-系统邮件 2-玩家邮件 3-发送邮件',
 appendixFlag int not null default 0 comment '是否含附件: 0:否 1:是 2:已领取',
 entityIds varchar(30) comment '附件道具id1001；2201；3001',
 itemNums varchar(30) comment '附件数量1；2；3'
)ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE MailContent_81(
 mailId int primary key auto_increment comment '主键',
 userId int not null comment '玩家id',
 sendUserId int not null comment '发送方id',
 receiveUserId int not null comment '接收方id',
 sendDttm Timestamp  not null default '0000-00-00 00:00:00' comment '发送日期、时间',
  readDttm Timestamp not null default '0000-00-00 00:00:00' comment '读取日期、时间',
 content text not null comment '消息内容',
 title varchar(40) not null comment '消息标题',
 mailReceiveType int not null comment '消息状态：接收方 0：删除的消息 1：读了的消息 2：未读的消息',
 mailType int not null comment '消息种类： 1-系统邮件 2-玩家邮件 3-发送邮件',
 appendixFlag int not null default 0 comment '是否含附件: 0:否 1:是 2:已领取',
 entityIds varchar(30) comment '附件道具id1001；2201；3001',
 itemNums varchar(30) comment '附件数量1；2；3'
)ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE MailContent_82(
 mailId int primary key auto_increment comment '主键',
 userId int not null comment '玩家id',
 sendUserId int not null comment '发送方id',
 receiveUserId int not null comment '接收方id',
 sendDttm Timestamp  not null default '0000-00-00 00:00:00' comment '发送日期、时间',
  readDttm Timestamp not null default '0000-00-00 00:00:00' comment '读取日期、时间',
 content text not null comment '消息内容',
 title varchar(40) not null comment '消息标题',
 mailReceiveType int not null comment '消息状态：接收方 0：删除的消息 1：读了的消息 2：未读的消息',
 mailType int not null comment '消息种类： 1-系统邮件 2-玩家邮件 3-发送邮件',
 appendixFlag int not null default 0 comment '是否含附件: 0:否 1:是 2:已领取',
 entityIds varchar(30) comment '附件道具id1001；2201；3001',
 itemNums varchar(30) comment '附件数量1；2；3'
)ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE MailContent_83(
 mailId int primary key auto_increment comment '主键',
 userId int not null comment '玩家id',
 sendUserId int not null comment '发送方id',
 receiveUserId int not null comment '接收方id',
 sendDttm Timestamp  not null default '0000-00-00 00:00:00' comment '发送日期、时间',
  readDttm Timestamp not null default '0000-00-00 00:00:00' comment '读取日期、时间',
 content text not null comment '消息内容',
 title varchar(40) not null comment '消息标题',
 mailReceiveType int not null comment '消息状态：接收方 0：删除的消息 1：读了的消息 2：未读的消息',
 mailType int not null comment '消息种类： 1-系统邮件 2-玩家邮件 3-发送邮件',
 appendixFlag int not null default 0 comment '是否含附件: 0:否 1:是 2:已领取',
 entityIds varchar(30) comment '附件道具id1001；2201；3001',
 itemNums varchar(30) comment '附件数量1；2；3'
)ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE MailContent_84(
 mailId int primary key auto_increment comment '主键',
 userId int not null comment '玩家id',
 sendUserId int not null comment '发送方id',
 receiveUserId int not null comment '接收方id',
 sendDttm Timestamp  not null default '0000-00-00 00:00:00' comment '发送日期、时间',
  readDttm Timestamp not null default '0000-00-00 00:00:00' comment '读取日期、时间',
 content text not null comment '消息内容',
 title varchar(40) not null comment '消息标题',
 mailReceiveType int not null comment '消息状态：接收方 0：删除的消息 1：读了的消息 2：未读的消息',
 mailType int not null comment '消息种类： 1-系统邮件 2-玩家邮件 3-发送邮件',
 appendixFlag int not null default 0 comment '是否含附件: 0:否 1:是 2:已领取',
 entityIds varchar(30) comment '附件道具id1001；2201；3001',
 itemNums varchar(30) comment '附件数量1；2；3'
)ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE MailContent_85(
 mailId int primary key auto_increment comment '主键',
 userId int not null comment '玩家id',
 sendUserId int not null comment '发送方id',
 receiveUserId int not null comment '接收方id',
 sendDttm Timestamp  not null default '0000-00-00 00:00:00' comment '发送日期、时间',
  readDttm Timestamp not null default '0000-00-00 00:00:00' comment '读取日期、时间',
 content text not null comment '消息内容',
 title varchar(40) not null comment '消息标题',
 mailReceiveType int not null comment '消息状态：接收方 0：删除的消息 1：读了的消息 2：未读的消息',
 mailType int not null comment '消息种类： 1-系统邮件 2-玩家邮件 3-发送邮件',
 appendixFlag int not null default 0 comment '是否含附件: 0:否 1:是 2:已领取',
 entityIds varchar(30) comment '附件道具id1001；2201；3001',
 itemNums varchar(30) comment '附件数量1；2；3'
)ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE MailContent_86(
 mailId int primary key auto_increment comment '主键',
 userId int not null comment '玩家id',
 sendUserId int not null comment '发送方id',
 receiveUserId int not null comment '接收方id',
 sendDttm Timestamp  not null default '0000-00-00 00:00:00' comment '发送日期、时间',
  readDttm Timestamp not null default '0000-00-00 00:00:00' comment '读取日期、时间',
 content text not null comment '消息内容',
 title varchar(40) not null comment '消息标题',
 mailReceiveType int not null comment '消息状态：接收方 0：删除的消息 1：读了的消息 2：未读的消息',
 mailType int not null comment '消息种类： 1-系统邮件 2-玩家邮件 3-发送邮件',
 appendixFlag int not null default 0 comment '是否含附件: 0:否 1:是 2:已领取',
 entityIds varchar(30) comment '附件道具id1001；2201；3001',
 itemNums varchar(30) comment '附件数量1；2；3'
)ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE MailContent_87(
 mailId int primary key auto_increment comment '主键',
 userId int not null comment '玩家id',
 sendUserId int not null comment '发送方id',
 receiveUserId int not null comment '接收方id',
 sendDttm Timestamp  not null default '0000-00-00 00:00:00' comment '发送日期、时间',
  readDttm Timestamp not null default '0000-00-00 00:00:00' comment '读取日期、时间',
 content text not null comment '消息内容',
 title varchar(40) not null comment '消息标题',
 mailReceiveType int not null comment '消息状态：接收方 0：删除的消息 1：读了的消息 2：未读的消息',
 mailType int not null comment '消息种类： 1-系统邮件 2-玩家邮件 3-发送邮件',
 appendixFlag int not null default 0 comment '是否含附件: 0:否 1:是 2:已领取',
 entityIds varchar(30) comment '附件道具id1001；2201；3001',
 itemNums varchar(30) comment '附件数量1；2；3'
)ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE MailContent_88(
 mailId int primary key auto_increment comment '主键',
 userId int not null comment '玩家id',
 sendUserId int not null comment '发送方id',
 receiveUserId int not null comment '接收方id',
 sendDttm Timestamp  not null default '0000-00-00 00:00:00' comment '发送日期、时间',
  readDttm Timestamp not null default '0000-00-00 00:00:00' comment '读取日期、时间',
 content text not null comment '消息内容',
 title varchar(40) not null comment '消息标题',
 mailReceiveType int not null comment '消息状态：接收方 0：删除的消息 1：读了的消息 2：未读的消息',
 mailType int not null comment '消息种类： 1-系统邮件 2-玩家邮件 3-发送邮件',
 appendixFlag int not null default 0 comment '是否含附件: 0:否 1:是 2:已领取',
 entityIds varchar(30) comment '附件道具id1001；2201；3001',
 itemNums varchar(30) comment '附件数量1；2；3'
)ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE MailContent_89(
 mailId int primary key auto_increment comment '主键',
 userId int not null comment '玩家id',
 sendUserId int not null comment '发送方id',
 receiveUserId int not null comment '接收方id',
 sendDttm Timestamp  not null default '0000-00-00 00:00:00' comment '发送日期、时间',
  readDttm Timestamp not null default '0000-00-00 00:00:00' comment '读取日期、时间',
 content text not null comment '消息内容',
 title varchar(40) not null comment '消息标题',
 mailReceiveType int not null comment '消息状态：接收方 0：删除的消息 1：读了的消息 2：未读的消息',
 mailType int not null comment '消息种类： 1-系统邮件 2-玩家邮件 3-发送邮件',
 appendixFlag int not null default 0 comment '是否含附件: 0:否 1:是 2:已领取',
 entityIds varchar(30) comment '附件道具id1001；2201；3001',
 itemNums varchar(30) comment '附件数量1；2；3'
)ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE MailContent_90(
 mailId int primary key auto_increment comment '主键',
 userId int not null comment '玩家id',
 sendUserId int not null comment '发送方id',
 receiveUserId int not null comment '接收方id',
 sendDttm Timestamp  not null default '0000-00-00 00:00:00' comment '发送日期、时间',
  readDttm Timestamp not null default '0000-00-00 00:00:00' comment '读取日期、时间',
 content text not null comment '消息内容',
 title varchar(40) not null comment '消息标题',
 mailReceiveType int not null comment '消息状态：接收方 0：删除的消息 1：读了的消息 2：未读的消息',
 mailType int not null comment '消息种类： 1-系统邮件 2-玩家邮件 3-发送邮件',
 appendixFlag int not null default 0 comment '是否含附件: 0:否 1:是 2:已领取',
 entityIds varchar(30) comment '附件道具id1001；2201；3001',
 itemNums varchar(30) comment '附件数量1；2；3'
)ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE MailContent_91(
 mailId int primary key auto_increment comment '主键',
 userId int not null comment '玩家id',
 sendUserId int not null comment '发送方id',
 receiveUserId int not null comment '接收方id',
 sendDttm Timestamp  not null default '0000-00-00 00:00:00' comment '发送日期、时间',
  readDttm Timestamp not null default '0000-00-00 00:00:00' comment '读取日期、时间',
 content text not null comment '消息内容',
 title varchar(40) not null comment '消息标题',
 mailReceiveType int not null comment '消息状态：接收方 0：删除的消息 1：读了的消息 2：未读的消息',
 mailType int not null comment '消息种类： 1-系统邮件 2-玩家邮件 3-发送邮件',
 appendixFlag int not null default 0 comment '是否含附件: 0:否 1:是 2:已领取',
 entityIds varchar(30) comment '附件道具id1001；2201；3001',
 itemNums varchar(30) comment '附件数量1；2；3'
)ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE MailContent_92(
 mailId int primary key auto_increment comment '主键',
 userId int not null comment '玩家id',
 sendUserId int not null comment '发送方id',
 receiveUserId int not null comment '接收方id',
 sendDttm Timestamp  not null default '0000-00-00 00:00:00' comment '发送日期、时间',
  readDttm Timestamp not null default '0000-00-00 00:00:00' comment '读取日期、时间',
 content text not null comment '消息内容',
 title varchar(40) not null comment '消息标题',
 mailReceiveType int not null comment '消息状态：接收方 0：删除的消息 1：读了的消息 2：未读的消息',
 mailType int not null comment '消息种类： 1-系统邮件 2-玩家邮件 3-发送邮件',
 appendixFlag int not null default 0 comment '是否含附件: 0:否 1:是 2:已领取',
 entityIds varchar(30) comment '附件道具id1001；2201；3001',
 itemNums varchar(30) comment '附件数量1；2；3'
)ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE MailContent_93(
 mailId int primary key auto_increment comment '主键',
 userId int not null comment '玩家id',
 sendUserId int not null comment '发送方id',
 receiveUserId int not null comment '接收方id',
 sendDttm Timestamp  not null default '0000-00-00 00:00:00' comment '发送日期、时间',
  readDttm Timestamp not null default '0000-00-00 00:00:00' comment '读取日期、时间',
 content text not null comment '消息内容',
 title varchar(40) not null comment '消息标题',
 mailReceiveType int not null comment '消息状态：接收方 0：删除的消息 1：读了的消息 2：未读的消息',
 mailType int not null comment '消息种类： 1-系统邮件 2-玩家邮件 3-发送邮件',
 appendixFlag int not null default 0 comment '是否含附件: 0:否 1:是 2:已领取',
 entityIds varchar(30) comment '附件道具id1001；2201；3001',
 itemNums varchar(30) comment '附件数量1；2；3'
)ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE MailContent_94(
 mailId int primary key auto_increment comment '主键',
 userId int not null comment '玩家id',
 sendUserId int not null comment '发送方id',
 receiveUserId int not null comment '接收方id',
 sendDttm Timestamp  not null default '0000-00-00 00:00:00' comment '发送日期、时间',
  readDttm Timestamp not null default '0000-00-00 00:00:00' comment '读取日期、时间',
 content text not null comment '消息内容',
 title varchar(40) not null comment '消息标题',
 mailReceiveType int not null comment '消息状态：接收方 0：删除的消息 1：读了的消息 2：未读的消息',
 mailType int not null comment '消息种类： 1-系统邮件 2-玩家邮件 3-发送邮件',
 appendixFlag int not null default 0 comment '是否含附件: 0:否 1:是 2:已领取',
 entityIds varchar(30) comment '附件道具id1001；2201；3001',
 itemNums varchar(30) comment '附件数量1；2；3'
)ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE MailContent_95(
 mailId int primary key auto_increment comment '主键',
 userId int not null comment '玩家id',
 sendUserId int not null comment '发送方id',
 receiveUserId int not null comment '接收方id',
 sendDttm Timestamp  not null default '0000-00-00 00:00:00' comment '发送日期、时间',
  readDttm Timestamp not null default '0000-00-00 00:00:00' comment '读取日期、时间',
 content text not null comment '消息内容',
 title varchar(40) not null comment '消息标题',
 mailReceiveType int not null comment '消息状态：接收方 0：删除的消息 1：读了的消息 2：未读的消息',
 mailType int not null comment '消息种类： 1-系统邮件 2-玩家邮件 3-发送邮件',
 appendixFlag int not null default 0 comment '是否含附件: 0:否 1:是 2:已领取',
 entityIds varchar(30) comment '附件道具id1001；2201；3001',
 itemNums varchar(30) comment '附件数量1；2；3'
)ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE MailContent_96(
 mailId int primary key auto_increment comment '主键',
 userId int not null comment '玩家id',
 sendUserId int not null comment '发送方id',
 receiveUserId int not null comment '接收方id',
 sendDttm Timestamp  not null default '0000-00-00 00:00:00' comment '发送日期、时间',
  readDttm Timestamp not null default '0000-00-00 00:00:00' comment '读取日期、时间',
 content text not null comment '消息内容',
 title varchar(40) not null comment '消息标题',
 mailReceiveType int not null comment '消息状态：接收方 0：删除的消息 1：读了的消息 2：未读的消息',
 mailType int not null comment '消息种类： 1-系统邮件 2-玩家邮件 3-发送邮件',
 appendixFlag int not null default 0 comment '是否含附件: 0:否 1:是 2:已领取',
 entityIds varchar(30) comment '附件道具id1001；2201；3001',
 itemNums varchar(30) comment '附件数量1；2；3'
)ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE MailContent_97(
 mailId int primary key auto_increment comment '主键',
 userId int not null comment '玩家id',
 sendUserId int not null comment '发送方id',
 receiveUserId int not null comment '接收方id',
 sendDttm Timestamp  not null default '0000-00-00 00:00:00' comment '发送日期、时间',
  readDttm Timestamp not null default '0000-00-00 00:00:00' comment '读取日期、时间',
 content text not null comment '消息内容',
 title varchar(40) not null comment '消息标题',
 mailReceiveType int not null comment '消息状态：接收方 0：删除的消息 1：读了的消息 2：未读的消息',
 mailType int not null comment '消息种类： 1-系统邮件 2-玩家邮件 3-发送邮件',
 appendixFlag int not null default 0 comment '是否含附件: 0:否 1:是 2:已领取',
 entityIds varchar(30) comment '附件道具id1001；2201；3001',
 itemNums varchar(30) comment '附件数量1；2；3'
)ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE MailContent_98(
 mailId int primary key auto_increment comment '主键',
 userId int not null comment '玩家id',
 sendUserId int not null comment '发送方id',
 receiveUserId int not null comment '接收方id',
 sendDttm Timestamp  not null default '0000-00-00 00:00:00' comment '发送日期、时间',
  readDttm Timestamp not null default '0000-00-00 00:00:00' comment '读取日期、时间',
 content text not null comment '消息内容',
 title varchar(40) not null comment '消息标题',
 mailReceiveType int not null comment '消息状态：接收方 0：删除的消息 1：读了的消息 2：未读的消息',
 mailType int not null comment '消息种类： 1-系统邮件 2-玩家邮件 3-发送邮件',
 appendixFlag int not null default 0 comment '是否含附件: 0:否 1:是 2:已领取',
 entityIds varchar(30) comment '附件道具id1001；2201；3001',
 itemNums varchar(30) comment '附件数量1；2；3'
)ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE MailContent_99(
 mailId int primary key auto_increment comment '主键',
 userId int not null comment '玩家id',
 sendUserId int not null comment '发送方id',
 receiveUserId int not null comment '接收方id',
 sendDttm Timestamp  not null default '0000-00-00 00:00:00' comment '发送日期、时间',
  readDttm Timestamp not null default '0000-00-00 00:00:00' comment '读取日期、时间',
 content text not null comment '消息内容',
 title varchar(40) not null comment '消息标题',
 mailReceiveType int not null comment '消息状态：接收方 0：删除的消息 1：读了的消息 2：未读的消息',
 mailType int not null comment '消息种类： 1-系统邮件 2-玩家邮件 3-发送邮件',
 appendixFlag int not null default 0 comment '是否含附件: 0:否 1:是 2:已领取',
 entityIds varchar(30) comment '附件道具id1001；2201；3001',
 itemNums varchar(30) comment '附件数量1；2；3'
)ENGINE=InnoDB DEFAULT CHARSET=utf8;

