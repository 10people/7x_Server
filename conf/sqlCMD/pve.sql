
##### pve关卡模版
CREATE TABLE `CheckPointsTemplet` (
  `id` int(11) NOT NULL auto_increment COMMENT '自增id',
  `bigPointId` int(11) NOT NULL COMMENT '大关卡id',
  `bigPointName` varchar(100) NOT NULL COMMENT '大关卡名字',
  `smallPointId` int(11) NOT NULL COMMENT '关卡小id',
  `smallPointName` varchar(100) NOT NULL COMMENT '小关卡id',
  `waveNum` tinyint(5) NOT NULL COMMENT '数波',
  `hpCost` int(11) NOT NULL COMMENT '体力消耗',
  `smallPointdescription` varchar(300) NOT NULL COMMENT '关卡描述',
  `imageId` int(11) NOT NULL,
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=61 DEFAULT CHARSET=utf8;



##### pve关卡敌人
CREATE TABLE `PvePointEnemy` (
  `id` int(11) NOT NULL COMMENT '主键',
  `pointId` int(11) NOT NULL COMMENT '对应checkPointsTemplet中的id',
  `enemyId` int(11) NOT NULL COMMENT '敌人',
  `level` int(11) NOT NULL COMMENT '敌人等级',
  `row` int(11) NOT NULL COMMENT '行',
  `col` int(11) NOT NULL COMMENT '列',
  `waveNum` tinyint(5) NOT NULL COMMENT '第几波',
  `awardId` int(11) NOT NULL COMMENT '掉落的id',
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

#####用户pve记录
CREATE TABLE `UserPveRecord` (
  `userId` int(11) NOT NULL COMMENT '户用id',
  `pointId` int(11) NOT NULL COMMENT '关卡id',
  `lastPointId` int(11) NOT NULL COMMENT '正在打的关卡id',
  `formId` int(11) NOT NULL COMMENT '默认阵形',
  `awards` varchar(500) NOT NULL COMMENT '奖励',
  `pointStar` varchar(500) NOT NULL COMMENT '关卡星级',
  `helpUserId` int(11) NOT NULL COMMENT '互助好友id',
  `isPass` int(11) NOT NULL COMMENT '是否通关',
  `challengeCount` int(11) NOT NULL COMMENT '战挑次数',
  `costChallengeCount` int(11) NOT NULL COMMENT '花钱的挑战次数',
  `updateAt` bigint(20) NOT NULL COMMENT '更新时间',
  PRIMARY KEY  (`userId`),
  UNIQUE KEY `index_uid` (`userId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;