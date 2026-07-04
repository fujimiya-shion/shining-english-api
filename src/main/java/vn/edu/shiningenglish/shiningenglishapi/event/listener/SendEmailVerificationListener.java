package vn.edu.shiningenglish.shiningenglishapi.event.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import vn.edu.shiningenglish.shiningenglishapi.enums.AuthenticatedBy;
import vn.edu.shiningenglish.shiningenglishapi.event.UserRegisteredEvent;

@Component
public class SendEmailVerificationListener {

    private static final Logger log = LoggerFactory.getLogger(SendEmailVerificationListener.class);

    @Async
    @EventListener
    public void handleUserRegistered(UserRegisteredEvent event) {
        if (event.getAuthenticatedBy() != AuthenticatedBy.local) {
            log.info("Skip email verification for third-party user: userId={}", event.getUserId());
            return;
        }
        log.info("SendEmailVerificationJob started for userId={}, email={}", event.getUserId(), event.getEmail());
        // In production, send actual email via JavaMailSender
        log.info("Verification email would be sent to: {} (userId={})", event.getEmail(), event.getUserId());
    }
}
