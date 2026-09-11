-- MySQL dump 10.13  Distrib 8.3.0, for Linux (x86_64)
--
-- Host: localhost    Database: home_energy_tracker
-- ------------------------------------------------------
-- Server version	8.3.0

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Current Database: `home_energy_tracker`
--

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `home_energy_tracker` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;

USE `home_energy_tracker`;

--
-- Table structure for table `devices`
--

DROP TABLE IF EXISTS `devices`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `devices` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `type` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `location` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `user_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_device_user_id` (`user_id`),
  CONSTRAINT `fk_device_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=201 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `devices`
--

LOCK TABLES `devices` WRITE;
/*!40000 ALTER TABLE `devices` DISABLE KEYS */;
INSERT INTO `devices` VALUES (1,'Device1','CAMERA','Location11',2),(2,'Device2','THERMOSTAT','Location21',3),(3,'Device3','LIGHT','Location01',4),(4,'Device4','LOCK','Location11',5),(5,'Device5','DOORBELL','Location21',6),(6,'Device6','SPEAKER','Location01',7),(7,'Device7','CAMERA','Location11',8),(8,'Device8','THERMOSTAT','Location21',9),(9,'Device9','LIGHT','Location01',10),(10,'Device10','LOCK','Location11',1),(11,'Device11','DOORBELL','Location21',2),(12,'Device12','SPEAKER','Location01',3),(13,'Device13','CAMERA','Location11',4),(14,'Device14','THERMOSTAT','Location21',5),(15,'Device15','LIGHT','Location01',6),(16,'Device16','LOCK','Location11',7),(17,'Device17','DOORBELL','Location21',8),(18,'Device18','SPEAKER','Location01',9),(19,'Device19','CAMERA','Location11',10),(20,'Device20','THERMOSTAT','Location21',1),(21,'Device21','LIGHT','Location01',2),(22,'Device22','LOCK','Location11',3),(23,'Device23','DOORBELL','Location21',4),(24,'Device24','SPEAKER','Location01',5),(25,'Device25','CAMERA','Location11',6),(26,'Device26','THERMOSTAT','Location21',7),(27,'Device27','LIGHT','Location01',8),(28,'Device28','LOCK','Location11',9),(29,'Device29','DOORBELL','Location21',10),(30,'Device30','SPEAKER','Location01',1),(31,'Device31','CAMERA','Location11',2),(32,'Device32','THERMOSTAT','Location21',3),(33,'Device33','LIGHT','Location01',4),(34,'Device34','LOCK','Location11',5),(35,'Device35','DOORBELL','Location21',6),(36,'Device36','SPEAKER','Location01',7),(37,'Device37','CAMERA','Location11',8),(38,'Device38','THERMOSTAT','Location21',9),(39,'Device39','LIGHT','Location01',10),(40,'Device40','LOCK','Location11',1),(41,'Device41','DOORBELL','Location21',2),(42,'Device42','SPEAKER','Location01',3),(43,'Device43','CAMERA','Location11',4),(44,'Device44','THERMOSTAT','Location21',5),(45,'Device45','LIGHT','Location01',6),(46,'Device46','LOCK','Location11',7),(47,'Device47','DOORBELL','Location21',8),(48,'Device48','SPEAKER','Location01',9),(49,'Device49','CAMERA','Location11',10),(50,'Device50','THERMOSTAT','Location21',1),(51,'Device51','LIGHT','Location01',2),(52,'Device52','LOCK','Location11',3),(53,'Device53','DOORBELL','Location21',4),(54,'Device54','SPEAKER','Location01',5),(55,'Device55','CAMERA','Location11',6),(56,'Device56','THERMOSTAT','Location21',7),(57,'Device57','LIGHT','Location01',8),(58,'Device58','LOCK','Location11',9),(59,'Device59','DOORBELL','Location21',10),(60,'Device60','SPEAKER','Location01',1),(61,'Device61','CAMERA','Location11',2),(62,'Device62','THERMOSTAT','Location21',3),(63,'Device63','LIGHT','Location01',4),(64,'Device64','LOCK','Location11',5),(65,'Device65','DOORBELL','Location21',6),(66,'Device66','SPEAKER','Location01',7),(67,'Device67','CAMERA','Location11',8),(68,'Device68','THERMOSTAT','Location21',9),(69,'Device69','LIGHT','Location01',10),(70,'Device70','LOCK','Location11',1),(71,'Device71','DOORBELL','Location21',2),(72,'Device72','SPEAKER','Location01',3),(73,'Device73','CAMERA','Location11',4),(74,'Device74','THERMOSTAT','Location21',5),(75,'Device75','LIGHT','Location01',6),(76,'Device76','LOCK','Location11',7),(77,'Device77','DOORBELL','Location21',8),(78,'Device78','SPEAKER','Location01',9),(79,'Device79','CAMERA','Location11',10),(80,'Device80','THERMOSTAT','Location21',1),(81,'Device81','LIGHT','Location01',2),(82,'Device82','LOCK','Location11',3),(83,'Device83','DOORBELL','Location21',4),(84,'Device84','SPEAKER','Location01',5),(85,'Device85','CAMERA','Location11',6),(86,'Device86','THERMOSTAT','Location21',7),(87,'Device87','LIGHT','Location01',8),(88,'Device88','LOCK','Location11',9),(89,'Device89','DOORBELL','Location21',10),(90,'Device90','SPEAKER','Location01',1),(91,'Device91','CAMERA','Location11',2),(92,'Device92','THERMOSTAT','Location21',3),(93,'Device93','LIGHT','Location01',4),(94,'Device94','LOCK','Location11',5),(95,'Device95','DOORBELL','Location21',6),(96,'Device96','SPEAKER','Location01',7),(97,'Device97','CAMERA','Location11',8),(98,'Device98','THERMOSTAT','Location21',9),(99,'Device99','LIGHT','Location01',10),(100,'Device100','LOCK','Location11',1),(101,'Device101','DOORBELL','Location21',2),(102,'Device102','SPEAKER','Location01',3),(103,'Device103','CAMERA','Location11',4),(104,'Device104','THERMOSTAT','Location21',5),(105,'Device105','LIGHT','Location01',6),(106,'Device106','LOCK','Location11',7),(107,'Device107','DOORBELL','Location21',8),(108,'Device108','SPEAKER','Location01',9),(109,'Device109','CAMERA','Location11',10),(110,'Device110','THERMOSTAT','Location21',1),(111,'Device111','LIGHT','Location01',2),(112,'Device112','LOCK','Location11',3),(113,'Device113','DOORBELL','Location21',4),(114,'Device114','SPEAKER','Location01',5),(115,'Device115','CAMERA','Location11',6),(116,'Device116','THERMOSTAT','Location21',7),(117,'Device117','LIGHT','Location01',8),(118,'Device118','LOCK','Location11',9),(119,'Device119','DOORBELL','Location21',10),(120,'Device120','SPEAKER','Location01',1),(121,'Device121','CAMERA','Location11',2),(122,'Device122','THERMOSTAT','Location21',3),(123,'Device123','LIGHT','Location01',4),(124,'Device124','LOCK','Location11',5),(125,'Device125','DOORBELL','Location21',6),(126,'Device126','SPEAKER','Location01',7),(127,'Device127','CAMERA','Location11',8),(128,'Device128','THERMOSTAT','Location21',9),(129,'Device129','LIGHT','Location01',10),(130,'Device130','LOCK','Location11',1),(131,'Device131','DOORBELL','Location21',2),(132,'Device132','SPEAKER','Location01',3),(133,'Device133','CAMERA','Location11',4),(134,'Device134','THERMOSTAT','Location21',5),(135,'Device135','LIGHT','Location01',6),(136,'Device136','LOCK','Location11',7),(137,'Device137','DOORBELL','Location21',8),(138,'Device138','SPEAKER','Location01',9),(139,'Device139','CAMERA','Location11',10),(140,'Device140','THERMOSTAT','Location21',1),(141,'Device141','LIGHT','Location01',2),(142,'Device142','LOCK','Location11',3),(143,'Device143','DOORBELL','Location21',4),(144,'Device144','SPEAKER','Location01',5),(145,'Device145','CAMERA','Location11',6),(146,'Device146','THERMOSTAT','Location21',7),(147,'Device147','LIGHT','Location01',8),(148,'Device148','LOCK','Location11',9),(149,'Device149','DOORBELL','Location21',10),(150,'Device150','SPEAKER','Location01',1),(151,'Device151','CAMERA','Location11',2),(152,'Device152','THERMOSTAT','Location21',3),(153,'Device153','LIGHT','Location01',4),(154,'Device154','LOCK','Location11',5),(155,'Device155','DOORBELL','Location21',6),(156,'Device156','SPEAKER','Location01',7),(157,'Device157','CAMERA','Location11',8),(158,'Device158','THERMOSTAT','Location21',9),(159,'Device159','LIGHT','Location01',10),(160,'Device160','LOCK','Location11',1),(161,'Device161','DOORBELL','Location21',2),(162,'Device162','SPEAKER','Location01',3),(163,'Device163','CAMERA','Location11',4),(164,'Device164','THERMOSTAT','Location21',5),(165,'Device165','LIGHT','Location01',6),(166,'Device166','LOCK','Location11',7),(167,'Device167','DOORBELL','Location21',8),(168,'Device168','SPEAKER','Location01',9),(169,'Device169','CAMERA','Location11',10),(170,'Device170','THERMOSTAT','Location21',1),(171,'Device171','LIGHT','Location01',2),(172,'Device172','LOCK','Location11',3),(173,'Device173','DOORBELL','Location21',4),(174,'Device174','SPEAKER','Location01',5),(175,'Device175','CAMERA','Location11',6),(176,'Device176','THERMOSTAT','Location21',7),(177,'Device177','LIGHT','Location01',8),(178,'Device178','LOCK','Location11',9),(179,'Device179','DOORBELL','Location21',10),(180,'Device180','SPEAKER','Location01',1),(181,'Device181','CAMERA','Location11',2),(182,'Device182','THERMOSTAT','Location21',3),(183,'Device183','LIGHT','Location01',4),(184,'Device184','LOCK','Location11',5),(185,'Device185','DOORBELL','Location21',6),(186,'Device186','SPEAKER','Location01',7),(187,'Device187','CAMERA','Location11',8),(188,'Device188','THERMOSTAT','Location21',9),(189,'Device189','LIGHT','Location01',10),(190,'Device190','LOCK','Location11',1),(191,'Device191','DOORBELL','Location21',2),(192,'Device192','SPEAKER','Location01',3),(193,'Device193','CAMERA','Location11',4),(194,'Device194','THERMOSTAT','Location21',5),(195,'Device195','LIGHT','Location01',6),(196,'Device196','LOCK','Location11',7),(197,'Device197','DOORBELL','Location21',8),(198,'Device198','SPEAKER','Location01',9),(199,'Device199','CAMERA','Location11',10),(200,'Device200','THERMOSTAT','Location21',1);
/*!40000 ALTER TABLE `devices` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `flyway_schema_history`
--

DROP TABLE IF EXISTS `flyway_schema_history`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `flyway_schema_history` (
  `installed_rank` int NOT NULL,
  `version` varchar(50) DEFAULT NULL,
  `description` varchar(200) NOT NULL,
  `type` varchar(20) NOT NULL,
  `script` varchar(1000) NOT NULL,
  `checksum` int DEFAULT NULL,
  `installed_by` varchar(100) NOT NULL,
  `installed_on` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `execution_time` int NOT NULL,
  `success` tinyint(1) NOT NULL,
  PRIMARY KEY (`installed_rank`),
  KEY `flyway_schema_history_s_idx` (`success`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `flyway_schema_history`
--

LOCK TABLES `flyway_schema_history` WRITE;
/*!40000 ALTER TABLE `flyway_schema_history` DISABLE KEYS */;
INSERT INTO `flyway_schema_history` VALUES (1,'1','user table','SQL','V1__user_table.sql',-2019836082,'root','2026-09-05 16:03:12',384,1),(2,'2','device table','SQL','V2__device_table.sql',-236645826,'root','2026-09-05 16:03:13',32,1);
/*!40000 ALTER TABLE `flyway_schema_history` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `firstname` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `lastname` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `email` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `address` text COLLATE utf8mb4_unicode_ci,
  `alerting` tinyint(1) NOT NULL DEFAULT '0',
  `energy_alerting_threshold` double NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_email` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES (1,'User1','Surname1','user1@example.com','1 Example St',0,1001),(2,'User2','Surname2','user2@example.com','2 Example St',1,1002),(3,'User3','Surname3','user3@example.com','3 Example St',0,1003),(4,'User4','Surname4','user4@example.com','4 Example St',1,1004),(5,'User5','Surname5','user5@example.com','5 Example St',0,1005),(6,'User6','Surname6','user6@example.com','6 Example St',1,1006),(7,'User7','Surname7','user7@example.com','7 Example St',0,1007),(8,'User8','Surname8','user8@example.com','8 Example St',1,1008),(9,'User9','Surname9','user9@example.com','9 Example St',0,1009),(10,'User10','Surname10','user10@example.com','10 Example St',1,1010);
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-09-11 16:16:08
