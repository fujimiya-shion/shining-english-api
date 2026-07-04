CREATE TABLE course_reviews (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    course_id BIGINT UNSIGNED NOT NULL,
    user_id BIGINT UNSIGNED NOT NULL,
    rating INT NOT NULL,
    content TEXT DEFAULT NULL,
    deleted_at TIMESTAMP NULL DEFAULT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uq_course_review (course_id, user_id),
    INDEX idx_course_reviews_course (course_id),
    INDEX idx_course_reviews_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
