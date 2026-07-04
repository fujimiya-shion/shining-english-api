package vn.edu.shiningenglish.shiningenglishapi.repository.order;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.shiningenglish.shiningenglishapi.model.entity.OrderItem;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
}
