package vn.edu.shiningenglish.shiningenglishapi.service.mail;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
public class MailService {

    private static final Logger log = LoggerFactory.getLogger(MailService.class);

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${app.mail.from_address}")
    private String fromAddress;

    @Value("${app.mail.from_name:Shining English}")
    private String fromName;

    @Value("${app.frontend_url:http://localhost:3000}")
    private String frontendUrl;

    public MailService(JavaMailSender mailSender, TemplateEngine templateEngine) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
    }

    @Async
    public void sendHtml(String to, String subject, String templateName, Context context) {
        try {
            var html = templateEngine.process(templateName, context);
            var message = mailSender.createMimeMessage();
            var helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromAddress, fromName);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true);
            mailSender.send(message);
            log.info("Email sent to {}: subject={}", to, subject);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage(), e);
        }
    }

    @Async
    public void sendVerificationEmail(String to, String userName, Long userId) {
        var ctx = new Context();
        ctx.setVariable("userName", userName);
        ctx.setVariable("userId", userId);
        ctx.setVariable("frontendUrl", frontendUrl);
        sendHtml(to, "Xác nhận email - Shining English", "mail/verify-email", ctx);
    }

    @Async
    public void sendPasswordResetEmail(String to, String token) {
        var resetUrl = frontendUrl + "/reset-password?token=" + token + "&email=" + to;
        var ctx = new Context();
        ctx.setVariable("resetUrl", resetUrl);
        ctx.setVariable("email", to);
        sendHtml(to, "Đặt lại mật khẩu - Shining English", "mail/password-reset", ctx);
    }
}
