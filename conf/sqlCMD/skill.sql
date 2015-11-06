
##### 技能模版
CREATE TABLE `Skill` (
  `id` int(11) NOT NULL COMMENT '模板Id',
  `skillId` int(11) NOT NULL COMMENT '技能Id',
  `name` varchar(20) NOT NULL COMMENT '技能名称',
  `description` varchar(100) NOT NULL COMMENT '描述',
  `level` int(11) NOT NULL COMMENT '技能等级',
  `icon` varchar(200) NOT NULL COMMENT '技能图标',
  `initCoolingTime` int(11) NOT NULL COMMENT '初始冷却',
  `coolingTime` int(11) NOT NULL COMMENT '冷却时间',
  `targetType_1` int(11) NOT NULL COMMENT '释放目标类型1',
  `targetRange_1` varchar(200) NOT NULL COMMENT '释放范围1',
  `rangeType_1` int(11) NOT NULL COMMENT '以谁为0.0点',
  `targetNum_1` int(11) NOT NULL COMMENT '目标数量1',
  `affectType_1` int(11) NOT NULL COMMENT '影响类型1',
  `affectValueType_1` int(11) NOT NULL COMMENT '数值类型1',
  `affectValue_1` float NOT NULL COMMENT '影响数值1',
  `lastTime_1` int(11) NOT NULL COMMENT '持续回合数1',
  `hasTarget_2` int(11) NOT NULL COMMENT '有没有第二效果',
  `premiseType_2` int(11) NOT NULL COMMENT '前提条件类型',
  `premiseValue_2` varchar(10) NOT NULL COMMENT '前提条件数值',
  `premiseTarget_2` int(11) NOT NULL COMMENT '前提条件判定者',
  `targetType_2` int(11) NOT NULL COMMENT '释放目标类型2',
  `targetRange_2` varchar(200) NOT NULL COMMENT '释放范围2',
  `rangeType_2` int(11) NOT NULL COMMENT '以谁为0.0点',
  `targetNum_2` int(11) NOT NULL COMMENT '目标数量2',
  `affectType_2` int(11) NOT NULL COMMENT '影响类型2',
  `affectValueType_2` int(11) NOT NULL COMMENT '数值类型2',
  `affectValue_2` varchar(200) NOT NULL COMMENT '影响数值2',
  `lastTime_2` int(11) NOT NULL COMMENT '持续回合数2',
  `triggerPercent` int(11) NOT NULL COMMENT '技能触发几率',
  `affectId_1` int(11) NOT NULL,
  `affectId_2` int(11) NOT NULL,
  `skillAffectId_1` int(11) NOT NULL,
  `skillAffectId_2` int(11) NOT NULL,
  `skillBulletAffect` int(11) NOT NULL,
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;



