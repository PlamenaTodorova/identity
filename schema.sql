CREATE DATABASE IF NOT EXISTS `identity`;

USE `identity`;

CREATE TABLE `users` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `email` VARCHAR(255) NOT NULL,
  `password_hash` VARCHAR(255) NOT NULL,
  `display_name` VARCHAR(120) NOT NULL,
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY `uk_users_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `user_app_preferences` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `user_id` BIGINT NOT NULL,
  `app_id` VARCHAR(64) NOT NULL,
  `enabled` BOOLEAN NOT NULL DEFAULT TRUE,
  UNIQUE KEY `uk_user_app_preferences_user_app` (`user_id`, `app_id`),
  CONSTRAINT `fk_user_app_preferences_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `refresh_tokens` (
  `token` VARCHAR(64) PRIMARY KEY,
  `user_id` BIGINT NOT NULL,
  `expires_at` TIMESTAMP NOT NULL,
  `revoked` BOOLEAN NOT NULL DEFAULT FALSE,
  CONSTRAINT `fk_refresh_tokens_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
