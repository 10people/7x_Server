
#####成就
CREATE TABLE `Achieve` (
  `id` int(11) NOT NULL,
  `name` varchar(100) NOT NULL,
  `description` varchar(200) NOT NULL,
  `stageCount` int(11) NOT NULL COMMENT '阶段数量',
  `achieveType` int(11) NOT NULL COMMENT '成就完成条件类型',
  `achieveValue` int(11) NOT NULL COMMENT '指定属性',
  `amount` varchar(100) NOT NULL COMMENT '成就完成条件数量',
  `awardId` varchar(200) NOT NULL COMMENT '成就完成奖励',
  `addType` tinyint(2) NOT NULL COMMENT '成就计算类型  1累加,2替换',
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


#####用户成就
CREATE TABLE `UserAchieve` (
  `id` int(11) NOT NULL auto_increment,
  `userId` int(11) NOT NULL,
  `achieveId` int(11) NOT NULL COMMENT '就成id',
  `achieveType` int(11) NOT NULL COMMENT '完成条件类型',
  `stageNow` tinyint(5) NOT NULL COMMENT '用户当前成就阶段',
  `isAward` tinyint(2) NOT NULL COMMENT '是否可领取奖励',
  `isFinish` tinyint(2) NOT NULL COMMENT '此成就是否完成',
  `achieveValue` int(11) NOT NULL COMMENT '完成条件类型属性',
  `requestNum` varchar(200) NOT NULL COMMENT '成就条件完成需要的数量',
  `finishNum` varchar(100) NOT NULL COMMENT '成就条件完成的数量',
  `stageCount` tinyint(5) NOT NULL COMMENT '成就一共几个阶段',
  `addType` tinyint(2) NOT NULL COMMENT '成就次数计算类型 1累加,2替换',
  `achieveAt` bigint(20) NOT NULL,
  PRIMARY KEY  (`id`),
  UNIQUE KEY `uid_achieveid` (`userId`,`achieveId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;






















#####成就完成条件
CREATE TABLE `AchieveGoal` (
  `id` int(11) NOT NULL,
  `achieveId` int(11) NOT NULL COMMENT '成就id',
  `stageNum` int(11) NOT NULL COMMENT '成就阶段',
  `achieveType` int(11) NOT NULL COMMENT '成就完成类型',
  `achieveValue` int(11) NOT NULL COMMENT '成就条件',
  `amount` int(11) NOT NULL COMMENT '成就条件需要完成的数量',
  `score` int(11) NOT NULL COMMENT '可得到的分数',
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8