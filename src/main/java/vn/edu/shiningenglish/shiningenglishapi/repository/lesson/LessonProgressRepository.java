package vn.edu.shiningenglish.shiningenglishapi.repository.lesson;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.shiningenglish.shiningenglishapi.model.entity.LessonProgress;
import java.util.List;
import java.util.Optional;

public interface LessonProgressRepository extends JpaRepository<LessonProgress, Long> {
    List<LessonProgress> findByUserIdAndCourseId(Long userId, Long courseId);
    Optional<LessonProgress> findByUserIdAndCourseIdAndLessonId(Long userId, Long courseId, Long lessonId);
}
