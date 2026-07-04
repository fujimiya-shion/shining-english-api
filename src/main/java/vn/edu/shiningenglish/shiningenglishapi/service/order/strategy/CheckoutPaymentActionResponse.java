package vn.edu.shiningenglish.shiningenglishapi.service.order.strategy;

import java.util.LinkedHashMap;
import java.util.Map;

public class CheckoutPaymentActionResponse {
    private final String type;
    private final String url;
    private final Map<String, Object> metadata;

    public CheckoutPaymentActionResponse(String type, String url, Map<String, Object> metadata) {
        this.type = type;
        this.url = url;
        this.metadata = metadata;
    }

    public Map<String, Object> toArray() {
        var map = new LinkedHashMap<String, Object>();
        map.put("type", type);
        map.put("url", url);
        if (metadata != null) map.put("metadata", metadata);
        return map;
    }

    public String getType() { return type; }
    public String getUrl() { return url; }
    public Map<String, Object> getMetadata() { return metadata; }
}
