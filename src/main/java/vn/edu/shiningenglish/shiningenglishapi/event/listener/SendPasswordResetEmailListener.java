package vn.edu.shiningenglish.shiningenglishapi.event.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import vn.edu.shiningenglish.shiningenglishapi.event.SendPasswordResetEvent;

@Component
public class SendPasswordResetEmailListener {

    private static final Logger log = LoggerFactory.getLogger(SendPasswordResetEmailListener.class);

    @Value("${app.url:http://localhost:3000}")
    private String appUrl;

    @Async
    @EventListener
    public void handlePasswordReset(SendPasswordResetEvent event) {
        log.info("SendPasswordResetEmailJob started for email={}", event.getEmail());
        var resetUrl = appUrl + "/reset-password?token=" + event.getToken() + "&email=" + event.getEmail();
        log.info("Password reset URL for {}: {}", event.getEmail(), resetUrl);
    }
}
