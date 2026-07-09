package vn.edu.shiningenglish.shiningenglishapi.event.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import vn.edu.shiningenglish.shiningenglishapi.event.SendPasswordResetEvent;
import vn.edu.shiningenglish.shiningenglishapi.service.mail.MailService;

@Component
public class SendPasswordResetEmailListener {

    private static final Logger log = LoggerFactory.getLogger(SendPasswordResetEmailListener.class);

    private final MailService mailService;

    public SendPasswordResetEmailListener(MailService mailService) {
        this.mailService = mailService;
    }

    @Async
    @EventListener
    public void handlePasswordReset(SendPasswordResetEvent event) {
        log.info("Sending password reset email to {}", event.getEmail());
        mailService.sendPasswordResetEmail(event.getEmail(), event.getToken());
    }
}
