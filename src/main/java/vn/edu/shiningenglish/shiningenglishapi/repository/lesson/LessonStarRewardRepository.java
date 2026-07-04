package vn.edu.shiningenglish.shiningenglishapi.repository.lesson;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.shiningenglish.shiningenglishapi.model.entity.LessonStarReward;
import java.util.Optional;

public interface LessonStarRewardRepository extends JpaRepository<LessonStarReward, Long> {
    Optional<LessonStarReward> findByUserIdAndCourseIdAndLessonIdAndSource(Long userId, Long courseId, Long lessonId, String source);
}
