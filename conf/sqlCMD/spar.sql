#####晶石等级模版
CREATE TABLE SparLevel (
  sparId int(11) NOT NULL COMMENT '晶石ID',
  sparType tinyint(5) NOT NULL COMMENT '晶石系列',
  sparLevel tinyint(5) NOT NULL COMMENT '晶石等级',
  sparName varchar(20) NOT NULL COMMENT '晶石名称',
  description varchar(100) NOT NULL COMMENT '晶石描述',
  picPath varchar(48) not null comment '图片路径',
  experienceMaxValue int(11) NOT NULL COMMENT '最大经验数值',
  experienceChangValue int(11) NOT NULL COMMENT '转换经验数值',
  PRIMARY KEY  (sparId)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

#####技能晶石数据
CREATE TABLE UserSpar (
  userSparId int(11) NOT NULL auto_increment,
  heroId int(11) NOT NULL default '0' COMMENT '用户武将ID',      
  userId int(11) NOT NULL COMMENT '玩家ID',
  sparId int(11) NOT NULL COMMENT '晶石等级模板ID',
  sparLevel int(11) NOT NULL COMMENT '晶石等级',
  sparNum int(11) NOT NULL COMMENT '晶石个数',
  experienceValue int(11) NOT NULL COMMENT '经验数值',
  PRIMARY KEY  (userSparId)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
