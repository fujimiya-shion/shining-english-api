package vn.edu.shiningenglish.shiningenglishapi.service.order.strategy;

import vn.edu.shiningenglish.shiningenglishapi.model.entity.Order;
import java.util.Map;

public interface PaymentStrategy {
    Map<String, Object> initialize(Order order, Map<String, Object> customerData);
    void cancel(Order order, String reason);
    Map<String, Object> refresh(Order order);
}
