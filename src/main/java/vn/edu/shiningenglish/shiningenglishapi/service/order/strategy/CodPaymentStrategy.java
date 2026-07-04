package vn.edu.shiningenglish.shiningenglishapi.service.order.strategy;

import org.springframework.stereotype.Component;
import vn.edu.shiningenglish.shiningenglishapi.enums.PaymentMethod;
import vn.edu.shiningenglish.shiningenglishapi.model.entity.Order;

import java.util.Map;

@Component
public class CodPaymentStrategy implements PaymentStrategy {
    @Override
    public PaymentMethod method() { return PaymentMethod.cod; }

    @Override
    public PaymentInitializationResult initialize(Order order, Map<String, Object> customerData) {
        return PaymentInitializationResult.none();
    }

    @Override
    public Order refresh(Order order) { return order; }

    @Override
    public Order cancel(Order order, String reason) { return order; }

    @Override
    public Order handleWebhook(Map<String, Object> payload) { return null; }
}
