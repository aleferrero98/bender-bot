-- Initialization script for Bender Bot database
-- DDL statements - Data Definition Language (CREATE, ALTER, DROP, TRUNCATE)

CREATE DATABASE IF NOT EXISTS bender_bot CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE bender_bot;

-- Tables
CREATE TABLE IF NOT EXISTS `tunnel` (
    `id` int unsigned NOT NULL AUTO_INCREMENT,
    `url` VARCHAR(256) NOT NULL,
    `exposed_port` int NOT NULL,
    `status` ENUM('ACTIVE','CANCELLED','EXPIRED') NOT NULL DEFAULT 'ACTIVE',
    `expires_at` DATETIME NOT NULL,
    `process_id` int NOT NULL,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT NULL,
    PRIMARY KEY (`id`)
);

CREATE TABLE IF NOT EXISTS `frequent_service` (
    `id` int unsigned NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(64) NOT NULL,
    `port` int NOT NULL,
    `short_io_url` VARCHAR(128) NOT NULL,
    `short_io_link_id` VARCHAR(64) NOT NULL,
    `status` ENUM('ENABLED','DISABLED') NOT NULL DEFAULT 'ENABLED',
    `tunnel_id` int unsigned,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT NULL,
    PRIMARY KEY (`id`),
    CONSTRAINT fk_tunnel_id FOREIGN KEY (tunnel_id) REFERENCES tunnel(id)
);

SELECT 'Bender Bot database initialized successfully!' as status;
