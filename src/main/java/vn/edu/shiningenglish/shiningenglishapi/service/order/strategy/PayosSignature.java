package vn.edu.shiningenglish.shiningenglishapi.service.order.strategy;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.TreeMap;

public class PayosSignature {

    public static String sign(Map<String, Object> data, String checksumKey) {
        var sorted = new TreeMap<>(data);
        var segments = new StringBuilder();
        var first = true;
        for (var entry : sorted.entrySet()) {
            if (!first) segments.append("&");
            segments.append(entry.getKey()).append("=").append(normalize(entry.getValue()));
            first = false;
        }
        return hmacSha256(segments.toString(), checksumKey);
    }

    public static boolean verify(Map<String, Object> data, String signature, String checksumKey) {
        return signature.equalsIgnoreCase(sign(data, checksumKey));
    }

    private static String normalize(Object value) {
        if (value == null) return "";
        if (value instanceof Boolean) return (Boolean) value ? "true" : "false";
        return value.toString();
    }

    private static String hmacSha256(String data, String key) {
        try {
            var mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            var bytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            var hex = new StringBuilder();
            for (byte b : bytes) hex.append(String.format("%02x", b));
            return hex.toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to compute HMAC-SHA256", e);
        }
    }
}
