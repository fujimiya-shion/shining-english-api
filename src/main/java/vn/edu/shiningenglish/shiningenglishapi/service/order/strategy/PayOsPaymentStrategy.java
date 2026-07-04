package vn.edu.shiningenglish.shiningenglishapi.service.order.strategy;

import org.springframework.stereotype.Component;
import vn.edu.shiningenglish.shiningenglishapi.model.entity.Order;
import java.util.Map;

@Component
public class PayOsPaymentStrategy implements PaymentStrategy {
    @Override
    public Map<String, Object> initialize(Order order, Map<String, Object> customerData) {
        return Map.of("method", "payos", "url", order.getPaymentCheckoutUrl() != null ? order.getPaymentCheckoutUrl() : "");
    }

    @Override
    public void cancel(Order order, String reason) {
    }

    @Override
    public Map<String, Object> refresh(Order order) {
        return Map.of("method", "payos", "status", order.getStatus().name());
    }
}
