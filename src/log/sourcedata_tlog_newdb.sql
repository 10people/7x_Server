-- phpMyAdmin SQL Dump
-- version 3.3.7
-- http://www.phpmyadmin.net
--
-- 主机: localhost
-- 生成日期: 2015 年 07 月 23 日 02:57
-- 服务器版本: 5.1.54
-- PHP 版本: 5.3.9

SET SQL_MODE="NO_AUTO_VALUE_ON_ZERO";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;

--
-- 数据库: `sourcedata_tlog_newdb`
--

-- --------------------------------------------------------

--
-- 表的结构 `GameSvrState`
--

CREATE TABLE IF NOT EXISTS `GameSvrState` (
  `dtEventTime` datetime NOT NULL,
  `vGameIP` varchar(32) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- 表的结构 `ItemFlow`
--

CREATE TABLE IF NOT EXISTS `ItemFlow` (
  `GameSvrId` varchar(25) NOT NULL,
  `dtEventTime` datetime NOT NULL,
  `vGameAppid` varchar(32) NOT NULL,
  `PlatID` int(11) NOT NULL DEFAULT '0',
  `ZoneID` int(11) NOT NULL DEFAULT '0',
  `vopenid` char(64) NOT NULL,
  `Level` int(11) NOT NULL,
  `Sequence` int(11) NOT NULL,
  `iGoodsType` int(11) NOT NULL,
  `iGoodsId` int(11) NOT NULL,
  `Count` int(11) NOT NULL,
  `AfterCount` int(11) NOT NULL,
  `Reason` int(11) NOT NULL,
  `SubReason` int(11) NOT NULL,
  `iMoney` int(11) NOT NULL,
  `iMoneyType` int(11) NOT NULL,
  `AddOrReduce` int(11) NOT NULL,
  `LoginChannel` int(11) NOT NULL DEFAULT '0',
  `RoleId` char(64) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- 表的结构 `MoneyFlow`
--

CREATE TABLE IF NOT EXISTS `MoneyFlow` (
  `GameSvrId` varchar(25) NOT NULL,
  `dtEventTime` datetime NOT NULL,
  `vGameAppid` varchar(32) NOT NULL,
  `PlatID` int(11) NOT NULL DEFAULT '0',
  `ZoneID` int(11) NOT NULL DEFAULT '0',
  `vopenid` char(64) NOT NULL,
  `Sequence` int(11) NOT NULL,
  `Level` int(11) NOT NULL,
  `AfterMoney` int(11) NOT NULL,
  `iMoney` int(11) NOT NULL,
  `Reason` int(11) NOT NULL,
  `SubReason` int(11) NOT NULL,
  `AddOrReduce` int(11) NOT NULL,
  `iMoneyType` int(11) NOT NULL,
  `LoginChannel` int(11) NOT NULL DEFAULT '0',
  `RoleId` char(64) NOT NULL,
  `Rmb` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- 表的结构 `PlayerCrash`
--

CREATE TABLE IF NOT EXISTS `PlayerCrash` (
  `GameSvrId` varchar(25) NOT NULL,
  `dtEventTime` datetime NOT NULL,
  `vGameAppid` varchar(32) NOT NULL,
  `PlatID` int(11) NOT NULL DEFAULT '0',
  `ZoneID` int(11) NOT NULL DEFAULT '0',
  `vopenid` char(64) NOT NULL,
  `Level` int(11) NOT NULL,
  `PlayerFriendsNum` int(11) NOT NULL,
  `ClientVersion` varchar(64) NOT NULL,
  `SystemSoftware` varchar(64) NOT NULL,
  `SystemHardware` varchar(64) NOT NULL,
  `TelecomOper` varchar(64) NOT NULL,
  `Network` varchar(64) NOT NULL,
  `ScreenWidth` int(11) NOT NULL DEFAULT '0',
  `ScreenHight` int(11) NOT NULL DEFAULT '0',
  `Density` float NOT NULL DEFAULT '0',
  `LoginChannel` int(11) NOT NULL DEFAULT '0',
  `CpuHardware` varchar(64) NOT NULL,
  `Memory` int(11) NOT NULL DEFAULT '0',
  `GLRender` varchar(64) NOT NULL,
  `GLVersion` varchar(64) NOT NULL,
  `DeviceId` varchar(64) NOT NULL,
  `RoleId` char(64) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- 表的结构 `PlayerExpFlow`
--

CREATE TABLE IF NOT EXISTS `PlayerExpFlow` (
  `GameSvrId` varchar(25) NOT NULL,
  `dtEventTime` datetime NOT NULL,
  `vGameAppid` varchar(32) NOT NULL,
  `PlatID` int(11) NOT NULL DEFAULT '0',
  `ZoneID` int(11) NOT NULL DEFAULT '0',
  `vopenid` char(64) NOT NULL,
  `ExpChange` int(11) NOT NULL,
  `BeforeLevel` int(11) NOT NULL,
  `AfterLevel` int(11) NOT NULL,
  `Time` int(11) NOT NULL,
  `Reason` int(11) NOT NULL,
  `SubReason` int(11) NOT NULL,
  `LoginChannel` int(11) NOT NULL DEFAULT '0',
  `RoleId` char(64) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- 表的结构 `PlayerLogin`
--

CREATE TABLE IF NOT EXISTS `PlayerLogin` (
  `GameSvrId` varchar(25) NOT NULL,
  `dtEventTime` datetime NOT NULL,
  `vGameAppid` varchar(32) NOT NULL,
  `PlatID` int(11) NOT NULL DEFAULT '0',
  `ZoneID` int(11) NOT NULL DEFAULT '0',
  `vopenid` char(64) NOT NULL,
  `Level` int(11) NOT NULL,
  `PlayerFriendsNum` int(11) NOT NULL,
  `ClientVersion` varchar(64) NOT NULL,
  `SystemSoftware` varchar(64) NOT NULL,
  `SystemHardware` varchar(64) NOT NULL,
  `TelecomOper` varchar(64) NOT NULL,
  `Network` varchar(64) NOT NULL,
  `ScreenWidth` int(11) NOT NULL DEFAULT '0',
  `ScreenHight` int(11) NOT NULL DEFAULT '0',
  `Density` float NOT NULL DEFAULT '0',
  `LoginChannel` int(11) NOT NULL DEFAULT '0',
  `CpuHardware` varchar(64) NOT NULL,
  `Memory` int(11) NOT NULL DEFAULT '0',
  `GLRender` varchar(64) NOT NULL,
  `GLVersion` varchar(64) NOT NULL,
  `DeviceId` varchar(64) NOT NULL,
  `RoleId` char(64) NOT NULL,
  `Ip` varchar(64) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- 表的结构 `PlayerLogout`
--

CREATE TABLE IF NOT EXISTS `PlayerLogout` (
  `GameSvrId` varchar(25) NOT NULL,
  `dtEventTime` datetime NOT NULL,
  `vGameAppid` varchar(32) NOT NULL,
  `PlatID` int(11) NOT NULL DEFAULT '0',
  `ZoneID` int(11) NOT NULL DEFAULT '0',
  `vopenid` char(64) NOT NULL,
  `OnlineTime` int(11) NOT NULL,
  `Level` int(11) NOT NULL,
  `PlayerFriendsNum` int(11) NOT NULL,
  `ClientVersion` varchar(64) NOT NULL,
  `SystemSoftware` varchar(64) NOT NULL,
  `SystemHardware` varchar(64) NOT NULL,
  `TelecomOper` varchar(64) NOT NULL,
  `Network` varchar(64) NOT NULL,
  `ScreenWidth` int(11) NOT NULL DEFAULT '0',
  `ScreenHight` int(11) NOT NULL DEFAULT '0',
  `Density` float NOT NULL DEFAULT '0',
  `LoginChannel` int(11) NOT NULL DEFAULT '0',
  `CpuHardware` varchar(64) NOT NULL,
  `Memory` int(11) NOT NULL DEFAULT '0',
  `GLRender` varchar(64) NOT NULL,
  `GLVersion` varchar(64) NOT NULL,
  `DeviceId` varchar(64) NOT NULL,
  `RoleId` char(64) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- 表的结构 `PlayerOnline`
--

CREATE TABLE IF NOT EXISTS `PlayerOnline` (
  `num` int(11) NOT NULL DEFAULT '0',
  `dtEventTime` datetime NOT NULL,
  `PlatID` int(11) NOT NULL DEFAULT '0',
  `GameSvrId` varchar(25) NOT NULL,
  `LoginChannel` int(11) NOT NULL DEFAULT '0',
  `RoleId` char(64) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- 表的结构 `PlayerRegister`
--

CREATE TABLE IF NOT EXISTS `PlayerRegister` (
  `GameSvrId` varchar(25) NOT NULL,
  `dtEventTime` datetime NOT NULL,
  `vGameAppid` varchar(32) NOT NULL,
  `PlatID` int(11) NOT NULL DEFAULT '0',
  `ZoneID` int(11) NOT NULL DEFAULT '0',
  `vopenid` char(64) NOT NULL,
  `ClientVersion` varchar(64) NOT NULL,
  `SystemSoftware` varchar(64) NOT NULL,
  `SystemHardware` varchar(64) NOT NULL,
  `TelecomOper` varchar(64) NOT NULL,
  `Network` varchar(64) NOT NULL,
  `ScreenWidth` int(11) NOT NULL DEFAULT '0',
  `ScreenHight` int(11) NOT NULL DEFAULT '0',
  `Density` float NOT NULL DEFAULT '0',
  `RegChannel` int(11) NOT NULL DEFAULT '0',
  `CpuHardware` varchar(64) NOT NULL,
  `Memory` int(11) NOT NULL DEFAULT '0',
  `GLRender` varchar(64) NOT NULL,
  `GLVersion` varchar(64) NOT NULL,
  `DeviceId` varchar(64) NOT NULL,
  `RoleId` char(64) NOT NULL,
  `Ip` varchar(64) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- 表的结构 `RoundFlow`
--

CREATE TABLE IF NOT EXISTS `RoundFlow` (
  `GameSvrId` varchar(25) NOT NULL,
  `dtEventTime` datetime NOT NULL,
  `vGameAppid` varchar(32) NOT NULL,
  `PlatID` int(11) NOT NULL DEFAULT '0',
  `ZoneID` int(11) NOT NULL DEFAULT '0',
  `vopenid` char(64) NOT NULL,
  `BattleID` int(11) NOT NULL,
  `BattleType` int(11) NOT NULL,
  `RoundScore` int(11) NOT NULL,
  `RoundTime` int(11) NOT NULL,
  `Result` int(11) NOT NULL,
  `Rank` int(11) NOT NULL,
  `Gold` int(11) NOT NULL,
  `LoginChannel` int(11) NOT NULL DEFAULT '0',
  `RoleId` char(64) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- 表的结构 `SnsFlow`
--

CREATE TABLE IF NOT EXISTS `SnsFlow` (
  `GameSvrId` varchar(25) NOT NULL,
  `dtEventTime` datetime NOT NULL,
  `vGameAppid` varchar(32) NOT NULL,
  `PlatID` int(11) NOT NULL DEFAULT '0',
  `ZoneID` int(11) NOT NULL DEFAULT '0',
  `ActorOpenID` varchar(64) NOT NULL,
  `RecNum` int(11) NOT NULL,
  `Count` int(11) NOT NULL,
  `SNSType` int(11) NOT NULL,
  `SNSSubType` int(11) NOT NULL,
  `LoginChannel` int(11) NOT NULL DEFAULT '0',
  `RoleId` char(64) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
