package vn.edu.shiningenglish.shiningenglishapi.service.order.strategy;

import org.springframework.stereotype.Component;
import vn.edu.shiningenglish.shiningenglishapi.model.entity.Order;
import java.util.Map;

@Component
public class CodPaymentStrategy implements PaymentStrategy {
    @Override
    public Map<String, Object> initialize(Order order, Map<String, Object> customerData) {
        return Map.of("method", "cod", "url", "");
    }

    @Override
    public void cancel(Order order, String reason) {
    }

    @Override
    public Map<String, Object> refresh(Order order) {
        return Map.of("method", "cod", "status", order.getStatus().name());
    }
}
