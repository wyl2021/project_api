-- MySQL dump 10.13  Distrib 8.3.0, for macos12.6 (x86_64)
--
-- Host: localhost    Database: project_progress
-- ------------------------------------------------------
-- Server version	8.0.28

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
-- Table structure for table `department`
--

DROP TABLE IF EXISTS `department`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `department` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_by` bigint DEFAULT NULL,
  `created_time` datetime DEFAULT NULL,
  `description` varchar(500) COLLATE utf8mb4_croatian_ci DEFAULT NULL,
  `name` varchar(50) COLLATE utf8mb4_croatian_ci NOT NULL,
  `updated_by` bigint DEFAULT NULL,
  `updated_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_croatian_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `department`
--

LOCK TABLES `department` WRITE;
/*!40000 ALTER TABLE `department` DISABLE KEYS */;
/*!40000 ALTER TABLE `department` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `progress_log`
--

DROP TABLE IF EXISTS `progress_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `progress_log` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `change_reason` varchar(500) COLLATE utf8mb4_croatian_ci DEFAULT NULL,
  `changed_by` bigint DEFAULT NULL,
  `changed_time` datetime DEFAULT NULL,
  `new_progress` int DEFAULT NULL,
  `old_progress` int DEFAULT NULL,
  `task_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKjg44y1kuayuxice8s152dt3pi` (`task_id`),
  CONSTRAINT `FKjg44y1kuayuxice8s152dt3pi` FOREIGN KEY (`task_id`) REFERENCES `task` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_croatian_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `progress_log`
--

LOCK TABLES `progress_log` WRITE;
/*!40000 ALTER TABLE `progress_log` DISABLE KEYS */;
/*!40000 ALTER TABLE `progress_log` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `project`
--

DROP TABLE IF EXISTS `project`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `project` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_by` bigint DEFAULT NULL,
  `created_time` datetime DEFAULT NULL,
  `description` varchar(500) COLLATE utf8mb4_croatian_ci DEFAULT NULL,
  `end_time` datetime DEFAULT NULL,
  `name` varchar(100) COLLATE utf8mb4_croatian_ci NOT NULL,
  `start_time` datetime DEFAULT NULL,
  `status` varchar(255) COLLATE utf8mb4_croatian_ci DEFAULT NULL,
  `total_progress` int DEFAULT NULL,
  `updated_by` bigint DEFAULT NULL,
  `updated_time` datetime DEFAULT NULL,
  `department_id` bigint DEFAULT NULL,
  `manager_id` bigint DEFAULT NULL,
  `original_end_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKl7ga8i4ry2amd4mb525tdmjf6` (`department_id`),
  KEY `FK2g58lgoxy5typ93ob5k7unehp` (`manager_id`),
  CONSTRAINT `FK2g58lgoxy5typ93ob5k7unehp` FOREIGN KEY (`manager_id`) REFERENCES `user` (`id`),
  CONSTRAINT `FKl7ga8i4ry2amd4mb525tdmjf6` FOREIGN KEY (`department_id`) REFERENCES `department` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_croatian_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `project`
--

LOCK TABLES `project` WRITE;
/*!40000 ALTER TABLE `project` DISABLE KEYS */;
INSERT INTO `project` VALUES (1,NULL,'2025-10-11 13:58:04','开发一个完整的电商网站系统','2025-11-11 13:58:04','电商网站开发','2025-08-11 13:58:04','IN_PROGRESS',35,NULL,'2025-10-11 13:58:04',NULL,4,NULL),(2,NULL,'2025-10-11 13:58:04','开发企业内部管理系统','2025-12-11 13:58:04','企业内部管理系统','2025-09-11 13:58:04','IN_PROGRESS',20,NULL,'2025-10-11 13:58:04',NULL,4,NULL),(3,NULL,'2025-10-11 13:58:04','开发一个移动应用','2026-01-11 13:58:04','移动应用开发','2025-10-11 13:58:04','NOT_STARTED',0,NULL,'2025-10-11 13:58:04',NULL,4,NULL),(4,NULL,'2025-10-11 13:58:04','升级公司官方网站','2025-09-11 13:58:04','公司官网升级','2025-06-11 13:58:04','COMPLETED',100,NULL,'2025-10-11 13:58:04',NULL,4,NULL);
/*!40000 ALTER TABLE `project` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `task`
--

DROP TABLE IF EXISTS `task`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `task` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `actual_hours` int DEFAULT NULL,
  `created_by` bigint DEFAULT NULL,
  `created_time` datetime DEFAULT NULL,
  `description` varchar(1000) COLLATE utf8mb4_croatian_ci DEFAULT NULL,
  `end_time` datetime DEFAULT NULL,
  `estimated_hours` int DEFAULT NULL,
  `name` varchar(100) COLLATE utf8mb4_croatian_ci NOT NULL,
  `progress` int DEFAULT NULL,
  `start_time` datetime DEFAULT NULL,
  `status` varchar(255) COLLATE utf8mb4_croatian_ci DEFAULT NULL,
  `updated_by` bigint DEFAULT NULL,
  `updated_time` datetime DEFAULT NULL,
  `assignee_id` bigint DEFAULT NULL,
  `parent_id` bigint DEFAULT NULL,
  `project_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKsrodfgrekcvv8ksyslehr53j8` (`assignee_id`),
  KEY `FK82ogu5quub0bhyuhp25riy7pf` (`parent_id`),
  KEY `FKk8qrwowg31kx7hp93sru1pdqa` (`project_id`),
  CONSTRAINT `FK82ogu5quub0bhyuhp25riy7pf` FOREIGN KEY (`parent_id`) REFERENCES `task` (`id`),
  CONSTRAINT `FKk8qrwowg31kx7hp93sru1pdqa` FOREIGN KEY (`project_id`) REFERENCES `project` (`id`),
  CONSTRAINT `FKsrodfgrekcvv8ksyslehr53j8` FOREIGN KEY (`assignee_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_croatian_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `task`
--

LOCK TABLES `task` WRITE;
/*!40000 ALTER TABLE `task` DISABLE KEYS */;
INSERT INTO `task` VALUES (1,140,NULL,'2025-10-11 13:58:04','开发电商网站的前端页面','2025-10-28 13:58:04',200,'前端页面开发',0,'2025-08-11 13:58:04','NOT_STARTED',NULL,'2025-10-11 13:58:04',5,NULL,1),(2,150,NULL,'2025-10-11 13:58:04','开发电商网站的后端API接口','2025-10-21 13:58:04',250,'后端接口开发',0,'2025-08-11 13:58:04','NOT_STARTED',NULL,'2025-10-11 13:58:04',6,NULL,1),(3,85,NULL,'2025-10-11 13:58:04','设计电商网站的UI界面','2025-08-25 13:58:04',80,'UI设计',0,'2025-07-28 13:58:04','NOT_STARTED',NULL,'2025-10-11 13:58:04',8,NULL,1),(4,0,NULL,'2025-10-11 13:58:04','测试电商网站的各项功能','2025-11-11 13:58:04',120,'系统测试',0,'2025-10-21 13:58:04','NOT_STARTED',NULL,'2025-10-11 13:58:04',7,NULL,1),(5,60,NULL,'2025-10-11 13:58:04','分析企业内部管理系统的需求并制定计划','2025-09-25 13:58:04',60,'需求分析与规划',0,'2025-09-11 13:58:04','NOT_STARTED',NULL,'2025-10-11 13:58:04',5,NULL,2),(6,120,NULL,'2025-10-11 13:58:04','开发企业内部管理系统的核心功能','2025-11-27 13:58:04',300,'核心功能开发',0,'2025-09-25 13:58:04','NOT_STARTED',NULL,'2025-10-11 13:58:04',6,NULL,2),(7,12,NULL,'2025-10-11 13:58:04','调研移动应用开发的相关技术','2025-10-25 13:58:04',40,'技术调研',0,'2025-10-11 13:58:04','NOT_STARTED',NULL,'2025-10-11 13:58:04',5,NULL,3);
/*!40000 ALTER TABLE `task` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_time` datetime DEFAULT NULL,
  `email` varchar(100) COLLATE utf8mb4_croatian_ci DEFAULT NULL,
  `is_active` bit(1) DEFAULT NULL,
  `name` varchar(50) COLLATE utf8mb4_croatian_ci NOT NULL,
  `password` varchar(100) COLLATE utf8mb4_croatian_ci NOT NULL,
  `phone` varchar(20) COLLATE utf8mb4_croatian_ci DEFAULT NULL,
  `role` varchar(255) COLLATE utf8mb4_croatian_ci DEFAULT NULL,
  `updated_time` datetime DEFAULT NULL,
  `username` varchar(50) COLLATE utf8mb4_croatian_ci NOT NULL,
  `department_id` bigint DEFAULT NULL,
  `avatar` varchar(255) COLLATE utf8mb4_croatian_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_sb8bbouer5wak8vyiiy4pf2bx` (`username`),
  KEY `FKgkh2fko1e4ydv1y6vtrwdc6my` (`department_id`),
  CONSTRAINT `FKgkh2fko1e4ydv1y6vtrwdc6my` FOREIGN KEY (`department_id`) REFERENCES `department` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_croatian_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user`
--

LOCK TABLES `user` WRITE;
/*!40000 ALTER TABLE `user` DISABLE KEYS */;
INSERT INTO `user` VALUES (1,'2025-10-11 11:33:45','testuser1@example.com',_binary '','测试用户1','$2a$10$O2xAZDAqG5/gquue6rJ2d.Vi4fZ.SYN4iocmfE16DK5SqvUoZnZDu','13800138000','DEVELOPER','2025-10-11 11:33:45','testuser1',NULL,'https://gips1.baidu.com/it/u=3592648155,3226527493&fm=3074&app=3074&f=PNG?w=2048&h=2048'),(2,'2025-10-11 11:59:40','admin@example.com',_binary '','系统管理员','$2a$10$WUqoG4yb1Ov4CcorOHsmwOpP9mxBQk2lIo7NcqvaDOG9IpIXhnCNm','13800138001','ADMIN','2025-10-11 11:59:40','admin',NULL,'https://gips2.baidu.com/it/u=1095238712,1889906700&fm=3074&app=3074&f=PNG?w=2048&h=2048'),(4,'2025-10-11 13:58:03','manager@example.com',_binary '','项目经理','$2a$10$k8EO0XluuYmhhpIs1KBzSel4vRE/S1v5TTKprZ.I6g7PVpmREwPlu','13800138002','MANAGER','2025-10-11 13:58:03','manager1',NULL,'https://gips1.baidu.com/it/u=3592648155,3226527493&fm=3074&app=3074&f=PNG?w=2048&h=2048'),(5,'2025-10-11 13:58:03','developer1@example.com',_binary '','开发人员1','$2a$10$69FOnfUgiXC5AiJ0pF19Guy00wNlOarqLnIkLZgmfag/nF0EEIEQe','13800138003','DEVELOPER','2025-10-11 13:58:03','developer1',NULL,'https://gips2.baidu.com/it/u=1095238712,1889906700&fm=3074&app=3074&f=PNG?w=2048&h=2048'),(6,'2025-10-11 13:58:03','developer2@example.com',_binary '','开发人员2','$2a$10$hLWk5g9lTgU3cF3Q6uoIUetJO5Bi7M7GuXTwLL3bpUMK7z9JoRyVe','13800138004','DEVELOPER','2025-10-11 13:58:03','developer2',NULL,'https://gips2.baidu.com/it/u=3383761095,3961713394&fm=3074&app=3074&f=PNG?w=2048&h=2048'),(7,'2025-10-11 13:58:04','tester@example.com',_binary '','测试人员','$2a$10$Ay2aAuPHEtT5VFGREd2UcunYozVPZgTP.OKFJ73MIXOo5dJjeNoO.','13800138005','TESTER','2025-10-11 13:58:04','tester1',NULL,'https://gips1.baidu.com/it/u=3592648155,3226527493&fm=3074&app=3074&f=PNG?w=2048&h=2048'),(8,'2025-10-11 13:58:04','designer@example.com',_binary '','设计师','$2a$10$GLWz6w2rwNS2k9oq.ZWNqOwzTRB9MMiz5jhNQGVXP0eQOP8sR2.xK','13800138006','DESIGNER','2025-10-11 13:58:04','designer1',NULL,'https://gips2.baidu.com/it/u=3383761095,3961713394&fm=3074&app=3074&f=PNG?w=2048&h=2048');
/*!40000 ALTER TABLE `user` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_project`
--

DROP TABLE IF EXISTS `user_project`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_project` (
  `user_id` bigint NOT NULL,
  `project_id` bigint NOT NULL,
  KEY `FKocfkr6u2yh3w1qmybs8vxuv1c` (`project_id`),
  KEY `FKpw81exe7fsdl7mddqujvu91kx` (`user_id`),
  CONSTRAINT `FKocfkr6u2yh3w1qmybs8vxuv1c` FOREIGN KEY (`project_id`) REFERENCES `project` (`id`),
  CONSTRAINT `FKpw81exe7fsdl7mddqujvu91kx` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_croatian_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_project`
--

LOCK TABLES `user_project` WRITE;
/*!40000 ALTER TABLE `user_project` DISABLE KEYS */;
/*!40000 ALTER TABLE `user_project` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-10-14 14:02:16
