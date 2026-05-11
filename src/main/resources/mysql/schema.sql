-- Initialization script for Bender Bot database
-- DDL statements - Data Definition Language (CREATE, ALTER, DROP, TRUNCATE)

CREATE DATABASE IF NOT EXISTS bender_bot CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE bender_bot;

-- Tables
CREATE TABLE IF NOT EXISTS `tunnel` (
    `tunnel_id` int unsigned NOT NULL AUTO_INCREMENT,
    `url` VARCHAR(256) NOT NULL,
    `exposed_port` int NOT NULL,
    `status` ENUM('ENABLED','DISABLED') NOT NULL DEFAULT 'ENABLED',
    `expires_at` DATETIME NOT NULL,
    `process_id` int NOT NULL,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT NULL,
    PRIMARY KEY (`tunnel_id`)
);

SELECT 'Bender Bot database initialized successfully!' as status;
