
#####粮仓和官府模版
CREATE TABLE `CopperFoodBuild` (
  `id` int(11) NOT NULL  COMMENT '主键',
  `type` tinyint(5) NOT NULL COMMENT '官府/粮仓   1铜币,2粮食',
  `collectMax` int(11) NOT NULL COMMENT '铜币生长上限',
  `resourceFull` int(11) NOT NULL COMMENT '满仓值',
  `userLevel` int(11) NOT NULL COMMENT '君主等级',
  `growSpeed` int(11) NOT NULL COMMENT '生长速度',
  `growSpeedMinute` int(11) NOT NULL COMMENT '生长时间段  N分钟',
  `costGold` int(11) NOT NULL COMMENT '花费1元宝可以买多少',
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


#####用户官府
CREATE TABLE `UserCopper` (
  `userId` int(11) NOT NULL COMMENT '用户Id',
  `copperCount` int(11) NOT NULL COMMENT '铜币总数量',
  `copperGrow` int(11) NOT NULL COMMENT '可收获的铜币数量',
  `collectAt` bigint(20) NOT NULL COMMENT '获收时间',
  PRIMARY KEY  (`userId`),
  UNIQUE KEY `uid_index` (`userId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

#####用户粮食数量
CREATE TABLE `UserFood` (
  `userId` int(11) NOT NULL,
  `foods` int(11) NOT NULL COMMENT '粮食数量',
  `foodGrow` int(11) NOT NULL COMMENT '粮食可收获数量',
  `collectAt` bigint(20) NOT NULL COMMENT '计算时间',
  PRIMARY KEY  (`userId`),
  UNIQUE KEY `uid_index` (`userId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

#####科技模版
CREATE TABLE `BookAcademy` (
  `id` int(11) NOT NULL,
  `technologyName` varchar(20) NOT NULL COMMENT '技名称科',
  `description` varchar(100) NOT NULL COMMENT '科技描述',
  `icon` varchar(100) NOT NULL,
  `heroType` tinyint(5) NOT NULL COMMENT '武将类型',
  `buildType` tinyint(5) NOT NULL COMMENT '科技类型',
  `maxLevel` tinyint(5) NOT NULL COMMENT '最高等级',
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

#####科技等级模版
CREATE TABLE `BookTechnologyLevel` (
  `id` int(11) NOT NULL,
  `technologyId` int(11) NOT NULL COMMENT '科技id',
  `technologyLevel` tinyint(5) NOT NULL COMMENT '科技等级',
  `affectType` tinyint(5) NOT NULL COMMENT '效果类型',
  `affectValueType` int(11) NOT NULL COMMENT '效果数值类型',
  `affectValue` float NOT NULL COMMENT '效果数值',
  `costCopper` int(11) NOT NULL COMMENT '消耗铜币',
  `costTime` int(11) NOT NULL COMMENT '消耗时间',
  `userLevel` tinyint(5) NOT NULL COMMENT '君主等级',
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

#####用户科技数据
CREATE TABLE `UserTechnology` (
  `id` int(11) NOT NULL auto_increment,
  `userId` int(11) NOT NULL,
  `technologyId` int(11) NOT NULL COMMENT '科技id',
  `technologyLevel` int(11) NOT NULL COMMENT '技科等级',
  `endAt` bigint(20) NOT NULL COMMENT '升级结束时间',
  `costTime` bigint(20) NOT NULL COMMENT '研究需要消耗时间',
  PRIMARY KEY  (`id`),
  UNIQUE KEY `uid_tid_index` (`technologyId`,`userId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


#####用户科技buff数据
CREATE TABLE `UserTechnologyBuff` (
  `id` int(11) NOT NULL auto_increment COMMENT '主键自增',
  `userId` int(11) NOT NULL COMMENT '用户id',
  `technologyId` int(11) NOT NULL COMMENT '科技id',
  `affectType` int(11) NOT NULL COMMENT '影响类型',
  `valueType` int(11) NOT NULL COMMENT '数值类型',
  `affectValue` float NOT NULL COMMENT '影响数值',
  PRIMARY KEY  (`id`),
  UNIQUE KEY `index_uid_tid` (`userId`,`technologyId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;