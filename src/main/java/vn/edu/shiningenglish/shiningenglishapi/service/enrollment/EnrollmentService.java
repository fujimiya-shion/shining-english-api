package vn.edu.shiningenglish.shiningenglishapi.service.enrollment;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.edu.shiningenglish.shiningenglishapi.enums.OrderStatus;
import vn.edu.shiningenglish.shiningenglishapi.event.CourseCompletedEvent;
import vn.edu.shiningenglish.shiningenglishapi.event.LessonCompletedEvent;
import vn.edu.shiningenglish.shiningenglishapi.model.entity.Enrollment;
import vn.edu.shiningenglish.shiningenglishapi.model.entity.Lesson;
import vn.edu.shiningenglish.shiningenglishapi.model.entity.LessonProgress;
import vn.edu.shiningenglish.shiningenglishapi.model.entity.Order;
import vn.edu.shiningenglish.shiningenglishapi.repository.course.CourseReviewRepository;
import vn.edu.shiningenglish.shiningenglishapi.repository.EnrollmentRepository;
import vn.edu.shiningenglish.shiningenglishapi.repository.lesson.LessonProgressRepository;
import vn.edu.shiningenglish.shiningenglishapi.repository.lesson.LessonRepository;
import vn.edu.shiningenglish.shiningenglishapi.repository.order.OrderRepository;

@Service
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final LessonRepository lessonRepository;
    private final LessonProgressRepository lessonProgressRepository;
    private final CourseReviewRepository courseReviewRepository;
    private final OrderRepository orderRepository;
    private final ApplicationEventPublisher eventPublisher;

    public EnrollmentService(EnrollmentRepository enrollmentRepository, LessonRepository lessonRepository,
                             LessonProgressRepository lessonProgressRepository,
                             CourseReviewRepository courseReviewRepository,
                             OrderRepository orderRepository,
                             ApplicationEventPublisher eventPublisher) {
        this.enrollmentRepository = enrollmentRepository;
        this.lessonRepository = lessonRepository;
        this.lessonProgressRepository = lessonProgressRepository;
        this.courseReviewRepository = courseReviewRepository;
        this.orderRepository = orderRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public Enrollment enroll(Long userId, Long courseId, Long orderId) {
        var existing = enrollmentRepository.findByUserIdAndCourseId(userId, courseId);
        if (existing.isPresent()) {
            return existing.get();
        }
        var enrollment = new Enrollment();
        enrollment.setUserId(userId);
        enrollment.setCourseId(courseId);
        enrollment.setOrderId(orderId);
        enrollment.setEnrolledAt(LocalDateTime.now());
        return enrollmentRepository.save(enrollment);
    }

    public boolean isEnrolled(Long userId, Long courseId) {
        var enrollment = enrollmentRepository.findByUserIdAndCourseId(userId, courseId);
        if (enrollment.isEmpty()) return false;

        var e = enrollment.get();
        // No order_id = free course = always enrolled
        if (e.getOrderId() == null) return true;

        // Has order_id → check if order is paid
        return orderRepository.findById(e.getOrderId())
            .map(o -> o.getStatus() == OrderStatus.paid)
            .orElse(false);
    }

    public boolean hasPendingEnrollment(Long userId, Long courseId) {
        var enrollment = enrollmentRepository.findByUserIdAndCourseId(userId, courseId);
        if (enrollment.isEmpty() || enrollment.get().getOrderId() == null) return false;

        return orderRepository.findById(enrollment.get().getOrderId())
            .map(o -> o.getStatus() == OrderStatus.pending)
            .orElse(false);
    }

    public Map<String, Object> getLearningProgress(Long userId, Long courseId) {
        var orderedLessons = lessonRepository.findByCourseIdOrderByGroupOrderAscLessonOrderAscIdAsc(courseId);
        var orderedIds = orderedLessons.stream().map(Lesson::getId).collect(Collectors.toList());
        var progressRows = lessonProgressRepository.findByUserIdAndCourseId(userId, courseId);
        var completedSet = progressRows.stream()
            .filter(p -> p.getCompletedAt() != null)
            .map(LessonProgress::getLessonId)
            .collect(Collectors.toSet());
        var completedIds = orderedIds.stream().filter(completedSet::contains).collect(Collectors.toList());
        var currentId = progressRows.stream()
            .filter(LessonProgress::getIsCurrent)
            .findFirst()
            .map(LessonProgress::getLessonId)
            .orElse(null);

        if (currentId == null) {
            currentId = orderedIds.stream()
                .filter(id -> !completedSet.contains(id))
                .findFirst()
                .orElse(null);
        }

        var total = orderedIds.size();
        var progressPct = total > 0 ? Math.round((double) completedIds.size() / total * 10000.0) / 100.0 : 0.0;
        var hasReviewed = courseReviewRepository.existsByCourseIdAndUserId(courseId, userId);

        var result = new LinkedHashMap<String, Object>();
        result.put("course_id", courseId);
        result.put("current_lesson_id", currentId);
        result.put("completed_lesson_ids", completedIds);
        result.put("total_lessons", total);
        result.put("progress_percentage", progressPct);
        result.put("has_reviewed", hasReviewed);
        return result;
    }

    @Transactional
    public Map<String, Object> completeLesson(Long userId, Long courseId, Long lessonId) {
        var orderedLessons = lessonRepository.findByCourseIdOrderByGroupOrderAscLessonOrderAscIdAsc(courseId);
        var orderedIds = orderedLessons.stream().map(Lesson::getId).collect(Collectors.toList());

        if (!orderedIds.contains(lessonId)) return null;

        lessonProgressRepository.findByUserIdAndCourseId(userId, courseId)
            .forEach(p -> {
                p.setIsCurrent(false);
                lessonProgressRepository.save(p);
            });

        var progress = lessonProgressRepository.findByUserIdAndCourseIdAndLessonId(userId, courseId, lessonId)
            .orElseGet(() -> {
                var lp = new LessonProgress();
                lp.setUserId(userId);
                lp.setCourseId(courseId);
                lp.setLessonId(lessonId);
                return lp;
            });
        progress.setCompletedAt(LocalDateTime.now());
        progress.setIsCurrent(false);
        lessonProgressRepository.save(progress);

        var completedSet = lessonProgressRepository.findByUserIdAndCourseId(userId, courseId).stream()
            .filter(p -> p.getCompletedAt() != null)
            .map(LessonProgress::getLessonId)
            .collect(Collectors.toSet());
        var completedIds = orderedIds.stream().filter(completedSet::contains).collect(Collectors.toList());

        var currentCandidate = orderedIds.stream()
            .filter(id -> !completedSet.contains(id))
            .findFirst()
            .orElse(null);

        if (currentCandidate != null) {
            var currentLp = lessonProgressRepository.findByUserIdAndCourseIdAndLessonId(userId, courseId, currentCandidate)
                .orElseGet(() -> {
                    var lp = new LessonProgress();
                    lp.setUserId(userId);
                    lp.setCourseId(courseId);
                    lp.setLessonId(currentCandidate);
                    return lp;
                });
            currentLp.setIsCurrent(true);
            lessonProgressRepository.save(currentLp);
        }

        // Dispatch lesson completion event (async star reward)
        var completedLesson = orderedLessons.stream()
            .filter(l -> l.getId().equals(lessonId))
            .findFirst().orElse(null);
        if (completedLesson != null) {
            var rewardAmount = completedLesson.getStarRewardVideo() != null ? completedLesson.getStarRewardVideo() : 0;
            eventPublisher.publishEvent(new LessonCompletedEvent(userId, courseId, lessonId, rewardAmount, completedLesson.getName()));
        }

        // Dispatch course completion event (async)
        var isLastLesson = orderedIds.size() > 0 && completedIds.size() >= orderedIds.size() && completedIds.contains(lessonId);
        if (isLastLesson) {
            eventPublisher.publishEvent(new CourseCompletedEvent(userId, courseId, 10));
        }

        var total = orderedIds.size();
        var progressPct = total > 0 ? Math.round((double) completedIds.size() / total * 10000.0) / 100.0 : 0.0;
        var hasReviewed = courseReviewRepository.existsByCourseIdAndUserId(courseId, userId);

        var result = new LinkedHashMap<String, Object>();
        result.put("course_id", courseId);
        result.put("current_lesson_id", currentCandidate);
        result.put("completed_lesson_ids", completedIds);
        result.put("total_lessons", total);
        result.put("progress_percentage", progressPct);
        result.put("has_reviewed", hasReviewed);

        var nextLesson = currentCandidate != null ? orderedLessons.stream()
            .filter(l -> l.getId().equals(currentCandidate)).findFirst().orElse(null) : null;
        if (nextLesson != null) {
            result.put("next_lesson", Map.of("id", nextLesson.getId(), "has_quiz", 
                nextLesson.getHasQuiz() != null && nextLesson.getHasQuiz()));
        }

        return result;
    }

    @Transactional
    public Map<String, Object> setCurrentLesson(Long userId, Long courseId, Long lessonId) {
        var orderedLessons = lessonRepository.findByCourseIdOrderByGroupOrderAscLessonOrderAscIdAsc(courseId);
        var orderedIds = orderedLessons.stream().map(Lesson::getId).collect(Collectors.toList());
        if (!orderedIds.contains(lessonId)) return null;

        lessonProgressRepository.findByUserIdAndCourseId(userId, courseId)
            .forEach(p -> {
                p.setIsCurrent(false);
                lessonProgressRepository.save(p);
            });

        var lp = lessonProgressRepository.findByUserIdAndCourseIdAndLessonId(userId, courseId, lessonId)
            .orElseGet(() -> {
                var p = new LessonProgress();
                p.setUserId(userId);
                p.setCourseId(courseId);
                p.setLessonId(lessonId);
                return p;
            });
        lp.setIsCurrent(true);
        lessonProgressRepository.save(lp);

        var completedIds = lessonProgressRepository.findByUserIdAndCourseId(userId, courseId).stream()
            .filter(p -> p.getCompletedAt() != null)
            .map(LessonProgress::getLessonId)
            .collect(Collectors.toList());
        var total = orderedIds.size();
        var progressPct = total > 0 ? Math.round((double) completedIds.size() / total * 10000.0) / 100.0 : 0.0;
        var hasReviewed = courseReviewRepository.existsByCourseIdAndUserId(courseId, userId);

        var result = new LinkedHashMap<String, Object>();
        result.put("course_id", courseId);
        result.put("current_lesson_id", lessonId);
        result.put("completed_lesson_ids", completedIds);
        result.put("total_lessons", total);
        result.put("progress_percentage", progressPct);
        result.put("has_reviewed", hasReviewed);
        return result;
    }

}
