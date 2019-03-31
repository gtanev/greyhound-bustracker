/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET NAMES utf8 */;
/*!50503 SET NAMES utf8mb4 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;

CREATE DATABASE IF NOT EXISTS `bus_tracker` /*!40100 DEFAULT CHARACTER SET utf8 */;
USE `bus_tracker`;

CREATE TABLE IF NOT EXISTS `carrier` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `carrier_name` varchar(255) NOT NULL,
  `version` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_mm1pr5cd5mipgdx95xryp33rd` (`carrier_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `location` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `location_id` bigint(20) NOT NULL,
  `location_name` varchar(255) DEFAULT NULL,
  `version` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_ndmw96jtvo3mq70rcdifvhmbi` (`location_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `driver` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `driver_id` bigint(20) NOT NULL,
  `first_name` varchar(255) NOT NULL,
  `last_name` varchar(255) NOT NULL,
  `middle_init` char(1) DEFAULT NULL,
  `oper_class` char(1) DEFAULT NULL,
  `version` bigint(20) NOT NULL,
  `carrier_id` bigint(20) DEFAULT NULL,
  `location_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_srsyonjn4f1w9i4o7hon7dnif` (`driver_id`),
  KEY `FKavgxgcmqddfv364qkbvns7gyg` (`carrier_id`),
  KEY `FKbfh71ut84kjlqspoeix8cn0vy` (`location_id`),
  CONSTRAINT `FKavgxgcmqddfv364qkbvns7gyg` FOREIGN KEY (`carrier_id`) REFERENCES `carrier` (`id`),
  CONSTRAINT `FKbfh71ut84kjlqspoeix8cn0vy` FOREIGN KEY (`location_id`) REFERENCES `location` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*!40101 SET SQL_MODE=IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS=IF(@OLD_FOREIGN_KEY_CHECKS IS NULL, 1, @OLD_FOREIGN_KEY_CHECKS) */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
