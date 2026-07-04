package vn.edu.shiningenglish.shiningenglishapi.model.dto.transaction.checkout;

import vn.edu.shiningenglish.shiningenglishapi.model.entity.Order;
import vn.edu.shiningenglish.shiningenglishapi.service.order.strategy.CheckoutPaymentActionResponse;
import java.util.LinkedHashMap;
import java.util.Map;

public class CheckoutOrderResponse {
    private final Order order;
    private final CheckoutPaymentActionResponse paymentAction;

    public CheckoutOrderResponse(Order order, CheckoutPaymentActionResponse paymentAction) {
        this.order = order;
        this.paymentAction = paymentAction;
    }

    public Map<String, Object> toArray() {
        var map = new LinkedHashMap<String, Object>();
        map.put("order", order);
        if (paymentAction != null) map.put("payment_action", paymentAction.toArray());
        return map;
    }

    public Order getOrder() { return order; }
    public CheckoutPaymentActionResponse getPaymentAction() { return paymentAction; }
}
