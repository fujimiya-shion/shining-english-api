package vn.edu.shiningenglish.shiningenglishapi.service.course;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.shiningenglish.shiningenglishapi.model.entity.CourseReview;
import vn.edu.shiningenglish.shiningenglishapi.repository.course.CourseReviewRepository;

@Service
public class CourseReviewService {

    private final CourseReviewRepository courseReviewRepository;

    public CourseReviewService(CourseReviewRepository courseReviewRepository) {
        this.courseReviewRepository = courseReviewRepository;
    }

    @Transactional
    public CourseReview upsertByUser(Long courseId, Long userId, int rating, String content) {
        var existing = courseReviewRepository.findByCourseIdAndUserId(courseId, userId);
        if (existing.isPresent()) {
            var review = existing.get();
            review.setRating(rating);
            review.setContent(content);
            return courseReviewRepository.save(review);
        }
        var review = new CourseReview();
        review.setCourseId(courseId);
        review.setUserId(userId);
        review.setRating(rating);
        review.setContent(content);
        return courseReviewRepository.save(review);
    }
}
