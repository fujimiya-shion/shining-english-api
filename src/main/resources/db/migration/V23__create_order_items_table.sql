CREATE TABLE order_items (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT UNSIGNED NOT NULL,
    course_id BIGINT UNSIGNED NOT NULL,
    quantity INT DEFAULT 1,
    price INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uq_order_item (order_id, course_id),
    INDEX idx_order_items_order (order_id),
    INDEX idx_order_items_course (course_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
