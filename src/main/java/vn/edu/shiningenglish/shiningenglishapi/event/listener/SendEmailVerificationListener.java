package vn.edu.shiningenglish.shiningenglishapi.event.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import vn.edu.shiningenglish.shiningenglishapi.enums.AuthenticatedBy;
import vn.edu.shiningenglish.shiningenglishapi.event.UserRegisteredEvent;
import vn.edu.shiningenglish.shiningenglishapi.service.mail.MailService;

@Component
public class SendEmailVerificationListener {

    private static final Logger log = LoggerFactory.getLogger(SendEmailVerificationListener.class);

    private final MailService mailService;

    public SendEmailVerificationListener(MailService mailService) {
        this.mailService = mailService;
    }

    @Async
    @EventListener
    public void handleUserRegistered(UserRegisteredEvent event) {
        if (event.getAuthenticatedBy() != AuthenticatedBy.local) {
            log.info("Skip email verification for third-party user: userId={}", event.getUserId());
            return;
        }
        log.info("Sending verification email to userId={}, email={}", event.getUserId(), event.getEmail());
        mailService.sendVerificationEmail(event.getEmail(), event.getName(), event.getUserId());
    }
}
