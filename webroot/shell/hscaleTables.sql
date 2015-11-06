drop table if exists Message_&;
create table Message_& (
   messageId            integer(16) not null auto_increment,
   sendUserId           integer(16) not null,
   receiveUserId        integer(16) not null,
   sendDttm             timestamp null default NULL,
   readDttm             timestamp null default NULL,
   comment              varchar(4000) null,
   title                varchar(100) null,
   messageSendType      integer(2) null default 0,
   messageReceiveType   integer(2) null default 0,
   messageType          integer(2) null default 0,
   childType            integer(2) null default 0,
   map                  blob null default NULL,
   appendixFlag         integer(2) null default 0,
   entityId             integer(16) null default 0,
   itemNum              integer(16) null default 0,
   equipStrongerId      integer(16) null default 0,
   orderId              varchar(20)  null comment '邮件订单号',
   KEY index_Message_receiveUserId_messageType(receiveUserId,messageType),
   primary key (messageId)
)
type = InnoDB;

drop table if exists UserAttributes_&;
create Table UserAttributes_&
(
   userId            integer(16) not null comment '',
   attrName          varchar(40) not null comment '',
   attrValue         varchar(40) not null comment '',
   primary key (userId, attrName)
)
comment = ''
type = InnoDB;



/** 以下1.4.0分 **/
drop table if exists Treasury_&;
CREATE TABLE Treasury_& (
  id int(16) NOT NULL auto_increment,
  userId int(16) NOT NULL,
  itemId int(16) NOT NULL,
  itemType int(2) default '0',
  itemCount int(4) default '0',
  useCount int(4) default '0',
  band int(2) default '0',
  equip int(2) default '0',
  throwAble int(2) default '0',
  childType int(4) default '0',
  equipStrongerId int(16) default '0',
  existEndTime timestamp NULL default NULL,
  PRIMARY KEY  (id),
  KEY index_Treasury_userId (userId)
) 
type=InnoDB;

drop table if exists EquipStronger_&;
CREATE TABLE EquipStronger_& (
  id int(16) NOT NULL auto_increment,
  equipStrongerId int(16) default '0',
  property varchar(40) default NULL,
  value1 varchar(100) default NULL,
  value2 varchar(200) default NULL,
  itemId int(16) default '0',
  PRIMARY KEY  (id),
  KEY index_EquipStronger_equipStrongerId (equipStrongerId),
  KEY index_EquipStronger_property (property)
) 
type=InnoDB;

drop table if exists UserTech_&;
create table UserTech_&
(
   userTechId           integer(16) not null auto_increment,
   userId               integer(16) not null,
   techEntId            integer(16) not null,
   level                integer(4) not null default 0,
   opDttm               timestamp null default NULL,
   primary key (userTechId),
   KEY index_UserTech_userId(userId),
   KEY index_UserTech_techEntId(techEntId)
)
type = InnoDB;

drop table if exists UserEvent_&;
create table UserEvent_&
(
   id                   integer(16) not null auto_increment,
   userId               integer(16) null default 0,
   eventId              varchar(40) null,
   num                  integer(2) null default 0,
   primary key (id),
   KEY index_UserEvent_userId(userId),
   KEY index_UserEvent_evnetId(eventId)
)
type = InnoDB;

drop table if exists UserFarmArea_&;
create table UserFarmArea_&
(
   id                   integer(16) not null auto_increment,
   userId               integer(16) null default 0,
   posX                 integer(2) null default 0,
   posY                 integer(2) null default 0,
   state                integer(2) null default 0,
   areaType             integer(2) not null default 0 comment '地块类型 0-普通；1-高级',
   primary key (id),
   KEY index_UserFarmArea_userId(userId)
)
type = InnoDB;

drop table if exists UserFarmPlant_&;
create table UserFarmPlant_&
(
   id                   integer(16) not null auto_increment,
   farmAreaId           integer(16) null default 0,
   farmTypeId           integer(16) null default 0,
   userId               integer(16) null default 0,
   status               integer(2) null default 0,
   startTime            timestamp null default NULL,
   yeild                integer(8) null default 0,
   standYeild           integer(8) null default 0,
   stealUserId          varchar(200) null,
   primary key (id),
   KEY index_UserFarmPlant_userId(userId)
)
type = InnoDB;

drop table if exists UserFuncMission_&;
create table UserFuncMission_&
(
   id                   integer(16) not null auto_increment,
   userId               integer(16) null default 0,
   missionId            integer(16) null default 0,
   primary key (id),
   KEY index_UserFuncMission_userId(userId)
)
type = InnoDB;

drop table if exists HeroEffect_&;
create table HeroEffect_&
(
   playerEffId          integer(16) not null auto_increment,
   heroId               integer(16) null default 0 ,
   effectId             varchar(40) null ,
   type                 varchar(40) null ,
   itemEffectId         integer(8) null default 0 ,
   absValue             integer(8) null default 0 ,
   perValue             integer(8) null default 0 ,
   showFlag             integer(2) null default 0 ,
   expireDttm           timestamp null default NULL ,
   primary key (playerEffId),
   KEY index_HeroEffect_heroId(heroId)
)
type = InnoDB;

drop table if exists CastleEffect_&;
create table CastleEffect_&
(
   playerEffId          integer(16) not null auto_increment,
   casId                integer(16) null default 0 ,
   effectId             varchar(40) null ,
   type                 varchar(40) null ,
   itemEffectId         integer(8) null default 0 ,
   absValue             integer(8) null default 0 ,
   perValue             integer(8) null default 0 ,
   showFlag             integer(2) null default 0 ,
   expireDttm           timestamp null default NULL ,
   primary key (playerEffId),
   KEY index_CaslteEffect_casId(casId)
)
type = InnoDB;

drop table if exists UserCard_&;
create Table UserCard_&
(
   cardId            integer(16) not null auto_increment,
   userId            integer(16) not null comment '',
   cardEntId         integer(16) not null comment '',
   status            integer(2) not null default '0' comment '',
   lockEndDttm       timestamp null default NULL comment '',
   primary key (cardId),
   KEY index_UserCard_userId(userId)
)
comment = ''
type = InnoDB;


drop table if exists CardChangLog_&;
create Table CardChangLog_&
(
   userId            integer(16) not null comment '',
   awardEntId        integer(16) not null comment '',
   suitCardId        integer(16) not null comment '',
   changNum          integer(8) not null default '-1' comment '',
   changNotes        varchar(200) null comment '',
   primary key (userId, awardEntId),
   KEY index_CardChangLog_suitCardId(suitCardId)
)
comment = ''
type = InnoDB;

drop table if exists CastleBuilding_&;
create Table CastleBuilding_&
(
	casBuiId 	integer(16) not null auto_increment,
	casId		integer(16) not null,
	buiEntId	integer(16) not null,
	level		integer(4) not null,
	posNo		varchar(40) not null,
	primary key (casBuiId),
	KEY index_CastleBuilding_casId(casId),
	KEY index_CastleBuilding_posNo(posNo)
)
type = InnoDB;