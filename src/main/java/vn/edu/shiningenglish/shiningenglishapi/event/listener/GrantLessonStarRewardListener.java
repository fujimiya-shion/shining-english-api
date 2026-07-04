package vn.edu.shiningenglish.shiningenglishapi.event.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.shiningenglish.shiningenglishapi.enums.StarTransactionType;
import vn.edu.shiningenglish.shiningenglishapi.event.LessonCompletedEvent;
import vn.edu.shiningenglish.shiningenglishapi.model.entity.LessonStarReward;
import vn.edu.shiningenglish.shiningenglishapi.repository.lesson.LessonStarRewardRepository;
import vn.edu.shiningenglish.shiningenglishapi.service.star.StarService;

import java.time.LocalDateTime;

@Component
public class GrantLessonStarRewardListener {

    private static final Logger log = LoggerFactory.getLogger(GrantLessonStarRewardListener.class);
    private final LessonStarRewardRepository lessonStarRewardRepository;
    private final StarService starService;

    public GrantLessonStarRewardListener(LessonStarRewardRepository lessonStarRewardRepository, StarService starService) {
        this.lessonStarRewardRepository = lessonStarRewardRepository;
        this.starService = starService;
    }

    @Async
    @EventListener
    @Transactional
    public void handleLessonCompleted(LessonCompletedEvent event) {
        if (event.getStarRewardAmount() <= 0) return;

        log.info("GrantLessonStarRewardJob started: userId={}, lessonId={}, amount={}",
            event.getUserId(), event.getLessonId(), event.getStarRewardAmount());

        var existing = lessonStarRewardRepository.findByUserIdAndCourseIdAndLessonIdAndSource(
            event.getUserId(), event.getCourseId(), event.getLessonId(), "video");
        if (existing.isPresent()) {
            log.info("Star reward already granted for lessonId={}, userId={}", event.getLessonId(), event.getUserId());
            return;
        }

        var reward = new LessonStarReward();
        reward.setUserId(event.getUserId());
        reward.setCourseId(event.getCourseId());
        reward.setLessonId(event.getLessonId());
        reward.setSource("video");
        reward.setAmount(event.getStarRewardAmount());
        reward.setAwardedAt(LocalDateTime.now());
        lessonStarRewardRepository.save(reward);

        starService.addStarByUserId(event.getStarRewardAmount(), event.getUserId(),
            "Completed lesson: " + event.getLessonName(), StarTransactionType.lesson_reward_video);

        log.info("GrantLessonStarRewardJob completed for lessonId={}, userId={}", event.getLessonId(), event.getUserId());
    }
}
