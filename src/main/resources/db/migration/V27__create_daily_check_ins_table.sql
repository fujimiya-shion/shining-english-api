CREATE TABLE daily_check_ins (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT UNSIGNED NOT NULL,
    checked_in_at TIMESTAMP NOT NULL,
    reward_amount INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_daily_check_ins_user (user_id),
    INDEX idx_daily_check_ins_date (checked_in_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
