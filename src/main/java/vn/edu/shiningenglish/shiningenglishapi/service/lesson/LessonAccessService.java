package vn.edu.shiningenglish.shiningenglishapi.service.lesson;

import org.springframework.stereotype.Service;
import vn.edu.shiningenglish.shiningenglishapi.model.entity.Lesson;
import vn.edu.shiningenglish.shiningenglishapi.service.enrollment.EnrollmentService;

@Service
public class LessonAccessService {

    private final EnrollmentService enrollmentService;

    public LessonAccessService(EnrollmentService enrollmentService) {
        this.enrollmentService = enrollmentService;
    }

    public boolean canWatchLessonVideo(Long userId, Lesson lesson) {
        if (lesson.getIsPreviewFree() != null && lesson.getIsPreviewFree()) {
            return true;
        }
        return canAccessLessonProtectedContent(userId, lesson);
    }

    public boolean canAccessLessonProtectedContent(Long userId, Lesson lesson) {
        if (userId == null) return false;
        var courseId = lesson.getCourseId();
        if (courseId == null || courseId <= 0) return false;
        return enrollmentService.isEnrolled(userId, courseId);
    }
}
