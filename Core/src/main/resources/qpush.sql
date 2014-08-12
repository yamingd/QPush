/*
Navicat MySQL Data Transfer

Source Server         : 127.0.0.1
Source Server Version : 50505
Source Host           : 127.0.0.1:3306
Source Database       : qpush

Target Server Type    : MYSQL
Target Server Version : 50505
File Encoding         : 65001

Date: 2014-08-12 16:35:39
*/

SET FOREIGN_KEY_CHECKS=0;
-- ----------------------------
-- Table structure for `client`
-- ----------------------------
DROP TABLE IF EXISTS `client`;
CREATE TABLE `client` (
  `id` bigint(20) NOT NULL,
  `productId` int(11) DEFAULT NULL,
  `userId` varchar(32) DEFAULT NULL,
  `deviceToken` varchar(255) DEFAULT NULL,
  `createAt` datetime DEFAULT NULL,
  `statusId` tinyint(4) DEFAULT NULL,
  `typeId` int(11) DEFAULT NULL,
  `lastSendAt` int(11) DEFAULT NULL,
  `lastOnline` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `ix_client_userId` (`userId`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- ----------------------------
-- Records of client
-- ----------------------------

-- ----------------------------
-- Table structure for `payload`
-- ----------------------------
DROP TABLE IF EXISTS `payload`;
CREATE TABLE `payload` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `title` varchar(255) DEFAULT NULL,
  `badge` int(11) DEFAULT NULL,
  `extras` varchar(255) DEFAULT NULL,
  `sound` varchar(255) DEFAULT NULL,
  `productId` int(11) DEFAULT NULL,
  `totalUsers` int(11) DEFAULT NULL,
  `createAt` int(11) DEFAULT NULL,
  `statusId` tinyint(2) DEFAULT NULL,
  `broadcast` tinyint(1) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `ix_payload_list` (`productId`,`broadcast`,`statusId`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- ----------------------------
-- Records of payload
-- ----------------------------

-- ----------------------------
-- Table structure for `payload_client`
-- ----------------------------
DROP TABLE IF EXISTS `payload_client`;
CREATE TABLE `payload_client` (
  `id` bigint(20) NOT NULL,
  `userId` varchar(255) NOT NULL,
  `productId` int(11) DEFAULT NULL,
  `statusId` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`,`userId`),
  KEY `ix_payload_client_pu` (`productId`,`userId`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- ----------------------------
-- Records of payload_client
-- ----------------------------

-- ----------------------------
-- Table structure for `payload_history`
-- ----------------------------
DROP TABLE IF EXISTS `payload_history`;
CREATE TABLE `payload_history` (
  `id` bigint(20) DEFAULT NULL,
  `title` varchar(255) DEFAULT NULL,
  `productId` int(11) DEFAULT NULL,
  `totalUsers` int(11) DEFAULT NULL,
  `sendAt` bigint(20) DEFAULT NULL,
  KEY `ix_payload_history_id` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- ----------------------------
-- Records of payload_history
-- ----------------------------

-- ----------------------------
-- Table structure for `product`
-- ----------------------------
DROP TABLE IF EXISTS `product`;
CREATE TABLE `product` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `title` varchar(255) DEFAULT NULL,
  `key` varchar(255) DEFAULT NULL,
  `secret` varchar(255) DEFAULT NULL,
  `clientTypeid` tinyint(2) DEFAULT NULL,
  `certPath` varchar(255) DEFAULT NULL,
  `devCertPath` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `ix_product_key` (`key`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- ----------------------------
-- Records of product
-- ----------------------------
