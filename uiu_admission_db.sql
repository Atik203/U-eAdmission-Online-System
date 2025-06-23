CREATE DATABASE  IF NOT EXISTS `uiu_admission_db` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;
USE `uiu_admission_db`;
-- MySQL dump 10.13  Distrib 8.0.41, for Win64 (x86_64)
--
-- Host: localhost    Database: uiu_admission_db
-- ------------------------------------------------------
-- Server version	8.0.41

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `applications`
--

DROP TABLE IF EXISTS `applications`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `applications` (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `first_name` varchar(100) NOT NULL,
  `last_name` varchar(100) NOT NULL,
  `email` varchar(100) NOT NULL,
  `phone` varchar(20) NOT NULL,
  `date_of_birth` date NOT NULL,
  `gender` varchar(10) NOT NULL,
  `address` varchar(255) NOT NULL,
  `city` varchar(100) NOT NULL,
  `postal_code` varchar(20) DEFAULT NULL,
  `father_name` varchar(100) NOT NULL,
  `father_occupation` varchar(100) DEFAULT NULL,
  `mother_name` varchar(100) NOT NULL,
  `mother_occupation` varchar(100) DEFAULT NULL,
  `guardian_phone` varchar(20) NOT NULL,
  `guardian_email` varchar(100) DEFAULT NULL,
  `program` varchar(100) NOT NULL,
  `institution` varchar(255) NOT NULL,
  `ssc_gpa` decimal(3,2) NOT NULL,
  `hsc_gpa` decimal(3,2) NOT NULL,
  `ssc_year` varchar(4) NOT NULL,
  `hsc_year` varchar(4) NOT NULL,
  `status` varchar(20) NOT NULL DEFAULT 'Pending',
  `payment_complete` tinyint(1) NOT NULL DEFAULT '0',
  `application_date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `user_id` (`user_id`),
  CONSTRAINT `applications_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `applications`
--

LOCK TABLES `applications` WRITE;
/*!40000 ALTER TABLE `applications` DISABLE KEYS */;
INSERT INTO `applications` VALUES (1,2,'Atikur','Rahaman','atikurrahaman0304@gmail.com','01723383575','2025-05-06','Male','Dhaka','Dhaka','1212','Rafiqul Islam','NA','Aklima Akter','NA','01834535343','NA','CSE','BMARPC',5.00,5.00,'2018','2020','Approved',1,'2025-05-04 17:31:23'),(2,2,'Atikur','Rahaman','atikurrahaman0304@gmail.com','01723383575','2025-04-30','Male','Dhaka','Dhaka','','Rafiqul Islam','NA','Aklima Akter','NA','0184352334','NA','Data Science','BMARPC',5.00,5.00,'2018','2020','Approved',1,'2025-05-04 19:51:35'),(3,2,'Atikur','Rahaman','atikurrahaman0304@gmail.com','01723383575','2025-05-14','Male','Dhaka','Dhaka','','Rafiqul Islam','NA','Aklima Akter','NA','0183433234','NA','Civil Engineering','BMARPC',5.00,5.00,'2018','2020','Approved',1,'2025-05-05 19:04:46'),(4,2,'Atikur','Rahaman','atikurrahaman0304@gmail.com','01723383575','2025-05-21','Male','Dhaka','Dhaka','1212','NA','NA','NA','NA','017233849874','NA','Economics','BMARPC',5.00,5.00,'2018','2020','Pending',0,'2025-05-25 06:19:24');
/*!40000 ALTER TABLE `applications` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `chat_messages`
--

DROP TABLE IF EXISTS `chat_messages`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `chat_messages` (
  `id` int NOT NULL AUTO_INCREMENT,
  `sender_id` int NOT NULL,
  `receiver_id` int DEFAULT NULL,
  `message` text NOT NULL,
  `timestamp` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `is_broadcast` tinyint(1) DEFAULT '0',
  `is_read` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `sender_id` (`sender_id`),
  KEY `receiver_id` (`receiver_id`),
  KEY `timestamp` (`timestamp`)
) ENGINE=InnoDB AUTO_INCREMENT=72 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `chat_messages`
--

LOCK TABLES `chat_messages` WRITE;
/*!40000 ALTER TABLE `chat_messages` DISABLE KEYS */;
INSERT INTO `chat_messages` VALUES (1,2,1,'........messages','2025-05-30 18:47:16',0,1),(2,2,1,'what.....','2025-05-30 18:47:39',0,1),(3,1,-1,'ok.','2025-05-30 18:52:29',0,0),(4,2,1,'nothing','2025-05-30 19:09:41',0,1),(5,2,1,'The U-eAdmission Online System is a comprehensive JavaFX application','2025-05-30 19:18:47',0,1),(6,1,2,'ok','2025-05-30 19:19:44',0,1),(7,2,1,'thank you','2025-05-30 19:37:09',0,1),(8,2,1,'hi','2025-05-30 19:53:41',0,1),(9,1,2,'hello','2025-05-30 20:36:59',0,1),(10,2,1,'ok','2025-05-30 20:37:55',0,1),(11,1,2,'..','2025-05-30 20:38:08',0,1),(12,1,2,'ok','2025-05-30 20:44:06',0,1),(13,2,1,'wow','2025-05-30 20:44:43',0,1),(14,2,1,'ok','2025-05-30 20:46:52',0,1),(15,1,2,'nothing','2025-05-30 20:48:23',0,1),(16,2,1,'working','2025-05-30 20:48:38',0,1),(17,1,2,'there is problem','2025-05-30 20:49:16',0,1),(18,1,2,'checking','2025-05-30 20:54:58',0,1),(19,1,2,'now almost fixed','2025-05-30 20:55:08',0,1),(20,1,2,'time is checking','2025-05-31 06:34:14',0,1),(21,2,1,'time is not working properly','2025-05-31 06:34:33',0,1),(22,2,1,'checking','2025-05-31 06:40:18',0,1),(23,2,1,'ok','2025-05-31 06:40:53',0,1),(24,1,2,'....','2025-05-31 06:43:12',0,1),(25,1,2,'ok','2025-05-31 06:48:33',0,1),(26,2,1,'ok','2025-05-31 06:55:21',0,1),(27,1,2,'checing','2025-05-31 07:09:28',0,1),(28,1,2,'there is problem','2025-05-31 07:14:51',0,1),(29,2,1,'multiple working','2025-05-31 07:15:03',0,1),(30,2,1,'checking again','2025-05-31 07:22:49',0,1),(31,1,2,'wokring','2025-05-31 07:23:03',0,1),(32,2,1,'what cauing problem','2025-05-31 07:23:22',0,1),(33,2,1,'....ok','2025-05-31 07:29:40',0,1),(34,2,1,'not wokring properly','2025-05-31 07:29:53',0,1),(35,2,1,'now checking','2025-05-31 07:33:55',0,1),(36,1,2,'working realtime','2025-05-31 07:34:02',0,1),(37,1,3,'sending message to test user','2025-05-31 07:34:20',0,1),(38,3,1,'ok receinv','2025-05-31 07:35:14',0,1),(39,2,1,'working','2025-05-31 07:35:24',0,1),(40,3,1,'ok','2025-05-31 07:35:39',0,1),(41,2,1,'......','2025-05-31 07:35:57',0,1),(42,1,3,'.......','2025-05-31 07:36:03',0,1),(43,1,2,'..........','2025-05-31 07:36:08',0,1),(44,3,1,'..........','2025-05-31 07:36:14',0,1),(45,1,2,'...........','2025-05-31 07:36:32',0,1),(46,1,2,'............................','2025-05-31 07:46:22',0,1),(47,2,1,'checking','2025-05-31 07:46:37',0,1),(48,1,2,'ok','2025-05-31 07:46:45',0,1),(49,2,1,'hello, I need help application related','2025-05-31 15:32:36',0,1),(50,3,1,'hello, I need help application related....','2025-05-31 15:32:51',0,1),(51,1,3,'what type of help','2025-05-31 15:33:05',0,1),(52,1,2,'what type of help needed','2025-05-31 15:33:24',0,1),(53,1,3,'..........','2025-05-31 15:34:09',0,1),(54,1,2,'............','2025-05-31 15:34:15',0,1),(55,2,1,'............','2025-05-31 15:34:20',0,1),(56,3,1,'................','2025-05-31 15:34:26',0,1),(57,2,1,'...................','2025-06-01 06:00:49',0,1),(58,3,1,'............','2025-06-01 06:00:56',0,1),(59,1,3,'.............','2025-06-01 06:01:06',0,1),(60,1,2,'...............','2025-06-01 06:01:12',0,1),(61,2,1,'hello','2025-06-01 07:14:34',0,1),(62,3,1,'hello i need','2025-06-01 07:14:45',0,1),(63,1,3,'ok','2025-06-01 07:15:30',0,1),(64,1,2,'ok','2025-06-01 07:15:35',0,1),(65,2,1,'Hello','2025-06-23 08:14:53',0,1),(66,4,2,'hello','2025-06-23 08:15:03',0,1),(67,3,1,'Hello','2025-06-23 08:16:21',0,1),(68,2,1,'ok','2025-06-23 08:16:31',0,1),(69,1,3,'message received','2025-06-23 08:16:49',0,1),(70,2,1,'.............','2025-06-23 08:21:41',0,1),(71,3,1,'..................','2025-06-23 08:22:53',0,1);
/*!40000 ALTER TABLE `chat_messages` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `chat_messages_queue`
--

DROP TABLE IF EXISTS `chat_messages_queue`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `chat_messages_queue` (
  `id` int NOT NULL AUTO_INCREMENT,
  `sender_id` int NOT NULL,
  `receiver_id` int NOT NULL,
  `message` text NOT NULL,
  `timestamp` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `sent` tinyint(1) DEFAULT '0',
  `attempts` int DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `sender_id` (`sender_id`),
  KEY `timestamp` (`timestamp`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `chat_messages_queue`
--

LOCK TABLES `chat_messages_queue` WRITE;
/*!40000 ALTER TABLE `chat_messages_queue` DISABLE KEYS */;
INSERT INTO `chat_messages_queue` VALUES (1,2,1,'hello','2025-05-30 18:14:47',0,0),(2,1,-1,'what','2025-05-30 18:16:37',0,0),(3,2,1,'hello','2025-05-30 18:23:02',0,0),(4,2,1,'what','2025-05-30 18:28:45',1,0);
/*!40000 ALTER TABLE `chat_messages_queue` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `exam_sessions`
--

DROP TABLE IF EXISTS `exam_sessions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `exam_sessions` (
  `id` int NOT NULL AUTO_INCREMENT,
  `student_id` int NOT NULL,
  `question_paper_id` int NOT NULL,
  `start_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `end_time` timestamp NULL DEFAULT NULL,
  `score` decimal(5,2) DEFAULT NULL,
  `max_score` decimal(5,2) DEFAULT NULL,
  `status` enum('in_progress','completed','abandoned') DEFAULT 'in_progress',
  `warning_count` int NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_exam_sessions_student_id` (`student_id`),
  KEY `idx_exam_sessions_paper_id` (`question_paper_id`),
  KEY `idx_exam_sessions_warning_count` (`warning_count`),
  CONSTRAINT `exam_sessions_ibfk_1` FOREIGN KEY (`student_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  CONSTRAINT `exam_sessions_ibfk_2` FOREIGN KEY (`question_paper_id`) REFERENCES `question_papers` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `exam_sessions`
--

LOCK TABLES `exam_sessions` WRITE;
/*!40000 ALTER TABLE `exam_sessions` DISABLE KEYS */;
INSERT INTO `exam_sessions` VALUES (1,1,6,'2025-06-21 18:44:39','2025-06-21 18:44:39',1.00,1.00,'completed',0),(2,1,6,'2025-06-21 19:03:46','2025-06-21 19:03:46',1.00,1.00,'completed',0),(3,2,6,'2025-06-22 20:09:42','2025-06-22 20:09:42',1.00,1.00,'completed',0),(4,2,6,'2025-06-22 20:50:51','2025-06-22 20:50:51',0.00,1.00,'completed',0),(5,2,6,'2025-06-22 20:56:45','2025-06-22 20:56:45',0.00,1.00,'completed',3),(10,2,6,'2025-06-23 09:27:05',NULL,NULL,NULL,'in_progress',0),(11,2,6,'2025-06-23 09:27:18','2025-06-23 09:27:18',0.00,1.00,'completed',0);
/*!40000 ALTER TABLE `exam_sessions` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `exam_subjects`
--

DROP TABLE IF EXISTS `exam_subjects`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `exam_subjects` (
  `id` int NOT NULL AUTO_INCREMENT,
  `school_id` int NOT NULL,
  `exam_type_id` int NOT NULL,
  `subject_id` int NOT NULL,
  `max_questions` int NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_exam_subject` (`school_id`,`exam_type_id`,`subject_id`),
  KEY `idx_exam_subjects_school_id` (`school_id`),
  KEY `idx_exam_subjects_exam_type_id` (`exam_type_id`),
  KEY `idx_exam_subjects_subject_id` (`subject_id`),
  CONSTRAINT `exam_subjects_ibfk_1` FOREIGN KEY (`school_id`) REFERENCES `schools` (`id`) ON DELETE CASCADE,
  CONSTRAINT `exam_subjects_ibfk_2` FOREIGN KEY (`exam_type_id`) REFERENCES `exam_types` (`id`) ON DELETE CASCADE,
  CONSTRAINT `exam_subjects_ibfk_3` FOREIGN KEY (`subject_id`) REFERENCES `subjects` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=2939 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `exam_subjects`
--

LOCK TABLES `exam_subjects` WRITE;
/*!40000 ALTER TABLE `exam_subjects` DISABLE KEYS */;
INSERT INTO `exam_subjects` VALUES (1,1,1,1,30,'2025-06-19 12:51:42','2025-06-19 12:51:42'),(2,1,1,2,15,'2025-06-19 12:51:42','2025-06-19 12:51:42'),(3,1,1,3,30,'2025-06-19 12:51:42','2025-06-19 12:51:42'),(4,1,2,1,30,'2025-06-19 12:51:42','2025-06-19 12:51:42'),(5,1,2,2,15,'2025-06-19 12:51:42','2025-06-19 12:51:42'),(6,1,2,3,30,'2025-06-19 12:51:42','2025-06-19 12:51:42'),(7,2,1,1,30,'2025-06-19 12:51:42','2025-06-19 12:51:42'),(8,2,1,2,15,'2025-06-19 12:51:42','2025-06-19 12:51:42'),(9,2,1,4,30,'2025-06-19 12:51:42','2025-06-19 12:51:42'),(10,2,2,1,30,'2025-06-19 12:51:42','2025-06-19 12:51:42'),(11,2,2,2,15,'2025-06-19 12:51:42','2025-06-19 12:51:42'),(12,2,2,4,30,'2025-06-19 12:51:42','2025-06-19 12:51:42'),(13,3,1,1,30,'2025-06-19 12:51:42','2025-06-19 12:51:42'),(14,3,1,2,15,'2025-06-19 12:51:42','2025-06-19 12:51:42'),(15,3,1,5,15,'2025-06-19 12:51:42','2025-06-19 12:51:42'),(16,3,1,6,15,'2025-06-19 12:51:42','2025-06-19 12:51:42'),(17,3,2,1,30,'2025-06-19 12:51:42','2025-06-19 12:51:42'),(18,3,2,2,15,'2025-06-19 12:51:42','2025-06-19 12:51:42'),(19,3,2,5,15,'2025-06-19 12:51:42','2025-06-19 12:51:42'),(20,3,2,6,15,'2025-06-19 12:51:42','2025-06-19 12:51:42'),(21,4,1,1,30,'2025-06-19 12:51:42','2025-06-19 12:51:42'),(22,4,1,2,15,'2025-06-19 12:51:42','2025-06-19 12:51:42'),(23,4,1,7,30,'2025-06-19 12:51:42','2025-06-19 12:51:42'),(24,4,2,1,30,'2025-06-19 12:51:42','2025-06-19 12:51:42'),(25,4,2,2,15,'2025-06-19 12:51:42','2025-06-19 12:51:42'),(26,4,2,7,30,'2025-06-19 12:51:42','2025-06-19 12:51:42');
/*!40000 ALTER TABLE `exam_subjects` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `exam_types`
--

DROP TABLE IF EXISTS `exam_types`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `exam_types` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `is_mock_exam` tinyint(1) NOT NULL DEFAULT '0',
  `description` text,
  `time_limit_minutes` int DEFAULT NULL,
  `total_marks` int DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=224 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `exam_types`
--

LOCK TABLES `exam_types` WRITE;
/*!40000 ALTER TABLE `exam_types` DISABLE KEYS */;
INSERT INTO `exam_types` VALUES (1,'Mock Exam',1,NULL,75,75,'2025-06-19 12:51:42','2025-06-19 12:51:42'),(2,'Actual Exam',0,NULL,120,100,'2025-06-19 12:51:42','2025-06-19 12:51:42');
/*!40000 ALTER TABLE `exam_types` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `question_options`
--

DROP TABLE IF EXISTS `question_options`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `question_options` (
  `id` int NOT NULL AUTO_INCREMENT,
  `question_id` int NOT NULL,
  `option_text` text NOT NULL,
  `is_correct` tinyint(1) NOT NULL DEFAULT '0',
  `option_order` int NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_options_question_id` (`question_id`),
  CONSTRAINT `question_options_ibfk_1` FOREIGN KEY (`question_id`) REFERENCES `questions` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=37 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `question_options`
--

LOCK TABLES `question_options` WRITE;
/*!40000 ALTER TABLE `question_options` DISABLE KEYS */;
INSERT INTO `question_options` VALUES (1,1,'a',1,1,'2025-06-21 18:43:25','2025-06-21 18:43:25'),(2,1,'c',0,2,'2025-06-21 18:43:25','2025-06-21 18:43:25'),(3,1,'d',0,3,'2025-06-21 18:43:25','2025-06-21 18:43:25'),(4,1,'e',0,4,'2025-06-21 18:43:25','2025-06-21 18:43:25'),(5,2,'a',0,1,'2025-06-21 18:43:57','2025-06-21 18:43:57'),(6,2,'e',0,2,'2025-06-21 18:43:57','2025-06-21 18:43:57'),(7,2,'d',0,3,'2025-06-21 18:43:57','2025-06-21 18:43:57'),(8,2,'c',1,4,'2025-06-21 18:43:57','2025-06-21 18:43:57'),(9,3,'x=3',0,1,'2025-06-21 18:56:41','2025-06-21 18:56:41'),(10,3,'y=2',0,2,'2025-06-21 18:56:41','2025-06-21 18:56:41'),(11,3,'y=5',1,3,'2025-06-21 18:56:41','2025-06-21 18:56:41'),(12,3,'x=4',0,4,'2025-06-21 18:56:41','2025-06-21 18:56:41'),(13,4,'a',0,1,'2025-06-21 18:57:49','2025-06-21 18:57:49'),(14,4,'v',0,2,'2025-06-21 18:57:49','2025-06-21 18:57:49'),(15,4,'c',1,3,'2025-06-21 18:57:49','2025-06-21 18:57:49'),(16,4,'e',0,4,'2025-06-21 18:57:49','2025-06-21 18:57:49'),(17,5,'A',0,1,'2025-06-21 19:00:30','2025-06-21 19:00:30'),(18,5,'B',0,2,'2025-06-21 19:00:30','2025-06-21 19:00:30'),(19,5,'D',1,3,'2025-06-21 19:00:30','2025-06-21 19:00:30'),(20,5,'G',0,4,'2025-06-21 19:00:30','2025-06-21 19:00:30'),(21,6,'\\[f(x) = ax^2 + bx + c\\]',1,1,'2025-06-21 19:01:55','2025-06-21 19:01:55'),(22,6,'x=2',0,2,'2025-06-21 19:01:55','2025-06-21 19:01:55'),(23,6,'y=5',0,3,'2025-06-21 19:01:55','2025-06-21 19:01:55'),(24,6,'x=1',0,4,'2025-06-21 19:01:55','2025-06-21 19:01:55'),(25,7,'e',0,1,'2025-06-22 14:22:26','2025-06-22 14:22:26'),(26,7,'a',0,2,'2025-06-22 14:22:26','2025-06-22 14:22:26'),(27,7,'d',0,3,'2025-06-22 14:22:26','2025-06-22 14:22:26'),(28,7,'h',1,4,'2025-06-22 14:22:26','2025-06-22 14:22:26'),(29,8,'gd',1,1,'2025-06-22 14:22:58','2025-06-22 14:22:58'),(30,8,'ge',0,2,'2025-06-22 14:22:58','2025-06-22 14:22:58'),(31,8,'vd',0,3,'2025-06-22 14:22:58','2025-06-22 14:22:58'),(32,8,'etg',0,4,'2025-06-22 14:22:58','2025-06-22 14:22:58'),(33,9,'a',1,1,'2025-06-22 14:33:00','2025-06-22 14:33:00'),(34,9,'g',0,2,'2025-06-22 14:33:00','2025-06-22 14:33:00'),(35,9,'e',0,3,'2025-06-22 14:33:00','2025-06-22 14:33:00'),(36,9,'d',0,4,'2025-06-22 14:33:00','2025-06-22 14:33:00');
/*!40000 ALTER TABLE `question_options` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `question_papers`
--

DROP TABLE IF EXISTS `question_papers`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `question_papers` (
  `id` int NOT NULL AUTO_INCREMENT,
  `title` varchar(255) NOT NULL,
  `description` text,
  `school_id` int NOT NULL,
  `exam_type_id` int NOT NULL,
  `total_questions` int NOT NULL,
  `subjects` text NOT NULL,
  `questions_per_subject` text NOT NULL,
  `time_limit_minutes` int NOT NULL,
  `total_marks` int NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `created_by` int DEFAULT NULL,
  `is_result_published` tinyint(1) NOT NULL DEFAULT '0',
  `pass_mark` decimal(5,2) DEFAULT '40.00',
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_school_exam_type` (`school_id`,`exam_type_id`),
  KEY `created_by` (`created_by`),
  KEY `idx_question_papers_school_id` (`school_id`),
  KEY `idx_question_papers_exam_type_id` (`exam_type_id`),
  KEY `idx_question_papers_is_published` (`is_result_published`),
  CONSTRAINT `question_papers_ibfk_1` FOREIGN KEY (`school_id`) REFERENCES `schools` (`id`) ON DELETE CASCADE,
  CONSTRAINT `question_papers_ibfk_2` FOREIGN KEY (`exam_type_id`) REFERENCES `exam_types` (`id`) ON DELETE CASCADE,
  CONSTRAINT `question_papers_ibfk_3` FOREIGN KEY (`created_by`) REFERENCES `users` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB AUTO_INCREMENT=489 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `question_papers`
--

LOCK TABLES `question_papers` WRITE;
/*!40000 ALTER TABLE `question_papers` DISABLE KEYS */;
INSERT INTO `question_papers` VALUES (1,'Engineering Mock Exam','Mock exam for School of Engineering & Technology',1,1,75,'English, General Mathematics, Higher Math & Physics','30, 15, 30',75,75,'2025-06-21 18:42:20','2025-06-23 09:32:16',1,0,40.00),(2,'School of Engineering & Technology Exam','Exam for School of Engineering & Technology',1,2,100,'English, General Mathematics, Higher Math & Physics','40, 20, 40',120,100,'2025-06-21 18:42:20','2025-06-23 09:32:16',1,0,40.00),(3,'Business Mock Exam','Mock exam for School of Business & Economics',2,1,75,'English, General Mathematics, Business & Economics','30, 15, 30',75,75,'2025-06-21 18:42:20','2025-06-23 09:32:16',1,0,40.00),(4,'School of Business & Economics Exam','Exam for School of Business & Economics',2,2,100,'English, General Mathematics, Business & Economics','40, 20, 40',120,100,'2025-06-21 18:42:20','2025-06-23 09:32:16',1,0,40.00),(5,'Humanities Mock Exam','Mock exam for School of Humanities & Social Sciences',3,1,75,'English, General Mathematics, Current Affairs, Higher English & Logical Reasoning','30, 15, 15, 15',75,75,'2025-06-21 18:42:20','2025-06-23 09:32:16',1,0,40.00),(6,'School of Humanities & Social Sciences Exam','Exam for School of Humanities & Social Sciences',3,2,100,'English, General Mathematics, Current Affairs, Higher English & Logical Reasoning','40, 20, 20, 20',120,100,'2025-06-21 18:42:20','2025-06-23 09:32:16',1,1,50.00),(7,'Life Sciences Mock Exam','Mock exam for School of Life Sciences',4,1,75,'English, General Mathematics, Biology & Chemistry','30, 15, 30',75,75,'2025-06-21 18:42:20','2025-06-23 09:32:16',1,0,40.00),(8,'School of Life Sciences Exam','Exam for School of Life Sciences',4,2,100,'English, General Mathematics, Biology & Chemistry','40, 20, 40',120,100,'2025-06-21 18:42:20','2025-06-23 09:32:16',1,0,40.00);
/*!40000 ALTER TABLE `question_papers` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `questions`
--

DROP TABLE IF EXISTS `questions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `questions` (
  `id` int NOT NULL AUTO_INCREMENT,
  `question_paper_id` int NOT NULL,
  `subject_id` int NOT NULL,
  `question_text` text NOT NULL,
  `has_image` tinyint(1) NOT NULL DEFAULT '0',
  `image_path` varchar(255) DEFAULT NULL,
  `has_latex` tinyint(1) NOT NULL DEFAULT '0',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_questions_paper_id` (`question_paper_id`),
  KEY `idx_questions_subject_id` (`subject_id`),
  CONSTRAINT `questions_ibfk_1` FOREIGN KEY (`question_paper_id`) REFERENCES `question_papers` (`id`) ON DELETE CASCADE,
  CONSTRAINT `questions_ibfk_2` FOREIGN KEY (`subject_id`) REFERENCES `subjects` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `questions`
--

LOCK TABLES `questions` WRITE;
/*!40000 ALTER TABLE `questions` DISABLE KEYS */;
INSERT INTO `questions` VALUES (1,3,4,'Testing Questions ?',0,NULL,0,'2025-06-21 18:43:25','2025-06-21 18:43:25'),(2,6,5,'Testing Questions?',0,NULL,0,'2025-06-21 18:43:57','2025-06-21 18:43:57'),(3,1,2,'Solve this Equation:    \\[\nf(x) = \\begin{cases}\nx^2 & \\text{if } x \\geq 0 \\\\\n-x^2 & \\text{if } x < 0\n\\end{cases}\n\\]',0,NULL,1,'2025-06-21 18:56:41','2025-06-21 18:56:41'),(4,8,7,'Solve this questions:',1,'http://res.cloudinary.com/cloudinary203/image/upload/v1750532257/qcnuzs6104nc6blevgdq.jpg',0,'2025-06-21 18:57:49','2025-06-21 18:57:49'),(5,1,1,'Which is the correct spelling?',0,NULL,0,'2025-06-21 19:00:30','2025-06-21 19:00:30'),(6,1,3,'Solve this Problem: \\[\nf(x) = \\begin{cases}\nx^2 & \\text{if } x \\geq 0 \\\\\n-x^2 & \\text{if } x < 0\n\\end{cases}\n\\]',0,NULL,1,'2025-06-21 19:01:55','2025-06-21 19:01:55'),(7,1,1,'Test Question for English 2?',0,NULL,0,'2025-06-22 14:22:26','2025-06-22 14:22:26'),(8,1,1,'Which is the correct spelling?',0,NULL,0,'2025-06-22 14:22:58','2025-06-22 14:22:58'),(9,1,3,'Question for Higher Math and Physics?',0,NULL,0,'2025-06-22 14:33:00','2025-06-22 14:33:00');
/*!40000 ALTER TABLE `questions` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `schools`
--

DROP TABLE IF EXISTS `schools`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `schools` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `description` text,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=444 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `schools`
--

LOCK TABLES `schools` WRITE;
/*!40000 ALTER TABLE `schools` DISABLE KEYS */;
INSERT INTO `schools` VALUES (1,'School of Engineering & Technology','School of Engineering & Technology at UIU','2025-06-19 12:51:42','2025-06-19 12:51:42'),(2,'School of Business & Economics','School of Business & Economics at UIU','2025-06-19 12:51:42','2025-06-19 12:51:42'),(3,'School of Humanities & Social Sciences','School of Humanities & Social Sciences at UIU','2025-06-19 12:51:42','2025-06-19 12:51:42'),(4,'School of Life Sciences','School of Life Sciences at UIU','2025-06-19 12:51:42','2025-06-19 12:51:42');
/*!40000 ALTER TABLE `schools` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `student_responses`
--

DROP TABLE IF EXISTS `student_responses`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `student_responses` (
  `id` int NOT NULL AUTO_INCREMENT,
  `student_id` int NOT NULL,
  `question_id` int NOT NULL,
  `selected_option_id` int DEFAULT NULL,
  `is_correct` tinyint(1) NOT NULL DEFAULT '0',
  `response_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `selected_option_id` (`selected_option_id`),
  KEY `idx_responses_student_id` (`student_id`),
  KEY `idx_responses_question_id` (`question_id`),
  CONSTRAINT `student_responses_ibfk_1` FOREIGN KEY (`student_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  CONSTRAINT `student_responses_ibfk_2` FOREIGN KEY (`question_id`) REFERENCES `questions` (`id`) ON DELETE CASCADE,
  CONSTRAINT `student_responses_ibfk_3` FOREIGN KEY (`selected_option_id`) REFERENCES `question_options` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `student_responses`
--

LOCK TABLES `student_responses` WRITE;
/*!40000 ALTER TABLE `student_responses` DISABLE KEYS */;
INSERT INTO `student_responses` VALUES (1,1,2,8,1,'2025-06-21 18:44:39'),(2,1,2,7,0,'2025-06-21 19:03:46'),(3,2,2,5,0,'2025-06-22 20:09:42'),(4,2,2,6,0,'2025-06-22 20:50:51'),(5,2,2,6,0,'2025-06-22 21:07:17'),(6,2,2,5,0,'2025-06-23 09:14:12'),(7,2,2,6,0,'2025-06-23 09:27:18');
/*!40000 ALTER TABLE `student_responses` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `subjects`
--

DROP TABLE IF EXISTS `subjects`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `subjects` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `description` text,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=774 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `subjects`
--

LOCK TABLES `subjects` WRITE;
/*!40000 ALTER TABLE `subjects` DISABLE KEYS */;
INSERT INTO `subjects` VALUES (1,'English',NULL,'2025-06-19 12:51:42','2025-06-19 12:51:42'),(2,'General Mathematics',NULL,'2025-06-19 12:51:42','2025-06-19 12:51:42'),(3,'Higher Math & Physics',NULL,'2025-06-19 12:51:42','2025-06-19 12:51:42'),(4,'Business & Economics',NULL,'2025-06-19 12:51:42','2025-06-19 12:51:42'),(5,'Current Affairs',NULL,'2025-06-19 12:51:42','2025-06-19 12:51:42'),(6,'Higher English & Logical Reasoning',NULL,'2025-06-19 12:51:42','2025-06-19 12:51:42'),(7,'Biology & Chemistry',NULL,'2025-06-19 12:51:42','2025-06-19 12:51:42');
/*!40000 ALTER TABLE `subjects` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_status`
--

DROP TABLE IF EXISTS `user_status`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_status` (
  `user_id` int NOT NULL,
  `status` varchar(20) DEFAULT 'offline',
  `last_active` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`user_id`),
  KEY `status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_status`
--

LOCK TABLES `user_status` WRITE;
/*!40000 ALTER TABLE `user_status` DISABLE KEYS */;
INSERT INTO `user_status` VALUES (1,'offline','2025-06-23 09:32:16'),(2,'offline','2025-06-23 09:32:16'),(3,'offline','2025-06-23 09:32:16'),(4,'offline','2025-06-23 09:32:16'),(5,'offline','2025-06-23 09:32:16'),(6,'offline','2025-06-23 09:32:16');
/*!40000 ALTER TABLE `user_status` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `id` int NOT NULL AUTO_INCREMENT,
  `first_name` varchar(50) NOT NULL,
  `last_name` varchar(50) NOT NULL,
  `email` varchar(100) NOT NULL,
  `phone` varchar(20) NOT NULL,
  `address` varchar(255) NOT NULL,
  `city` varchar(50) NOT NULL,
  `country` varchar(50) NOT NULL,
  `password` varchar(255) NOT NULL,
  `role` enum('student','admin') NOT NULL DEFAULT 'student',
  `last_login_time` timestamp NULL DEFAULT NULL,
  `ip_address` varchar(45) DEFAULT NULL,
  `is_logged_in` tinyint(1) DEFAULT '0',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `email` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES (1,'Admin','User','admin@uiu.ac.bd','12345678','UIU Campus','Dhaka','Bangladesh','admin123','admin','2025-06-23 09:28:49','192.168.0.104',0,'2025-05-04 15:38:18','2025-06-23 09:32:05'),(2,'Atikur','Rahaman','atikurrahaman0304@gmail.com','01723383575','Dhaka','Dhaka','Bangladesh','atik1234','student','2025-06-23 09:24:32','192.168.0.104',0,'2025-05-04 15:42:10','2025-06-23 09:28:37'),(3,'Test','User','testuser@gmail.com','01723432342','Dhaka','Dhaka','Bangladesh','testuser','student','2025-06-23 08:22:06','192.168.0.104',0,'2025-05-31 07:10:58','2025-06-23 08:23:03'),(4,'Atikur','Rahaman','admin@gmail.com','0172349484','Sayednagar','Dhaka','Bangladesh','admin','admin','2025-06-23 08:14:17','192.168.0.104',0,'2025-06-22 15:36:06','2025-06-23 08:15:12'),(5,'Test ','Admin','testadmin@gmail.com','0172349503','NA','Dhaka','Bangladesh','admin','admin',NULL,NULL,0,'2025-06-22 15:39:46','2025-06-22 15:39:46'),(6,'Test','Admin1','testadmin1@gmail.com','017494329','NA','Dhaka','Bangladesh','admin','admin',NULL,NULL,0,'2025-06-22 15:43:48','2025-06-22 15:43:48');
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

-- Dump completed on 2025-06-23 15:51:37
