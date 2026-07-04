package vn.edu.shiningenglish.shiningenglishapi.repository.lesson;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.edu.shiningenglish.shiningenglishapi.model.entity.LessonProgress;
import java.util.List;
import java.util.Optional;

public interface LessonProgressRepository extends JpaRepository<LessonProgress, Long> {
    List<LessonProgress> findByUserIdAndCourseId(Long userId, Long courseId);
    Optional<LessonProgress> findByUserIdAndCourseIdAndLessonId(Long userId, Long courseId, Long lessonId);

    @Modifying
    @Query("UPDATE LessonProgress lp SET lp.isCurrent = false WHERE lp.userId = :userId AND lp.courseId = :courseId AND lp.isCurrent = true")
    void clearCurrentByUserIdAndCourseId(@Param("userId") Long userId, @Param("courseId") Long courseId);
}
