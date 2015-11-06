
##### 武将模板
CREATE TABLE HeroTemplet (
  tempId int(11) NOT NULL COMMENT '模板Id',
  heroType int(11) NOT NULL COMMENT '武将类型',
  heroName varchar(48) NOT NULL COMMENT '武将名称',
  atk int(11) NOT NULL COMMENT '攻击',
  def int(11) NOT NULL COMMENT '防御',
  hp int(11) NOT NULL COMMENT '体力值',
  durability int(11) NOT NULL default '0' COMMENT '耐久度',
  critRate int(11) NOT NULL COMMENT '暴击率',
  critHurt int(11) NOT NULL COMMENT '暴击伤害',
  dodgeRate int(11) NOT NULL COMMENT '闪避率',
  hitRate int(11) NOT NULL COMMENT '命中率',
  atkApt float(6,3) NOT NULL COMMENT '攻击力资质',
  defApt float(6,3) NOT NULL COMMENT '防御力资质',
  hpApt float(6,3) NOT NULL COMMENT '体力资质',
  quality int(11) NOT NULL COMMENT '品质',
  sparType tinyint(5) NOT NULL COMMENT '晶石系列',
  expId int(11) NOT NULL COMMENT '经验表id',
  growId int(11) NOT NULL,
  atkOtherSideTableId int(11) NOT NULL COMMENT '对方武将被攻击的顺序表id',
  atkOrder int(11) NOT NULL COMMENT '本武将的攻击顺序表',
  delHurt int(11) NOT NULL COMMENT '伤害减免',
  activeSkill int(11) NOT NULL COMMENT '主动技能',
  unactiveSkill varchar(20) NOT NULL COMMENT '被动技能',
  characterCode int(11) NOT NULL,
  resouseId int(11) NOT NULL COMMENT '前端使用',
  description varchar(100) comment '武将描述',
  PRIMARY KEY  (tempId)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


##### 武将类型模板
CREATE TABLE HeroType(
	typeId int not null comment '类型id',
	atkScope varchar(200) not null comment '攻击范围',
	name varchar(48) not null comment '类型名称',
	primary key (typeId)
)ENGINE=InnoDB DEFAULT CHARSET=utf8;

##### 武将经验模板
CREATE TABLE HeroExp(
	expId int not null comment '经验类型',
	level int not null comment '等级',
	needExp int not null comment '需要经验值',
	primary key (expId, level)
)ENGINE=InnoDB DEFAULT CHARSET=utf8;


##### 玩家武将
CREATE TABLE UserHero (
  userId int(11) NOT NULL COMMENT '玩家id',
  heroId int(11) NOT NULL auto_increment COMMENT '武将id',
  heroTempId int(11) NOT NULL COMMENT '武将模板id',
  heroName varchar(48) NOT NULL COMMENT '武将姓名',
  curExp int(2) NOT NULL COMMENT '武将经验',
  level int(3) NOT NULL COMMENT '武将等级',
  heroType int(2) NOT NULL COMMENT '武将职业',
  atk int(11) NOT NULL COMMENT '攻击',
  def int(11) NOT NULL COMMENT '防御',
  hp int(11) NOT NULL COMMENT '体力值',
  durability int(11) NOT NULL default '0' COMMENT '耐久度',
  atkApt float(6,3) NOT NULL COMMENT '攻击力资质',
  defApt float(6,3) NOT NULL COMMENT '防御力资质',
  hpApt float(6,3) NOT NULL COMMENT '体力资质',
  activeSkill int(11) NOT NULL COMMENT '主动技能',
  unactiveSkill varchar(20) NOT NULL COMMENT '被动技能',
  coordinateX int(2) NOT NULL default '-1' COMMENT '箭塔摆放位置X',
  coordinateY int(2) NOT NULL default '-1' COMMENT '箭塔摆放位置Y',
  growRankId int(2) NOT NULL default '0' COMMENT '成长次数',
  characterCode int(2) NOT NULL COMMENT '武将性格',
   isNew int(11) default 0 comment '武将是否为新武将',
  PRIMARY KEY  (heroId)
) ENGINE=InnoDB AUTO_INCREMENT=516 DEFAULT CHARSET=utf8;




##### 对方武将被攻击的顺序表
CREATE TABLE OtherSideHeroAtkedOrder(
	tableId int not null comment '主键',
	heroType1 int not null comment '武将类型1',
	weight1 int not null comment '权重1',
	heroType2 int not null comment '武将类型1',
	weight2 int not null comment '权重1',
	heroType3 int not null comment '武将类型1',
	weight3 int not null comment '权重1',
	heroType4 int not null comment '武将类型1',
	weight4 int not null comment '权重1',
	heroType5 int not null comment '武将类型1',
	weight5 int not null comment '权重1',
	primary key (tableId)
)ENGINE=InnoDB DEFAULT CHARSET=utf8;


####武将阵型
CREATE TABLE HeroForm(
	userId int not null comment '玩家id',
	formId int not null comment '阵型id',
	formDetail varchar(50) comment '阵型详情',
	primary key (userId, formId)
)ENGINE=InnoDB DEFAULT CHARSET=utf8;

####武将成长模板
CREATE TABLE HeroGrowTemplet(
	growId int not null comment '成长表id',
	rankId int not null comment '阶数',
	itemTempId int not null comment '成长或进阶需要消耗的道具',
	itemNum int not null comment '成长或进阶需要消耗的道具数量',
	addNum float not null comment '成长增加值',
	primary key (growId, rankId)
)ENGINE=InnoDB DEFAULT CHARSET=utf8;