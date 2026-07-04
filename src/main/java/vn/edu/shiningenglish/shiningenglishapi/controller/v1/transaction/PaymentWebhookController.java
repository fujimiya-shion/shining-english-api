package vn.edu.shiningenglish.shiningenglishapi.controller.v1.transaction;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.shiningenglish.shiningenglishapi.common.BaseController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/payments/webhooks")
public class PaymentWebhookController extends BaseController {

    @PostMapping("/payos")
    public ResponseEntity<Map<String, Object>> payos(@RequestBody Map<String, Object> body) {
        // Simplified - would integrate with PayOS
        return success(Map.of("processed", true, "order_id", null));
    }
}
