#####活动副本
CREATE TABLE `ActivityCopy` (
  `id` int(11) NOT NULL,
  `name` varchar(50) NOT NULL,
  `description` varchar(200) NOT NULL,
  `copyType` int(11) NOT NULL COMMENT '副本类型,是活动副本还是紧急',
  `passAward` int(11) NOT NULL COMMENT '通关奖励',
  `level` int(11) NOT NULL COMMENT '需求等级',
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ;



#####活动副本配置
CREATE TABLE `ActivityConf` (
  `id` int(11) NOT NULL auto_increment,
  `copyId` int(11) NOT NULL,
  `day` varchar(50) NOT NULL,
  `isClose` int(11) NOT NULL,
  `beginAt` varchar(50) NOT NULL,
  `endAt` varchar(50) NOT NULL,
  `isDefault` int(11) NOT NULL COMMENT '是否是默认配置',
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


#####活动副本奖励倍数配置
CREATE TABLE `ActivityAward` (
  `id` int(11) NOT NULL auto_increment,
  `copyId` int(11) NOT NULL,
  `day` varchar(50) NOT NULL COMMENT '日期',
  `beginAt` varchar(50) NOT NULL COMMENT '开始时间',
  `endAt` varchar(50) NOT NULL COMMENT '结束时间',
  `awardMore` int(11) NOT NULL COMMENT '掉落概率倍数',
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ;


#####用户活动副本
CREATE TABLE `UserActivityCopy` (
  `id` int(11) NOT NULL auto_increment,
  `userId` int(11) NOT NULL,
  `copyId` int(11) NOT NULL,
  `pointId` varchar(50) NOT NULL,
  `waveNum` varchar(50) NOT NULL,
    `enterCount` int(11) NOT NULL COMMENT '次数',
  `updateAt` bigint(20) NOT NULL COMMENT '更新时间',
    `crossAward` tinyint(5) NOT NULL COMMENT '是否领取通关奖励',
  PRIMARY KEY  (`id`),
  UNIQUE KEY `uid_cid` (`userId`,`copyId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;