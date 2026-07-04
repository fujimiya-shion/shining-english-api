package vn.edu.shiningenglish.shiningenglishapi.service.order.strategy;

import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import vn.edu.shiningenglish.shiningenglishapi.enums.PaymentMethod;
import java.util.Map;

@Component
public class PaymentStrategyFactory {

    private final Map<PaymentMethod, PaymentStrategy> strategies;

    public PaymentStrategyFactory(ApplicationContext ctx) {
        this.strategies = Map.of(
            PaymentMethod.cod, ctx.getBean(CodPaymentStrategy.class),
            PaymentMethod.payos, ctx.getBean(PayosPaymentStrategy.class),
            PaymentMethod.star, ctx.getBean(StarPaymentStrategy.class)
        );
    }

    public PaymentStrategy make(PaymentMethod method) {
        var strategy = strategies.get(method);
        if (strategy == null) throw new IllegalArgumentException("Unsupported payment method: " + method);
        return strategy;
    }
}
