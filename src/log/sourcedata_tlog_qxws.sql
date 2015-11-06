/*
Navicat MySQL Data Transfer

Source Server         : 192.168.3.90
Source Server Version : 50154
Source Host           : 192.168.3.90:3306
Source Database       : sourcedata_tlog_newdb

Target Server Type    : MYSQL
Target Server Version : 50154
File Encoding         : 65001

Date: 2015-08-04 17:51:13
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for Challenge
-- ----------------------------
DROP TABLE IF EXISTS `Challenge`;
CREATE TABLE `Challenge` (
  `GameSvrId` varchar(25) NOT NULL,
  `vopenid` varchar(64) NOT NULL,
  `RoleId` varchar(64) NOT NULL,
  `RoleName` varchar(64) NOT NULL,
  `ZoneID` int(11) NOT NULL DEFAULT '0',
  `OpposName` varchar(64) NOT NULL,
  `OpposId` int(11) NOT NULL,
  `Win` int(11) NOT NULL,
  `OldRank` int(11) NOT NULL,
  `Rank` int(11) NOT NULL,
  `dtEventTime` datetime NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for ChallengeAward
-- ----------------------------
DROP TABLE IF EXISTS `ChallengeAward`;
CREATE TABLE `ChallengeAward` (
  `GameSvrId` varchar(25) NOT NULL,
  `vopenid` varchar(64) NOT NULL,
  `RoleId` varchar(64) NOT NULL,
  `RoleName` varchar(64) NOT NULL,
  `ZoneID` int(11) NOT NULL DEFAULT '0',
  `AwardType` int(11) NOT NULL,
  `Num` int(11) NOT NULL,
  `dtEventTime` datetime NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for ChallengeExchange
-- ----------------------------
DROP TABLE IF EXISTS `ChallengeExchange`;
CREATE TABLE `ChallengeExchange` (
  `GameSvrId` varchar(25) NOT NULL,
  `vopenid` varchar(64) NOT NULL,
  `RoleId` varchar(64) NOT NULL,
  `RoleName` varchar(64) NOT NULL,
  `ZoneID` int(11) NOT NULL DEFAULT '0',
  `iGoodsid` int(11) NOT NULL,
  `iGoods` varchar(64) NOT NULL,
  `Num` int(11) NOT NULL,
  `OldPrestige` int(11) NOT NULL,
  `Prestige` int(11) NOT NULL,
  `dtEventTime` datetime NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for ConveyDart
-- ----------------------------
DROP TABLE IF EXISTS `ConveyDart`;
CREATE TABLE `ConveyDart` (
  `GameSvrId` varchar(25) NOT NULL,
  `vopenid` varchar(64) NOT NULL,
  `RoleId` varchar(64) NOT NULL,
  `RoleName` varchar(64) NOT NULL,
  `ZoneID` int(11) NOT NULL DEFAULT '0',
  `DartType` int(11) NOT NULL,
  `Copper` int(11) NOT NULL,
  `Count` int(11) NOT NULL,
  `dtEventTime` datetime NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for DailyTask
-- ----------------------------
DROP TABLE IF EXISTS `DailyTask`;
CREATE TABLE `DailyTask` (
  `GameSvrId` varchar(25) NOT NULL,
  `vopenid` varchar(64) NOT NULL,
  `RoleId` varchar(64) NOT NULL,
  `RoleName` varchar(64) NOT NULL,
  `ZoneID` int(11) NOT NULL DEFAULT '0',
  `Task` varchar(64) NOT NULL,
  `Taskid` int(11) NOT NULL,
  `dtEventTime` datetime NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for EmailLog
-- ----------------------------
DROP TABLE IF EXISTS `EmailLog`;
CREATE TABLE `EmailLog` (
  `GameSvrId` varchar(25) NOT NULL,
  `LogType` int(11) NOT NULL,
  `vopenid` varchar(64) NOT NULL,
  `RoleId` varchar(64) NOT NULL,
  `RoleName` varchar(64) NOT NULL,
  `ZoneID` int(11) NOT NULL DEFAULT '0',
  `Recopenid` varchar(64) NOT NULL,
  `Recrid` varchar(64) NOT NULL,
  `Recrname` varchar(64) NOT NULL,
  `Title` varchar(128) NOT NULL,
  `Content` text NOT NULL,
  `Attach` text NOT NULL,
  `dtEventTime` datetime NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for EquipLvup
-- ----------------------------
DROP TABLE IF EXISTS `EquipLvup`;
CREATE TABLE `EquipLvup` (
  `GameSvrId` varchar(25) NOT NULL,
  `vopenid` varchar(64) NOT NULL,
  `RoleId` varchar(64) NOT NULL,
  `RoleName` varchar(64) NOT NULL,
  `ZoneID` int(11) NOT NULL DEFAULT '0',
  `EquipId` int(11) NOT NULL,
  `Equips` varchar(64) NOT NULL,
  `oldEquipId` int(11) NOT NULL,
  `oldEquips` varchar(64) NOT NULL,
  `iGoods` varchar(64) NOT NULL,
  `iGoodsnum` int(11) NOT NULL,
  `dtEventTime` datetime NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for EquipRefine
-- ----------------------------
DROP TABLE IF EXISTS `EquipRefine`;
CREATE TABLE `EquipRefine` (
  `GameSvrId` varchar(25) NOT NULL,
  `vopenid` varchar(64) NOT NULL,
  `RoleId` varchar(64) NOT NULL,
  `RoleName` varchar(64) NOT NULL,
  `ZoneID` int(11) NOT NULL DEFAULT '0',
  `EquipId` int(11) NOT NULL,
  `Equips` varchar(64) NOT NULL,
  `BeforeAttr` varchar(64) NOT NULL,
  `Attr` varchar(64) NOT NULL,
  `Money` int(11) NOT NULL,
  `dtEventTime` datetime NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for EquipStrength
-- ----------------------------
DROP TABLE IF EXISTS `EquipStrength`;
CREATE TABLE `EquipStrength` (
  `GameSvrId` varchar(25) NOT NULL,
  `vopenid` varchar(64) NOT NULL,
  `RoleId` varchar(64) NOT NULL,
  `RoleName` varchar(64) NOT NULL,
  `ZoneID` int(11) NOT NULL DEFAULT '0',
  `EquipId` int(11) NOT NULL,
  `Equips` varchar(64) NOT NULL,
  `BeforeLevel` int(11) NOT NULL,
  `EquipLevel` int(11) NOT NULL,
  `Consumes` text NOT NULL,
  `dtEventTime` datetime NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for FineGem
-- ----------------------------
DROP TABLE IF EXISTS `FineGem`;
CREATE TABLE `FineGem` (
  `GameSvrId` varchar(25) NOT NULL,
  `SeekType` int(11) NOT NULL,
  `vopenid` varchar(64) NOT NULL,
  `RoleId` varchar(64) NOT NULL,
  `RoleName` varchar(64) NOT NULL,
  `iGoodsid` int(11) NOT NULL,
  `iGoods` varchar(64) NOT NULL,
  `Num` int(11) NOT NULL,
  `DeliGoods` varchar(64) NOT NULL,
  `DelNum` int(11) NOT NULL,
  `dtEventTime` datetime NOT NULL,
  `ZoneID` int(11) NOT NULL DEFAULT '0'
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for GetExp
-- ----------------------------
DROP TABLE IF EXISTS `GetExp`;
CREATE TABLE `GetExp` (
  `GameSvrId` varchar(25) NOT NULL,
  `vopenid` varchar(64) NOT NULL,
  `RoleId` varchar(64) NOT NULL,
  `RoleName` varchar(64) NOT NULL,
  `ZoneID` int(11) NOT NULL DEFAULT '0',
  `Reason` int(11) NOT NULL DEFAULT '0',
  `Num` varchar(25) NOT NULL,
  `CurNum` int(11) NOT NULL DEFAULT '0',
  `dtEventTime` datetime NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for Guild
-- ----------------------------
DROP TABLE IF EXISTS `Guild`;
CREATE TABLE `Guild` (
  `GameSvrId` varchar(25) NOT NULL,
  `LogType` varchar(16) NOT NULL,
  `vopenid` varchar(64) NOT NULL,
  `RoleId` varchar(64) NOT NULL,
  `RoleName` varchar(64) NOT NULL,
  `ZoneID` int(11) NOT NULL DEFAULT '0',
  `Guildid` int(11) NOT NULL,
  `GuildName` varchar(64) NOT NULL,
  `GuildLv` int(11) NOT NULL,
  `Reason` varchar(64) NOT NULL,
  `dtEventTime` datetime NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for GuildBreak
-- ----------------------------
DROP TABLE IF EXISTS `GuildBreak`;
CREATE TABLE `GuildBreak` (
  `GameSvrId` varchar(25) NOT NULL,
  `vopenid` varchar(64) NOT NULL,
  `RoleId` varchar(64) NOT NULL,
  `RoleName` varchar(64) NOT NULL,
  `ZoneID` int(11) NOT NULL DEFAULT '0',
  `GuildId` int(11) NOT NULL,
  `GuildName` varchar(64) NOT NULL,
  `GuildLv` int(11) NOT NULL,
  `GuildExp` int(11) NOT NULL,
  `GuildBuild` int(11) NOT NULL,
  `dtEventTime` datetime NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for GuildDonate
-- ----------------------------
DROP TABLE IF EXISTS `GuildDonate`;
CREATE TABLE `GuildDonate` (
  `GameSvrId` varchar(25) NOT NULL,
  `vopenid` varchar(64) NOT NULL,
  `RoleId` varchar(64) NOT NULL,
  `RoleName` varchar(64) NOT NULL,
  `ZoneID` int(11) NOT NULL DEFAULT '0',
  `GuildId` int(11) NOT NULL,
  `GuildName` varchar(64) NOT NULL,
  `Tigernum` int(11) NOT NULL,
  `Contribute` int(11) NOT NULL,
  `dtEventTime` datetime NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for GuildOut
-- ----------------------------
DROP TABLE IF EXISTS `GuildOut`;
CREATE TABLE `GuildOut` (
  `GameSvrId` varchar(25) NOT NULL,
  `vopenid` varchar(64) NOT NULL,
  `RoleId` varchar(64) NOT NULL,
  `RoleName` varchar(64) NOT NULL,
  `ZoneID` int(11) NOT NULL DEFAULT '0',
  `GuildId` int(11) NOT NULL,
  `GuildName` varchar(64) NOT NULL,
  `KickOpenid` varchar(64) NOT NULL,
  `KickrId` varchar(64) NOT NULL,
  `KickrName` varchar(64) NOT NULL,
  `dtEventTime` datetime NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for GuildTransfer
-- ----------------------------
DROP TABLE IF EXISTS `GuildTransfer`;
CREATE TABLE `GuildTransfer` (
  `GameSvrId` varchar(25) NOT NULL,
  `vopenid` varchar(64) NOT NULL,
  `RoleId` varchar(64) NOT NULL,
  `RoleName` varchar(64) NOT NULL,
  `ZoneID` int(11) NOT NULL DEFAULT '0',
  `GuildId` int(11) NOT NULL,
  `GuildName` varchar(64) NOT NULL,
  `oldOpenid` varchar(64) NOT NULL,
  `oldRid` varchar(64) NOT NULL,
  `oldRname` varchar(64) NOT NULL,
  `dtEventTime` datetime NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for HeroBattle
-- ----------------------------
DROP TABLE IF EXISTS `HeroBattle`;
CREATE TABLE `HeroBattle` (
  `GameSvrId` varchar(25) NOT NULL,
  `vopenid` varchar(64) NOT NULL,
  `RoleId` varchar(64) NOT NULL,
  `RoleName` varchar(64) NOT NULL,
  `ZoneID` int(11) NOT NULL DEFAULT '0',
  `GateId` int(11) NOT NULL,
  `Gates` varchar(64) NOT NULL,
  `Win` int(11) NOT NULL,
  `Num` int(11) NOT NULL,
  `dtEventTime` datetime NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for KillRebelArmy
-- ----------------------------
DROP TABLE IF EXISTS `KillRebelArmy`;
CREATE TABLE `KillRebelArmy` (
  `GameSvrId` varchar(25) NOT NULL,
  `vopenid` varchar(64) NOT NULL,
  `RoleId` varchar(64) NOT NULL,
  `RoleName` varchar(64) NOT NULL,
  `ZoneID` int(11) NOT NULL DEFAULT '0',
  `Gates` varchar(64) NOT NULL,
  `GateId` int(11) NOT NULL,
  `Win` int(11) NOT NULL,
  `Count` int(11) NOT NULL,
  `AwardType` int(11) NOT NULL,
  `Num` int(11) NOT NULL,
  `dtEventTime` datetime NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for KillRobber
-- ----------------------------
DROP TABLE IF EXISTS `KillRobber`;
CREATE TABLE `KillRobber` (
  `GameSvrId` varchar(25) NOT NULL,
  `vopenid` varchar(64) NOT NULL,
  `RoleId` varchar(64) NOT NULL,
  `RoleName` varchar(64) NOT NULL,
  `ZoneID` int(11) NOT NULL DEFAULT '0',
  `Gates` varchar(64) NOT NULL,
  `Gateid` int(11) NOT NULL,
  `Win` int(11) NOT NULL,
  `Count` int(11) NOT NULL,
  `Awardtype` int(11) NOT NULL,
  `Num` int(11) NOT NULL,
  `dtEventTime` datetime NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for KingChange
-- ----------------------------
DROP TABLE IF EXISTS `KingChange`;
CREATE TABLE `KingChange` (
  `GameSvrId` varchar(25) NOT NULL,
  `vopenid` varchar(64) NOT NULL,
  `RoleId` int(11) NOT NULL,
  `oldName` varchar(64) NOT NULL,
  `newName` varchar(64) NOT NULL,
  `dtEventTime` varchar(64) NOT NULL,
  `ZoneID` int(11) NOT NULL DEFAULT '0'
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for KingLvup
-- ----------------------------
DROP TABLE IF EXISTS `KingLvup`;
CREATE TABLE `KingLvup` (
  `GameSvrId` varchar(25) NOT NULL,
  `vopenid` varchar(64) NOT NULL,
  `RoleId` int(11) NOT NULL,
  `RoleName` varchar(64) NOT NULL,
  `Level` varchar(64) NOT NULL,
  `Exp` varchar(64) NOT NULL,
  `dtEventTime` varchar(64) NOT NULL,
  `ZoneID` int(11) NOT NULL DEFAULT '0'
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for KingTalent
-- ----------------------------
DROP TABLE IF EXISTS `KingTalent`;
CREATE TABLE `KingTalent` (
  `GameSvrId` varchar(25) NOT NULL,
  `vopenid` varchar(64) NOT NULL,
  `RoleId` varchar(64) NOT NULL,
  `RoleName` varchar(64) NOT NULL,
  `ZoneID` int(11) NOT NULL DEFAULT '0',
  `Talentid` int(11) NOT NULL DEFAULT '0',
  `Talents` varchar(64) NOT NULL DEFAULT '0',
  `Level` int(11) NOT NULL,
  `VigourType` int(11) NOT NULL DEFAULT '0',
  `Num` int(11) NOT NULL DEFAULT '0',
  `dtEventTime` datetime NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for LootDart
-- ----------------------------
DROP TABLE IF EXISTS `LootDart`;
CREATE TABLE `LootDart` (
  `GameSvrId` varchar(25) NOT NULL,
  `vopenid` varchar(64) NOT NULL,
  `RoleId` varchar(64) NOT NULL,
  `RoleName` varchar(64) NOT NULL,
  `RobedOpenid` varchar(64) NOT NULL,
  `RobedRid` varchar(64) NOT NULL,
  `Robedrname` varchar(64) NOT NULL,
  `Copper` int(11) NOT NULL,
  `Count` int(11) NOT NULL,
  `dtEventTime` datetime NOT NULL,
  `ZoneID` int(11) NOT NULL DEFAULT '0'
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for LootRich
-- ----------------------------
DROP TABLE IF EXISTS `LootRich`;
CREATE TABLE `LootRich` (
  `GameSvrId` varchar(25) NOT NULL,
  `vopenid` varchar(64) NOT NULL,
  `RoleId` varchar(64) NOT NULL,
  `RoleName` varchar(64) NOT NULL,
  `ZoneID` int(11) NOT NULL DEFAULT '0',
  `Gates` varchar(64) NOT NULL,
  `GateId` int(11) NOT NULL,
  `Win` int(11) NOT NULL,
  `Count` int(11) NOT NULL,
  `Copper` int(11) NOT NULL,
  `dtEventTime` datetime NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for Pawn
-- ----------------------------
DROP TABLE IF EXISTS `Pawn`;
CREATE TABLE `Pawn` (
  `GameSvrId` varchar(25) NOT NULL,
  `vopenid` varchar(64) NOT NULL,
  `RoleId` varchar(64) NOT NULL,
  `RoleName` varchar(64) NOT NULL,
  `ZoneID` int(11) NOT NULL DEFAULT '0',
  `iGoodsid` int(11) NOT NULL,
  `iGoods` varchar(64) NOT NULL,
  `Num` int(11) NOT NULL,
  `Renum` int(11) NOT NULL,
  `Money` int(11) NOT NULL,
  `dtEventTime` datetime NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for PhysicalPower
-- ----------------------------
DROP TABLE IF EXISTS `PhysicalPower`;
CREATE TABLE `PhysicalPower` (
  `GameSvrId` varchar(25) NOT NULL,
  `vopenid` varchar(64) NOT NULL,
  `RoleId` varchar(64) NOT NULL,
  `RoleName` varchar(64) NOT NULL,
  `ZoneID` int(11) NOT NULL DEFAULT '0',
  `OldPower` int(11) NOT NULL,
  `Num` int(11) NOT NULL,
  `Power` int(11) NOT NULL,
  `Reason` varchar(64) NOT NULL,
  `dtEventTime` datetime NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for ShopBuy
-- ----------------------------
DROP TABLE IF EXISTS `ShopBuy`;
CREATE TABLE `ShopBuy` (
  `GameSvrId` varchar(25) NOT NULL,
  `LogType` int(11) NOT NULL,
  `vopenid` varchar(64) NOT NULL,
  `RoleId` varchar(64) NOT NULL,
  `RoleName` varchar(64) NOT NULL,
  `ZoneID` int(11) NOT NULL DEFAULT '0',
  `iGoodsid` int(11) NOT NULL,
  `iGoods` varchar(64) NOT NULL,
  `BuyNum` int(11) NOT NULL,
  `Money` int(11) NOT NULL,
  `RemainMoney` int(11) NOT NULL,
  `dtEventTime` datetime NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for Worship
-- ----------------------------
DROP TABLE IF EXISTS `Worship`;
CREATE TABLE `Worship` (
  `GameSvrId` varchar(25) NOT NULL,
  `wsType` varchar(25) NOT NULL,
  `vopenid` varchar(64) NOT NULL,
  `RoleId` varchar(64) NOT NULL,
  `RoleName` varchar(64) NOT NULL,
  `ZoneID` int(11) NOT NULL DEFAULT '0',
  `Consumes` text NOT NULL,
  `dtEventTime` datetime NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
