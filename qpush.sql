# ************************************************************
# Sequel Pro SQL dump
# Version 4096
#
# http://www.sequelpro.com/
# http://code.google.com/p/sequel-pro/
#
# Host: (MySQL 10.0.19-MariaDB-log)
# Database: qpush
# Generation Time: 2015-08-07 03:40:39 +0000
# ************************************************************


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;


# Dump of table client
# ------------------------------------------------------------

DROP TABLE IF EXISTS `client`;

CREATE TABLE `client` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '记录id',
  `productId` int(11) DEFAULT NULL COMMENT '产品id. 关联到product表',
  `userId` varchar(32) DEFAULT NULL COMMENT '用户id',
  `deviceToken` varchar(255) DEFAULT NULL COMMENT '设备推送Token',
  `createAt` datetime DEFAULT NULL COMMENT '创建日期',
  `statusId` tinyint(2) NOT NULL DEFAULT '1' COMMENT '客户端状态(1:在线,0:下线,2:Sleep)',
  `typeId` int(11) DEFAULT NULL COMMENT '类别id(1:iPhone,2:iPad,3:Android)',
  `lastSendAt` int(11) DEFAULT NULL COMMENT '最后发送日期',
  `lastOnline` int(11) DEFAULT NULL COMMENT '最后在线日期',
  `badge` int(11) NOT NULL DEFAULT '0' COMMENT '提醒小红点',
  PRIMARY KEY (`id`),
  UNIQUE KEY `ix_client_userId` (`userId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='客户端信息表';



# Dump of table payload
# ------------------------------------------------------------

DROP TABLE IF EXISTS `payload`;

CREATE TABLE `payload` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '记录id',
  `title` varchar(500) DEFAULT NULL COMMENT '标题',
  `badge` int(11) DEFAULT NULL COMMENT '提示总数',
  `extras` varchar(500) DEFAULT NULL COMMENT '补充信息',
  `sound` varchar(10) DEFAULT NULL COMMENT '提示声音',
  `productId` int(11) DEFAULT NULL COMMENT '产品id',
  `totalUsers` int(11) DEFAULT '0' COMMENT '推送到总用户数',
  `createAt` int(11) DEFAULT NULL COMMENT '创建日期',
  `statusId` tinyint(2) DEFAULT NULL COMMENT '推送状态',
  `broadcast` tinyint(1) DEFAULT NULL COMMENT '是否是广播',
  `sentDate` int(11) DEFAULT NULL COMMENT '推送日期',
  `offlineMode` tinyint(1) NOT NULL DEFAULT '0' COMMENT '离线时的推送模式',
  `toMode` tinyint(1) NOT NULL DEFAULT '0' COMMENT '接受人模式(0:所有,1:在线)',
  PRIMARY KEY (`id`),
  KEY `ix_payload_list` (`productId`,`broadcast`,`statusId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='推送消息表';



# Dump of table payload_client
# ------------------------------------------------------------

DROP TABLE IF EXISTS `payload_client`;

CREATE TABLE `payload_client` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `payloadId` bigint(20) NOT NULL COMMENT '推送消息id',
  `userId` varchar(50) NOT NULL DEFAULT '' COMMENT '需要推送到用户id',
  `productId` int(11) DEFAULT NULL COMMENT '关联的产品id',
  `statusId` smallint(1) NOT NULL DEFAULT '0' COMMENT '推送状态(0: 推送中, 2: 成功, 3: 失败)',
  `onlineMode` tinyint(1) NOT NULL DEFAULT '0' COMMENT '上线后失败消息处理(0:忽略,1:发送)',
  `createTime` int(11) DEFAULT NULL COMMENT '记录时间',
  `tryLimit` smallint(1) NOT NULL DEFAULT '3' COMMENT '最多尝试次数',
  `errorId` smallint(2) DEFAULT NULL COMMENT '错误标示',
  `errorMsg` varchar(1000) DEFAULT NULL COMMENT '错误信息',
  PRIMARY KEY (`id`),
  KEY `ix_payload_client_pu_resend` (`productId`,`userId`,`onlineMode`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='需要推送到用户表';



# Dump of table payload_history
# ------------------------------------------------------------

DROP TABLE IF EXISTS `payload_history`;

CREATE TABLE `payload_history` (
  `id` bigint(20) NOT NULL COMMENT '推送消息id',
  `userId` int(11) NOT NULL COMMENT '用户id',
  `productId` int(11) DEFAULT NULL COMMENT '产品id',
  `status` tinyint(2) DEFAULT NULL COMMENT '推送状态',
  `createAt` int(11) DEFAULT NULL COMMENT '创建日期',
  PRIMARY KEY (`id`,`userId`),
  KEY `ix_payload_history_id` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='推送历史表. 在确认时写入';



# Dump of table product
# ------------------------------------------------------------

DROP TABLE IF EXISTS `product`;

CREATE TABLE `product` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '产品id',
  `title` varchar(255) DEFAULT NULL COMMENT '产品名称',
  `appKey` varchar(255) DEFAULT NULL COMMENT '产品key',
  `secret` varchar(255) DEFAULT NULL COMMENT '产品secret',
  `clientTypeid` tinyint(2) DEFAULT NULL COMMENT '客户端类别id, 判断消息的投递方式，在离线状态下',
  `certPass` varchar(255) DEFAULT NULL COMMENT 'iOS推送证书密码(正式)',
  `certPath` varchar(255) DEFAULT NULL COMMENT 'iOS推送证书路径(正式)',
  `devCertPass` varchar(255) DEFAULT NULL COMMENT 'iOS推送证书密码(开发)',
  `devCertPath` varchar(255) DEFAULT NULL COMMENT 'iOS推送证书路径(开发)',
  PRIMARY KEY (`id`),
  UNIQUE KEY `ix_product_key` (`appKey`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='产品表';



# Dump of table topic
# ------------------------------------------------------------

DROP TABLE IF EXISTS `topic`;

CREATE TABLE `topic` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `title` varchar(50) DEFAULT NULL,
  `productId` int(11) NOT NULL,
  `status` tinyint(1) NOT NULL DEFAULT '1' COMMENT '1/0(启用/停用)',
  `totalClient` int(11) NOT NULL DEFAULT '0',
  `addAt` int(11) NOT NULL COMMENT '添加时间戳',
  `objectId` bigint(11) NOT NULL COMMENT '唯一的标示(有业务系统传入)',
  PRIMARY KEY (`id`),
  KEY `ix_topic_objectId` (`objectId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;



# Dump of table topic_client
# ------------------------------------------------------------

DROP TABLE IF EXISTS `topic_client`;

CREATE TABLE `topic_client` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `topicId` int(11) NOT NULL,
  `clientId` int(11) DEFAULT NULL,
  `userId` varchar(32) NOT NULL DEFAULT '',
  `addAt` int(11) NOT NULL COMMENT '添加时间戳',
  PRIMARY KEY (`id`),
  KEY `ix_topic_client_topicId` (`topicId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;




/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
