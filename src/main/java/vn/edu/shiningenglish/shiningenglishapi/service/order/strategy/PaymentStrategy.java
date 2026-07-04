package vn.edu.shiningenglish.shiningenglishapi.service.order.strategy;

import vn.edu.shiningenglish.shiningenglishapi.enums.PaymentMethod;
import vn.edu.shiningenglish.shiningenglishapi.model.entity.Order;
import java.util.Map;

public interface PaymentStrategy {
    PaymentMethod method();
    PaymentInitializationResult initialize(Order order, Map<String, Object> customerData);
    Order refresh(Order order);
    Order cancel(Order order, String reason);
    Order handleWebhook(Map<String, Object> payload);
}
