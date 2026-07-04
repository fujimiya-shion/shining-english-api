CREATE TABLE lesson_star_rewards (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT UNSIGNED NOT NULL,
    course_id BIGINT UNSIGNED NOT NULL,
    lesson_id BIGINT UNSIGNED DEFAULT 0,
    source VARCHAR(50) DEFAULT NULL,
    amount INT DEFAULT 0,
    awarded_at TIMESTAMP NULL DEFAULT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_star_rewards_user (user_id),
    INDEX idx_star_rewards_course (course_id),
    INDEX idx_star_rewards_lesson (lesson_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
