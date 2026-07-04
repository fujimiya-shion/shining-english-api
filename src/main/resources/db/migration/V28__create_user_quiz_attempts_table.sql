CREATE TABLE user_quiz_attempts (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT UNSIGNED NOT NULL,
    quiz_id BIGINT UNSIGNED NOT NULL,
    score_percent DOUBLE DEFAULT NULL,
    passed TINYINT(1) DEFAULT 0,
    submitted_at TIMESTAMP NULL DEFAULT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_quiz_attempts_user (user_id),
    INDEX idx_user_quiz_attempts_quiz (quiz_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
