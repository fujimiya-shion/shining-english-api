package vn.edu.shiningenglish.shiningenglishapi.repository.course;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.shiningenglish.shiningenglishapi.model.entity.CourseReview;
import java.util.Optional;

public interface CourseReviewRepository extends JpaRepository<CourseReview, Long> {
    Optional<CourseReview> findByCourseIdAndUserId(Long courseId, Long userId);
    boolean existsByCourseIdAndUserId(Long courseId, Long userId);
}
