package vn.edu.shiningenglish.shiningenglishapi.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
public class RecaptchaVerifier {

    private static final Logger log = LoggerFactory.getLogger(RecaptchaVerifier.class);

    @Value("${recaptcha.secret:}")
    private String secretKey;

    private final RestTemplate restTemplate = new RestTemplate();

    public void verifyOrFail(String token, String expectedAction, String ipAddress) {
        if (secretKey.isBlank()) {
            log.warn("reCAPTCHA secret key not configured");
            return;
        }
        try {
            var url = "https://www.google.com/recaptcha/api/siteverify?secret=%s&response=%s".formatted(secretKey, token);
            if (ipAddress != null) {
                url += "&remoteip=" + ipAddress;
            }
            var response = restTemplate.postForObject(url, null, Map.class);
            if (response == null || !Boolean.TRUE.equals(response.get("success"))) {
                throw new IllegalArgumentException("reCAPTCHA verification failed");
            }
        } catch (Exception e) {
            log.error("reCAPTCHA error: {}", e.getMessage());
            throw new IllegalArgumentException("reCAPTCHA verification failed");
        }
    }
}
