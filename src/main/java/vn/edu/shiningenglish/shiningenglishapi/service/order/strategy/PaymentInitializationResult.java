package vn.edu.shiningenglish.shiningenglishapi.service.order.strategy;

import java.util.Map;

public class PaymentInitializationResult {
    private final String actionType;
    private final String actionUrl;
    private final Map<String, Object> metadata;

    public PaymentInitializationResult(String actionType, String actionUrl, Map<String, Object> metadata) {
        this.actionType = actionType;
        this.actionUrl = actionUrl;
        this.metadata = metadata;
    }

    public static PaymentInitializationResult none() {
        return new PaymentInitializationResult(null, null, null);
    }

    public static PaymentInitializationResult redirect(String url, Map<String, Object> metadata) {
        return new PaymentInitializationResult("redirect", url, metadata);
    }

    public String getActionType() { return actionType; }
    public String getActionUrl() { return actionUrl; }
    public Map<String, Object> getMetadata() { return metadata; }

    public CheckoutPaymentActionResponse toCheckoutAction() {
        if (actionType == null || actionUrl == null) return null;
        return new CheckoutPaymentActionResponse(actionType, actionUrl, metadata);
    }
}
