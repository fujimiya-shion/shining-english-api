package vn.edu.shiningenglish.shiningenglishapi.model.dto.transaction.checkout;

import vn.edu.shiningenglish.shiningenglishapi.model.entity.Order;
import java.util.LinkedHashMap;
import java.util.Map;

public class CheckoutOrderResponse {
    private Order order;
    private Map<String, Object> paymentAction;

    public CheckoutOrderResponse(Order order, Map<String, Object> paymentAction) {
        this.order = order;
        this.paymentAction = paymentAction;
    }

    public Order getOrder() { return order; }
    public Map<String, Object> getPaymentAction() { return paymentAction; }

    public Map<String, Object> toArray() {
        var map = new LinkedHashMap<String, Object>();
        map.put("order", order);
        map.put("payment_action", paymentAction);
        return map;
    }
}
