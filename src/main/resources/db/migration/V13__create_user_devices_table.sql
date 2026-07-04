CREATE TABLE user_devices (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT UNSIGNED NOT NULL,
    personal_access_token_id BIGINT UNSIGNED DEFAULT NULL,
    device_identifier VARCHAR(255) NOT NULL,
    device_name VARCHAR(255) DEFAULT NULL,
    platform VARCHAR(100) DEFAULT NULL,
    ip_address VARCHAR(45) DEFAULT NULL,
    user_agent TEXT DEFAULT NULL,
    logged_in_at TIMESTAMP NULL DEFAULT NULL,
    last_seen_at TIMESTAMP NULL DEFAULT NULL,
    logged_out_at TIMESTAMP NULL DEFAULT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_devices_user (user_id),
    INDEX idx_user_devices_token (personal_access_token_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
