CREATE TABLE lesson_progresses (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT UNSIGNED NOT NULL,
    course_id BIGINT UNSIGNED NOT NULL,
    lesson_id BIGINT UNSIGNED NOT NULL,
    is_current TINYINT(1) DEFAULT 0,
    completed_at TIMESTAMP NULL DEFAULT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uq_lesson_progress (user_id, course_id, lesson_id),
    INDEX idx_lesson_progresses_user (user_id),
    INDEX idx_lesson_progresses_course (course_id),
    INDEX idx_lesson_progresses_lesson (lesson_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
