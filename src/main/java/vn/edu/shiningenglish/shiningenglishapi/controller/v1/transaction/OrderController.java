package vn.edu.shiningenglish.shiningenglishapi.controller.v1.transaction;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import vn.edu.shiningenglish.shiningenglishapi.common.BaseController;
import vn.edu.shiningenglish.shiningenglishapi.enums.PaymentMethod;
import vn.edu.shiningenglish.shiningenglishapi.model.entity.User;
import vn.edu.shiningenglish.shiningenglishapi.service.order.OrderService;
import vn.edu.shiningenglish.shiningenglishapi.valueobject.CheckoutCustomerData;
import vn.edu.shiningenglish.shiningenglishapi.valueobject.MetaPagination;
import vn.edu.shiningenglish.shiningenglishapi.valueobject.QueryOption;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController extends BaseController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> index(Authentication auth, @RequestParam Map<String, String> params) {
        var user = (User) auth.getPrincipal();
        var options = QueryOption.fromArray(params, true);
        var page = orderService.listByUserId(user.getId(), options);
        var meta = MetaPagination.fromPage(page);
        return success("OK", page.getContent(), 200, meta.toArray());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> show(Authentication auth, @PathVariable Long id) {
        var user = (User) auth.getPrincipal();
        var order = orderService.detailByUserId(user.getId(), id);
        if (order.isEmpty()) return notfound("Order not found");
        return success(order.get());
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> store(Authentication auth, @RequestBody Map<String, Object> body) {
        var user = (User) auth.getPrincipal();
        var type = (String) body.get("type");
        var paymentMethodStr = (String) body.getOrDefault("payment_method", "cod");
        var paymentMethod = PaymentMethod.valueOf(paymentMethodStr);
        var customerData = CheckoutCustomerData.fromRawMap(body);

        try {
            Map<String, Object> checkout;
            if ("cart".equals(type)) {
                checkout = orderService.createFromCart(user.getId(), paymentMethod, customerData);
            } else {
                checkout = orderService.createBuyNow(
                    user.getId(),
                    ((Number) body.get("course_id")).longValue(),
                    body.containsKey("quantity") ? ((Number) body.get("quantity")).intValue() : 1,
                    paymentMethod,
                    customerData
                );
            }
            return created(checkout, "Order created");
        } catch (RuntimeException e) {
            return error(e.getMessage(), 422);
        }
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<Map<String, Object>> cancel(Authentication auth, @PathVariable Long id) {
        var user = (User) auth.getPrincipal();
        var cancelled = orderService.cancelByUserId(user.getId(), id);
        if (!cancelled) return notfound("Order not found");
        return success("Order cancelled");
    }
}
