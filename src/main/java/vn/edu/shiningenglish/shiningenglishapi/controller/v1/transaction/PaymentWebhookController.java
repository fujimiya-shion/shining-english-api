package vn.edu.shiningenglish.shiningenglishapi.controller.v1.transaction;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.shiningenglish.shiningenglishapi.common.BaseController;
import vn.edu.shiningenglish.shiningenglishapi.service.order.strategy.PaymentStrategyFactory;
import vn.edu.shiningenglish.shiningenglishapi.enums.PaymentMethod;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/payments/webhooks")
public class PaymentWebhookController extends BaseController {

    private final PaymentStrategyFactory strategyFactory;

    public PaymentWebhookController(PaymentStrategyFactory strategyFactory) {
        this.strategyFactory = strategyFactory;
    }

    @PostMapping("/payos")
    public ResponseEntity<Map<String, Object>> payos(@RequestBody Map<String, Object> body) {
        try {
            var strategy = strategyFactory.make(PaymentMethod.payos);
            var order = strategy.handleWebhook(body);
            if (order == null) {
                return success(Map.of("processed", true, "order_id", null));
            }
            return success(Map.of("processed", true, "order_id", order.getId()));
        } catch (RuntimeException e) {
            return error(e.getMessage(), 422);
        }
    }
}
