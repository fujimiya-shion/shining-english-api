package vn.edu.shiningenglish.shiningenglishapi.service.order.strategy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import vn.edu.shiningenglish.shiningenglishapi.enums.OrderStatus;
import vn.edu.shiningenglish.shiningenglishapi.enums.PaymentMethod;
import vn.edu.shiningenglish.shiningenglishapi.model.entity.Order;
import vn.edu.shiningenglish.shiningenglishapi.repository.order.OrderRepository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.*;

@Component
public class PayosPaymentStrategy implements PaymentStrategy {

    private static final Logger log = LoggerFactory.getLogger(PayosPaymentStrategy.class);

    private final OrderRepository orderRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${payos.base_url:https://api-merchant.payos.vn}")
    private String baseUrl;

    @Value("${payos.client_id:}")
    private String clientId;

    @Value("${payos.api_key:}")
    private String apiKey;

    @Value("${payos.checksum_key:}")
    private String checksumKey;

    @Value("${app.frontend_url:http://localhost:3000}")
    private String frontendUrl;

    public PayosPaymentStrategy(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override
    public PaymentMethod method() {
        return PaymentMethod.payos;
    }

    @Override
    public PaymentInitializationResult initialize(Order order, Map<String, Object> customerData) {
        if (order.getTotalAmount() == null || order.getTotalAmount() <= 0) {
            return PaymentInitializationResult.none();
        }

        var orderCode = order.getId().intValue();
        var returnUrl = frontendUrl + "/payment/success?orderCode=" + orderCode;
        var cancelUrl = frontendUrl + "/payment/fail?orderCode=" + orderCode;
        var description = ("SE" + orderCode).substring(0, Math.min(9, ("SE" + orderCode).length()));

        var itemsPayload = new ArrayList<Map<String, Object>>();

        var payload = new LinkedHashMap<String, Object>();
        payload.put("orderCode", orderCode);
        payload.put("amount", order.getTotalAmount());
        payload.put("description", description);
        payload.put("buyerName", customerData.getOrDefault("buyer_name", customerData.getOrDefault("fullName", "")));
        payload.put("buyerEmail", customerData.getOrDefault("buyer_email", customerData.getOrDefault("email", "")));
        payload.put("buyerPhone", customerData.getOrDefault("buyer_phone", customerData.getOrDefault("phone", "")));
        payload.put("items", itemsPayload);
        payload.put("cancelUrl", cancelUrl);
        payload.put("returnUrl", returnUrl);
        payload.put("signature", PayosSignature.sign(Map.of(
            "amount", order.getTotalAmount(),
            "cancelUrl", cancelUrl,
            "description", description,
            "orderCode", orderCode,
            "returnUrl", returnUrl
        ), checksumKey));

        try {
            var response = request("POST", "/v2/payment-requests", payload);
            
            if (response.getStatusCode().isError()) {
                throw new RuntimeException("Failed to create payOS payment link.");
            }

            @SuppressWarnings("unchecked")
            var body = response.getBody();
            var code = body != null ? body.get("code") : null;
            if (!"00".equals(code)) {
                var desc = body != null ? body.get("desc") : null;
                throw new RuntimeException(desc instanceof String ? (String) desc : "Failed to create payOS payment link.");
            }

            @SuppressWarnings("unchecked")
            var data = body != null ? (Map<String, Object>) body.get("data") : null;
            var checkoutUrl = data != null ? (String) data.get("checkoutUrl") : null;
            if (data == null || checkoutUrl == null) {
                throw new RuntimeException("payOS did not return a checkout URL.");
            }

            var paymentLinkId = (String) data.get("paymentLinkId");
            
            var metadata = new LinkedHashMap<String, Object>();
            metadata.put("provider", "payos");
            metadata.put("provider_status", data.get("status"));
            metadata.put("qr_code", data.get("qrCode"));
            metadata.put("raw_create_link_response", data);

            order.setPaymentReference(paymentLinkId);
            order.setPaymentCheckoutUrl(checkoutUrl);
            order.setPaymentMetadata(toJsonString(metadata));
            orderRepository.save(order);

            return PaymentInitializationResult.redirect(checkoutUrl, Map.of(
                "provider", "payos",
                "payment_link_id", paymentLinkId != null ? paymentLinkId : "",
                "status", data.get("status") != null ? data.get("status") : ""
            ));

        } catch (Exception e) {
            log.error("PayOS initialize error", e);
            throw new RuntimeException(e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Order refresh(Order order) {
        if (order.getPaymentMethod() != PaymentMethod.payos
            || order.getPaymentReference() == null || order.getPaymentReference().isBlank()) {
            return order;
        }

        try {
            var response = request("GET", "/v2/payment-requests/" + order.getPaymentReference(), null);
            if (response.getStatusCode().isError()) return order;

            var body = response.getBody();
            var code = body != null ? body.get("code") : null;
            if (!"00".equals(code)) return order;

            var data = (Map<String, Object>) body.get("data");
            if (data == null) return order;

            var providerStatus = ((String) data.getOrDefault("status", "PENDING")).toUpperCase();
            var nextStatus = mapProviderStatus(providerStatus);

            var metadata = new LinkedHashMap<String, Object>();
            if (order.getPaymentMetadata() != null) {
                try {
                    var mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                    var existing = mapper.readValue(order.getPaymentMetadata(), Map.class);
                    metadata.putAll(existing);
                } catch (Exception ignored) {}
            }
            metadata.put("provider", "payos");
            metadata.put("provider_status", providerStatus);
            metadata.put("raw_get_link_response", data);

            if (nextStatus == OrderStatus.paid) {
                order.setPaidAt(order.getPaidAt() != null ? order.getPaidAt() : LocalDateTime.now());
            }
            order.setStatus(nextStatus);
            order.setPaymentMetadata(toJsonString(metadata));
            orderRepository.save(order);

            return orderRepository.findById(order.getId()).orElse(order);
        } catch (Exception e) {
            log.warn("PayOS refresh error", e);
            return order;
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Order cancel(Order order, String reason) {
        if (order.getPaymentMethod() != PaymentMethod.payos
            || order.getPaymentReference() == null || order.getPaymentReference().isBlank()) {
            return order;
        }

        try {
            var response = request("POST", "/v2/payment-requests/" + order.getPaymentReference() + "/cancel",
                Map.of("cancellationReason", reason));
            if (response.getStatusCode().isError()) {
                throw new RuntimeException("Failed to cancel payOS payment link.");
            }

            var body = response.getBody();
            var code = body != null ? body.get("code") : null;
            if (!"00".equals(code)) {
                throw new RuntimeException("Failed to cancel payOS payment link.");
            }

            var data = (Map<String, Object>) body.get("data");
            var metadata = new LinkedHashMap<String, Object>();
            if (order.getPaymentMetadata() != null) {
                try {
                    var mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                    var existing = mapper.readValue(order.getPaymentMetadata(), Map.class);
                    metadata.putAll(existing);
                } catch (Exception ignored) {}
            }
            metadata.put("provider", "payos");
            metadata.put("provider_status", "CANCELLED");
            metadata.put("raw_cancel_link_response", data != null ? data : Map.of());

            order.setPaymentMetadata(toJsonString(metadata));
            orderRepository.save(order);
        } catch (Exception e) {
            log.error("PayOS cancel error", e);
            throw new RuntimeException(e.getMessage());
        }
        return order;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Order handleWebhook(Map<String, Object> payload) {
        var signature = (String) payload.get("signature");
        var data = (Map<String, Object>) payload.get("data");

        if (signature == null || data == null) {
            throw new RuntimeException("Invalid payOS webhook payload.");
        }

        if (!PayosSignature.verify(data, signature, checksumKey)) {
            throw new RuntimeException("Invalid payOS webhook signature.");
        }

        var orderCode = data.get("orderCode");
        if (orderCode == null || !(orderCode instanceof Number)) return null;

        var orderOpt = orderRepository.findById(((Number) orderCode).longValue());
        if (orderOpt.isEmpty()) return null;

        var order = orderOpt.get();
        var providerStatus = resolveWebhookStatus(data);
        var nextStatus = mapProviderStatus(providerStatus);

        var metadata = new LinkedHashMap<String, Object>();
        if (order.getPaymentMetadata() != null) {
            try {
                var mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                var existing = mapper.readValue(order.getPaymentMetadata(), Map.class);
                metadata.putAll(existing);
            } catch (Exception ignored) {}
        }
        metadata.put("provider", "payos");
        metadata.put("provider_status", providerStatus);
        metadata.put("last_webhook", data);

        if (nextStatus == OrderStatus.paid) {
            order.setPaidAt(order.getPaidAt() != null ? order.getPaidAt() : LocalDateTime.now());
        }
        order.setStatus(nextStatus);
        var paymentLinkId = (String) data.get("paymentLinkId");
        if (paymentLinkId != null) order.setPaymentReference(paymentLinkId);
        order.setPaymentMetadata(toJsonString(metadata));
        orderRepository.save(order);

        return order;
    }

    private String toJsonString(Map<String, Object> map) {
        try {
            return new ObjectMapper().writeValueAsString(map);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize metadata to JSON", e);
            return "{}";
        }
    }

    private HttpHeaders headers() {
        var headers = new HttpHeaders();
        headers.set("x-client-id", clientId);
        headers.set("x-api-key", apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    private String getBaseUrl() {
        return baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    }

    private ResponseEntity<Map<String, Object>> request(String method, String uri, Map<String, Object> payload) {
        var url = getBaseUrl() + uri;
        if ("GET".equalsIgnoreCase(method)) {
            return restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers()), (Class<Map<String, Object>>) (Class<?>) Map.class);
        }
        return restTemplate.exchange(url, HttpMethod.valueOf(method.toUpperCase()), new HttpEntity<>(payload, headers()), (Class<Map<String, Object>>) (Class<?>) Map.class);
    }

    private OrderStatus mapProviderStatus(String providerStatus) {
        return switch (providerStatus) {
            case "PAID" -> OrderStatus.paid;
            case "CANCELLED" -> OrderStatus.cancelled;
            default -> OrderStatus.pending;
        };
    }

    private String resolveWebhookStatus(Map<String, Object> data) {
        var code = data.get("code");
        var desc = data.get("desc");
        var codeStr = code != null ? code.toString().toUpperCase() : "";
        var descStr = desc != null ? desc.toString().toUpperCase() : "";

        if ("00".equals(codeStr)) return "PAID";
        if (descStr.contains("CANCEL")) return "CANCELLED";
        return "PENDING";
    }
}
