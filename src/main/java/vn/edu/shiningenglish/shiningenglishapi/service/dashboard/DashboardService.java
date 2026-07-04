package vn.edu.shiningenglish.shiningenglishapi.service.dashboard;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import vn.edu.shiningenglish.shiningenglishapi.model.entity.Enrollment;
import vn.edu.shiningenglish.shiningenglishapi.model.entity.LessonProgress;
import vn.edu.shiningenglish.shiningenglishapi.repository.EnrollmentRepository;
import vn.edu.shiningenglish.shiningenglishapi.repository.lesson.LessonProgressRepository;
import vn.edu.shiningenglish.shiningenglishapi.repository.lesson.LessonRepository;
import vn.edu.shiningenglish.shiningenglishapi.repository.quiz.UserQuizAttemptRepository;

@Service
public class DashboardService {

    private final EnrollmentRepository enrollmentRepository;
    private final LessonProgressRepository lessonProgressRepository;
    private final LessonRepository lessonRepository;
    private final UserQuizAttemptRepository userQuizAttemptRepository;

    public DashboardService(EnrollmentRepository enrollmentRepository, LessonProgressRepository lessonProgressRepository,
                            LessonRepository lessonRepository, UserQuizAttemptRepository userQuizAttemptRepository) {
        this.enrollmentRepository = enrollmentRepository;
        this.lessonProgressRepository = lessonProgressRepository;
        this.lessonRepository = lessonRepository;
        this.userQuizAttemptRepository = userQuizAttemptRepository;
    }

    public Map<String, Object> overview(Long userId) {
        var enrollments = enrollmentRepository.findByUserId(userId);
        var courseIds = enrollments.stream().map(Enrollment::getCourseId).collect(Collectors.toList());

        var allProgress = lessonProgressRepository.findAll().stream()
            .filter(p -> p.getUserId().equals(userId) && courseIds.contains(p.getCourseId()))
            .collect(Collectors.toList());

        var completedRows = allProgress.stream().filter(p -> p.getCompletedAt() != null).collect(Collectors.toList());
        var currentRows = allProgress.stream().filter(LessonProgress::getIsCurrent).collect(Collectors.toList());

        // Calculate hours this week
        var startOfWeek = LocalDate.now().with(java.time.DayOfWeek.MONDAY).atStartOfDay();
        var hoursThisWeek = completedRows.stream()
            .filter(p -> p.getCompletedAt() != null && p.getCompletedAt().isAfter(startOfWeek))
            .mapToDouble(p -> {
                var lesson = lessonRepository.findById(p.getLessonId());
                return lesson.map(l -> l.getDurationMinutes() != null ? l.getDurationMinutes() / 60.0 : 0.0).orElse(0.0);
            })
            .sum();

        // Streak days
        var completedDates = completedRows.stream()
            .filter(p -> p.getCompletedAt() != null)
            .map(p -> p.getCompletedAt().toLocalDate())
            .collect(Collectors.toSet());

        var streakDays = 0;
        var cursor = LocalDate.now();
        while (completedDates.contains(cursor) || (streakDays == 0 && completedDates.contains(cursor.minusDays(1)))) {
            if (!completedDates.contains(cursor)) cursor = cursor.minusDays(1);
            streakDays++;
            cursor = cursor.minusDays(1);
        }

        // Today stats
        var todayStart = LocalDate.now().atStartOfDay();
        var todayCompleted = completedRows.stream()
            .filter(p -> p.getCompletedAt() != null && p.getCompletedAt().isAfter(todayStart))
            .collect(Collectors.toList());
        var completedLessonsToday = todayCompleted.size();
        var minutesToday = todayCompleted.stream()
            .mapToInt(p -> lessonRepository.findById(p.getLessonId())
                .map(l -> l.getDurationMinutes() != null ? l.getDurationMinutes() : 0).orElse(0))
            .sum();

        var stats = new LinkedHashMap<String, Object>();
        stats.put("enrolled_courses", enrollments.size());
        stats.put("hours_this_week", Math.round(hoursThisWeek * 100.0) / 100.0);
        stats.put("certificates", 0);
        stats.put("streak_days", streakDays);

        // Enrolled courses
        var enrolledCourses = enrollments.stream().map(e -> {
            var map = new LinkedHashMap<String, Object>();
            map.put("course_id", e.getCourseId());
            map.put("enrolled_at", e.getEnrolledAt());
            map.put("progress", allProgress.stream()
                .filter(p -> p.getCourseId().equals(e.getCourseId()) && p.getCompletedAt() != null).count());
            return map;
        }).collect(Collectors.toList());

        // Weekly plan
        var dailyGoal = 2;
        var dailyMinutesGoal = 5;
        var lessonTone = completedLessonsToday >= dailyGoal ? "done" : (completedLessonsToday > 0 ? "doing" : "todo");
        var hoursTone = minutesToday >= dailyMinutesGoal ? "done" : (minutesToday > 0 ? "doing" : "todo");
        var streakTone = streakDays >= 7 ? "done" : (streakDays > 0 ? "doing" : "todo");

        var weeklyPlan = List.of(
            Map.of("label", "Hoàn thành bài học hôm nay", "status", completedLessonsToday + "/" + dailyGoal + " bài", "tone", lessonTone),
            Map.of("label", "Giờ học hôm nay", "status", minutesToday + "m/" + dailyMinutesGoal + "m", "tone", hoursTone),
            Map.of("label", "Duy trì chuỗi học", "status", streakDays + "/7 ngày", "tone", streakTone)
        );

        var result = new LinkedHashMap<String, Object>();
        result.put("stats", stats);
        result.put("enrolled_courses", enrolledCourses);
        result.put("recent_activity", List.of());
        result.put("certificates", List.of());
        result.put("weekly_plan", weeklyPlan);
        return result;
    }
}
