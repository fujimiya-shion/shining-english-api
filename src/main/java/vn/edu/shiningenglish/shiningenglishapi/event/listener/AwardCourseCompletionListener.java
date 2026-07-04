package vn.edu.shiningenglish.shiningenglishapi.event.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.shiningenglish.shiningenglishapi.enums.StarTransactionType;
import vn.edu.shiningenglish.shiningenglishapi.event.CourseCompletedEvent;
import vn.edu.shiningenglish.shiningenglishapi.model.entity.LessonStarReward;
import vn.edu.shiningenglish.shiningenglishapi.repository.lesson.LessonStarRewardRepository;
import vn.edu.shiningenglish.shiningenglishapi.service.star.StarService;

import java.time.LocalDateTime;

@Component
public class AwardCourseCompletionListener {

    private static final Logger log = LoggerFactory.getLogger(AwardCourseCompletionListener.class);
    private final LessonStarRewardRepository lessonStarRewardRepository;
    private final StarService starService;

    @Value("${star.course_complete:10}")
    private int amount;

    public AwardCourseCompletionListener(LessonStarRewardRepository lessonStarRewardRepository, StarService starService) {
        this.lessonStarRewardRepository = lessonStarRewardRepository;
        this.starService = starService;
    }

    @Async
    @EventListener
    @Transactional
    public void handleCourseCompleted(CourseCompletedEvent event) {
        var rewardAmount = event.getCompletionRewardAmount() > 0 ? event.getCompletionRewardAmount() : amount;
        if (rewardAmount <= 0) return;

        log.info("AwardCourseCompletionJob started: userId={}, courseId={}", event.getUserId(), event.getCourseId());

        var existing = lessonStarRewardRepository.findByUserIdAndCourseIdAndLessonIdAndSource(
            event.getUserId(), event.getCourseId(), 0L, "course_complete");
        if (existing.isPresent()) {
            log.info("Course completion reward already granted for userId={}, courseId={}",
                event.getUserId(), event.getCourseId());
            return;
        }

        var reward = new LessonStarReward();
        reward.setUserId(event.getUserId());
        reward.setCourseId(event.getCourseId());
        reward.setLessonId(0L);
        reward.setSource("course_complete");
        reward.setAmount(rewardAmount);
        reward.setAwardedAt(LocalDateTime.now());
        lessonStarRewardRepository.save(reward);

        starService.addStarByUserId(rewardAmount, event.getUserId(),
            "Course completed", StarTransactionType.course_complete);

        log.info("AwardCourseCompletionJob completed for userId={}, courseId={}",
            event.getUserId(), event.getCourseId());
    }
}
