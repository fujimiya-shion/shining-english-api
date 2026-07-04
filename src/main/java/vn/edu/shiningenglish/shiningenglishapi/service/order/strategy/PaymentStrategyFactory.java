package vn.edu.shiningenglish.shiningenglishapi.service.order.strategy;

import org.springframework.stereotype.Component;
import vn.edu.shiningenglish.shiningenglishapi.enums.PaymentMethod;
import java.util.EnumMap;
import java.util.Map;

@Component
public class PaymentStrategyFactory {

    private final Map<PaymentMethod, PaymentStrategy> strategies = new EnumMap<>(PaymentMethod.class);

    public PaymentStrategyFactory(CodPaymentStrategy cod, PayOsPaymentStrategy payos, StarPaymentStrategy star) {
        strategies.put(PaymentMethod.cod, cod);
        strategies.put(PaymentMethod.payos, payos);
        strategies.put(PaymentMethod.star, star);
    }

    public PaymentStrategy resolve(PaymentMethod method) {
        var strategy = strategies.get(method);
        if (strategy == null) throw new IllegalArgumentException("Unsupported payment method: " + method);
        return strategy;
    }
}
