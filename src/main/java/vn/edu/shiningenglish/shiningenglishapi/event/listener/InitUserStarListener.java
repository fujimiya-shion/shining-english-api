package vn.edu.shiningenglish.shiningenglishapi.event.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import vn.edu.shiningenglish.shiningenglishapi.enums.StarTransactionType;
import vn.edu.shiningenglish.shiningenglishapi.event.UserRegisteredEvent;
import vn.edu.shiningenglish.shiningenglishapi.service.star.StarService;

@Component
public class InitUserStarListener {

    private static final Logger log = LoggerFactory.getLogger(InitUserStarListener.class);
    private final StarService starService;

    @Value("${star.registration_bonus:15}")
    private int initAmount;

    public InitUserStarListener(StarService starService) {
        this.starService = starService;
    }

    @Async
    @EventListener
    @Transactional
    public void handleUserRegistered(UserRegisteredEvent event) {
        if (initAmount <= 0) return;
        log.info("InitUserStarJob started for userId={}, amount={}", event.getUserId(), initAmount);
        try {
            starService.addStarByUserId(initAmount, event.getUserId(),
                "Bạn được tặng sao khi đăng ký tài khoản",
                StarTransactionType.registration_bonus);
            log.info("InitUserStarJob completed for userId={}", event.getUserId());
        } catch (Exception e) {
            log.error("Failed to initialize user stars for userId={}", event.getUserId(), e);
        }
    }
}
