-- phpMyAdmin SQL Dump
-- version 2.11.7
-- http://www.phpmyadmin.net
--
-- 主機: localhost:3306
-- 建立日期: Dec 01, 2011, 10:08 AM
-- 伺服器版本: 5.0.77
-- PHP 版本: 5.1.6

SET SQL_MODE="NO_AUTO_VALUE_ON_ZERO";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;

--
-- 資料庫: `yen_camtalk`
--

-- --------------------------------------------------------

--
-- 資料表格式： `caminfo_tb`
--

CREATE TABLE `caminfo_tb` (
  `camID` varchar(50) character set utf8 collate utf8_unicode_ci NOT NULL,
  `camName` varchar(50) character set utf8 collate utf8_unicode_ci NOT NULL,
  `camTalkAc` varchar(50) character set utf8 collate utf8_unicode_ci NOT NULL,
  `camTalkPw` varchar(50) character set utf8 collate utf8_unicode_ci NOT NULL,
  `camIP` varchar(50) character set utf8 collate utf8_unicode_ci NOT NULL,
  `camTalkPort` int(11) NOT NULL default '80',
  `camVideoPort` int(11) NOT NULL default '554',
  `camVideoCode` enum('mpeg4','h264') character set utf8 collate utf8_unicode_ci NOT NULL,
  PRIMARY KEY  (`camID`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

--
-- 列出以下資料庫的數據： `caminfo_tb`
--

INSERT INTO `caminfo_tb` VALUES('0', '測試', 'root', '1234', '192.168.1.225', 80, 554, 'h264');
INSERT INTO `caminfo_tb` VALUES('97', '禾聯碩展示室IPCAM_1', 'root', 'pass', '60.250.57.155', 80, 554, 'h264');
INSERT INTO `caminfo_tb` VALUES('88', 'John''s Home IP-cam', 'admin', '1234', '220.133.51.208', 80, 554, 'mpeg4');
INSERT INTO `caminfo_tb` VALUES('29', '悅明達內部測試', 'root', '1234', '111.240.147.11', 8090, 554, 'h264');
INSERT INTO `caminfo_tb` VALUES('55', 'Steven''s Office 魚缸', 'admin', 'admin', '60.251.125.164', 80, 554, 'h264');

-- --------------------------------------------------------

--
-- 資料表格式： `camlist_tb`
--

CREATE TABLE `camlist_tb` (
  `camID` varchar(50) collate utf8_unicode_ci NOT NULL,
  `userMail` varchar(50) collate utf8_unicode_ci NOT NULL,
  `camAvailable` enum('true','false') collate utf8_unicode_ci NOT NULL default 'true' COMMENT '該筆是否有效',
  PRIMARY KEY  (`camID`,`userMail`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

--
-- 列出以下資料庫的數據： `camlist_tb`
--

INSERT INTO `camlist_tb` VALUES('0', '123@123', 'true');
INSERT INTO `camlist_tb` VALUES('29', '123@123', 'true');
INSERT INTO `camlist_tb` VALUES('29', '1234@1234', 'true');
INSERT INTO `camlist_tb` VALUES('29', 'david@avadesign.com.tw', 'true');
INSERT INTO `camlist_tb` VALUES('29', 'jeff@heran.com.tw', 'true');
INSERT INTO `camlist_tb` VALUES('29', 'john@avadesign.com.tw', 'true');
INSERT INTO `camlist_tb` VALUES('29', 'pad1@avadesign.com.tw', 'true');
INSERT INTO `camlist_tb` VALUES('29', 'pad2@avadesign.com.tw', 'true');
INSERT INTO `camlist_tb` VALUES('29', 'rosesdssp@gmail.com', 'true');
INSERT INTO `camlist_tb` VALUES('29', 'steven@andromeda.com.tw', 'true');
INSERT INTO `camlist_tb` VALUES('88', 'rosesdssp@gmail.com', 'true');
INSERT INTO `camlist_tb` VALUES('97', '123@123', 'true');
INSERT INTO `camlist_tb` VALUES('97', 'atsai@heran.com.tw', 'true');
INSERT INTO `camlist_tb` VALUES('97', 'david@avadesign.com.tw', 'true');
INSERT INTO `camlist_tb` VALUES('97', 'demo@heran.com.tw', 'true');
INSERT INTO `camlist_tb` VALUES('97', 'jeff@heran.com.tw', 'true');
INSERT INTO `camlist_tb` VALUES('97', 'john@avadesign.com.tw', 'true');
INSERT INTO `camlist_tb` VALUES('97', 'jun@heran.com.tw', 'true');
INSERT INTO `camlist_tb` VALUES('97', 'pad1@avadesign.com.tw', 'true');
INSERT INTO `camlist_tb` VALUES('97', 'pad2@avadesign.com.tw', 'true');
INSERT INTO `camlist_tb` VALUES('97', 'ployshen@heran.com.tw', 'true');
INSERT INTO `camlist_tb` VALUES('97', 'rosesdssp@gmail.com', 'true');
INSERT INTO `camlist_tb` VALUES('97', 'steven@heran.com.tw', 'true');

-- --------------------------------------------------------

--
-- 資料表格式： `event_log`
--

CREATE TABLE `event_log` (
  `userMail` varchar(50) character set utf8 collate utf8_unicode_ci NOT NULL,
  `camID` varchar(50) character set utf8 collate utf8_unicode_ci NOT NULL,
  `eventDate` date NOT NULL,
  `eventTime` time NOT NULL,
  `eventType` enum('md') NOT NULL,
  `eventAvailable` enum('true','false') character set utf8 collate utf8_unicode_ci NOT NULL default 'true',
  PRIMARY KEY  (`userMail`,`camID`,`eventTime`,`eventDate`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- 列出以下資料庫的數據： `event_log`
--


-- --------------------------------------------------------

--
-- 資料表格式： `user_log`
--

CREATE TABLE `user_log` (
  `logNo` int(10) unsigned zerofill NOT NULL auto_increment,
  `userMail` varchar(50) character set utf8 collate utf8_unicode_ci NOT NULL,
  `logTime` datetime NOT NULL,
  `logType` enum('login','logout') character set utf8 collate utf8_unicode_ci NOT NULL,
  `logEquip` enum('phone','pc','tv') character set utf8 collate utf8_unicode_ci NOT NULL,
  `logIP` varchar(25) character set utf8 collate utf8_unicode_ci NOT NULL,
  PRIMARY KEY  (`logNo`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 AUTO_INCREMENT=1 ;

--
-- 列出以下資料庫的數據： `user_log`
--


-- --------------------------------------------------------

--
-- 資料表格式： `user_tb`
--

CREATE TABLE `user_tb` (
  `userMail` varchar(50) character set utf8 collate utf8_unicode_ci NOT NULL,
  `userPwd` varchar(50) character set utf8 collate utf8_unicode_ci NOT NULL,
  `user_md_set` enum('on','off') character set utf8 collate utf8_unicode_ci NOT NULL default 'off' COMMENT 'motion_detection_set',
  PRIMARY KEY  (`userMail`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

--
-- 列出以下資料庫的數據： `user_tb`
--

INSERT INTO `user_tb` VALUES('123@123', '123', 'off');
INSERT INTO `user_tb` VALUES('1234@1234', '123', 'on');
INSERT INTO `user_tb` VALUES('demo@heran.com.tw', '0926484075', 'off');
INSERT INTO `user_tb` VALUES('david@avadesign.com.tw', '0911577006', 'off');
INSERT INTO `user_tb` VALUES('atsai@heran.com.tw', '0911869358', 'off');
INSERT INTO `user_tb` VALUES('steven@heran.com.tw', '0921888999', 'off');
INSERT INTO `user_tb` VALUES('jeff@heran.com.tw', '0932272862', 'off');
INSERT INTO `user_tb` VALUES('ployshen@heran.com.tw', '0934028298', 'off');
INSERT INTO `user_tb` VALUES('jun@heran.com.tw', '0952373787', 'off');
INSERT INTO `user_tb` VALUES('12345@12345', '123', 'off');
INSERT INTO `user_tb` VALUES('john@avadesign.com.tw', '0937462495', 'off');
INSERT INTO `user_tb` VALUES('steven@andromeda.com.tw', '0936140192', 'off');
INSERT INTO `user_tb` VALUES('rosesdssp@gmail.com', '0936975951', 'on');
INSERT INTO `user_tb` VALUES('pad1@avadesign.com.tw', '0937462495', 'off');
INSERT INTO `user_tb` VALUES('pad2@avadesign.com.tw', '0937462495', 'off');
INSERT INTO `user_tb` VALUES('dennykai@heran.com.tw', '0939076959', 'off');
INSERT INTO `user_tb` VALUES('dennykao@heran.com.tw', '0939076959', 'off');
INSERT INTO `user_tb` VALUES('jeffery@heran.com.tw', '0987792163', 'off');
