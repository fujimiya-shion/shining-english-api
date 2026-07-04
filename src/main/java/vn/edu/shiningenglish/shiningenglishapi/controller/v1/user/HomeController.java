package vn.edu.shiningenglish.shiningenglishapi.controller.v1.user;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import vn.edu.shiningenglish.shiningenglishapi.common.BaseController;
import vn.edu.shiningenglish.shiningenglishapi.model.entity.Course;
import vn.edu.shiningenglish.shiningenglishapi.model.entity.CourseReview;
import vn.edu.shiningenglish.shiningenglishapi.model.entity.Lesson;
import vn.edu.shiningenglish.shiningenglishapi.model.entity.PersonalAccessToken;
import vn.edu.shiningenglish.shiningenglishapi.model.entity.User;
import vn.edu.shiningenglish.shiningenglishapi.repository.CategoryRepository;
import vn.edu.shiningenglish.shiningenglishapi.repository.LevelRepository;
import vn.edu.shiningenglish.shiningenglishapi.repository.PersonalAccessTokenRepository;
import vn.edu.shiningenglish.shiningenglishapi.repository.course.CourseRepository;
import vn.edu.shiningenglish.shiningenglishapi.repository.course.CourseReviewRepository;
import vn.edu.shiningenglish.shiningenglishapi.repository.EnrollmentRepository;
import vn.edu.shiningenglish.shiningenglishapi.repository.lesson.LessonRepository;
import vn.edu.shiningenglish.shiningenglishapi.repository.user.UserRepository;
import vn.edu.shiningenglish.shiningenglishapi.util.UrlBuilder;

@RestController
@RequestMapping("/api/v1/home")
public class HomeController extends BaseController {

    private final CourseRepository courseRepository;
    private final CourseReviewRepository courseReviewRepository;
    private final LessonRepository lessonRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final CategoryRepository categoryRepository;
    private final LevelRepository levelRepository;
    private final PersonalAccessTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final UrlBuilder urlBuilder;

    public HomeController(CourseRepository courseRepository, CourseReviewRepository courseReviewRepository,
                          LessonRepository lessonRepository, EnrollmentRepository enrollmentRepository,
                          CategoryRepository categoryRepository, LevelRepository levelRepository,
                          PersonalAccessTokenRepository tokenRepository, UserRepository userRepository,
                          UrlBuilder urlBuilder) {
        this.courseRepository = courseRepository;
        this.courseReviewRepository = courseReviewRepository;
        this.lessonRepository = lessonRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.categoryRepository = categoryRepository;
        this.levelRepository = levelRepository;
        this.tokenRepository = tokenRepository;
        this.userRepository = userRepository;
        this.urlBuilder = urlBuilder;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> index(
            @RequestHeader(value = "User-Authorization", required = false) String userToken) {
        var metrics = resolveHomeMetrics();
        var currentUser = resolveCurrentUser(userToken);

        var payloads = new ArrayList<Map<String, Object>>();
        payloads.add(makeBannerPayload());
        payloads.add(makeHeroPayload(metrics));
        payloads.add(makeCourseListingPayload(currentUser));
        payloads.add(makeFeaturePayload());
        payloads.add(makeProcessPayload());
        payloads.add(makeTestimonialPayload());
        payloads.add(makeStatisticPayload(metrics));
        payloads.add(makeCtaPayload());

        return success(Map.of("payloads", payloads));
    }

    private Map<String, Object> makePayload(String type, Map<String, Object> data) {
        return Map.of("type", type, "data", data);
    }

    // ─── Banner ───────────────────────────────────────────────────────────

    private Map<String, Object> makeBannerPayload() {
        return makePayload("banner", Map.of(
            "banner_logo", "/images/app_logo.svg",
            "banner_eyebrow", "More Than English",
            "banner_title", "More Than English. Find Your Shine.",
            "banner_description", "Change the way you see English — and yourself.",
            "banner_action_buttons", List.of(
                Map.of("title", "Trải nghiệm miễn phí", "action", "/blogs", "type", "PRIMARY"),
                Map.of("title", "Khám phá khóa học", "action", "/courses", "type", "SECONDARY")
            ),
            "banner_highlights", List.of(
                mapOf("text", "Xây dựng sự tự tin từ gốc.", "icon_path", null, "icon_type", "book-open"),
                mapOf("text", "30 phút mỗi ngày.", "icon_path", null, "icon_type", "clock"),
                mapOf("text", "Để bạn dùng được tiếng Anh trong đời sống.", "icon_path", null, "icon_type", "award")
            )
        ));
    }

    // ─── Hero ──────────────────────────────────────────────────────────────

    private Map<String, Object> makeHeroPayload(Map<String, Object> metrics) {
        var learnerCount = (String) metrics.get("learner_count");
        var averageRating = (String) metrics.get("average_rating");

        var heroData = new LinkedHashMap<String, Object>();
        heroData.put("title", null);
        heroData.put("html_title", "More Than English.<br><span>Find Your Shine.</span>");
        heroData.put("description", "Thay đổi cách bạn nhìn về tiếng Anh — và về chính mình.");
        heroData.put("actions", List.of(
            Map.of("title", "Trải nghiệm miễn phí", "action", "/blogs", "type", "primary"),
            Map.of("title", "Khám phá khóa học", "action", "/courses", "type", "secondary")
        ));
        heroData.put("ctas", List.of(
            Map.of("title", learnerCount, "description", "Người Học Đã Theo"),
            Map.of("title", averageRating, "description", "Đánh Giá Thật")
        ));
        heroData.put("image", "/images/home/hero.png");
        heroData.put("image_tags", List.of(
            Map.of("text", "15 phút/bài", "hex_bg_color", "#FFFFFF", "hex_text_color", "#172B4D"),
            Map.of("text", "Bài mới hằng tuần", "hex_bg_color", "#F5A400", "hex_text_color", "#FFFFFF")
        ));
        heroData.put("image_cta", Map.of(
            "icon", "rocket",
            "title", "Học trực tuyến cùng người dạy",
            "description", "Mình trực tiếp phản hồi & cập nhật bài mới"
        ));
        return makePayload("hero", heroData);
    }

    // ─── Courses ───────────────────────────────────────────────────────────

    private Map<String, Object> makeCourseListingPayload(Optional<User> currentUser) {
        var courses = courseRepository.findAll().stream()
            .filter(c -> Boolean.TRUE.equals(c.getStatus()))
            .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
            .limit(4)
            .collect(Collectors.toList());

        var courseIds = courses.stream().map(Course::getId).collect(Collectors.toList());
        var enrolledCourseIds = currentUser.isPresent() && !courseIds.isEmpty()
            ? enrollmentRepository.findByUserId(currentUser.get().getId()).stream()
                .map(e -> e.getCourseId())
                .filter(courseIds::contains)
                .collect(Collectors.toSet())
            : java.util.Collections.<Long>emptySet();

        var courseData = courses.stream().map(c -> {
            var m = new LinkedHashMap<String, Object>();
            m.put("id", c.getId());
            m.put("name", c.getName());
            m.put("slug", c.getSlug());
            m.put("price", c.getPrice());
            m.put("thumbnail", urlBuilder.buildThumbnailUrl(c.getThumbnail()));
            m.put("rating", c.getRating());
            m.put("learned", c.getLearned());
            m.put("category_id", c.getCategoryId());
            m.put("level_id", c.getLevelId());
            m.put("description", c.getDescription());
            m.put("status", c.getStatus());
            m.put("allow_star_payment", c.getAllowStarPayment());
            m.put("star_price", c.getStarPrice());

            // category
            if (c.getCategoryId() != null) {
                categoryRepository.findById(c.getCategoryId())
                    .ifPresent(cat -> m.put("category", mapOf("id", cat.getId(), "name", cat.getName())));
            }
            // level
            if (c.getLevelId() != null) {
                levelRepository.findById(c.getLevelId())
                    .ifPresent(lvl -> m.put("level", mapOf("id", lvl.getId(), "name", lvl.getName())));
            }
            // card metrics
            var lessons = lessonRepository.findByCourseIdOrderByGroupOrderAscLessonOrderAscIdAsc(c.getId());
            var totalDuration = lessons.stream().mapToInt(l -> l.getDurationMinutes() != null ? l.getDurationMinutes() : 0).sum();
            m.put("lessons_count", lessons.size());
            m.put("comments_count", 0);
            m.put("total_duration_minutes", totalDuration);
            // enrolled flag
            m.put("enrolled", enrolledCourseIds.contains(c.getId()));

            return m;
        }).collect(Collectors.toList());

        return makePayload("courses", Map.of(
            "title", "Khóa Học Mình Tự Làm",
            "description", "Nội dung tự quay – tự dạy, tập trung vào hiệu quả thực tế",
            "courses", courseData,
            "hex_bg_colors", List.of(),
            "render_background_type", "frontend"
        ));
    }

    // ─── Features ──────────────────────────────────────────────────────────

    private Map<String, Object> makeFeaturePayload() {
        var items = List.of(
            mapOf("title", "Lộ trình cá nhân", "description", "Từng bài được sắp xếp rõ ràng để bạn học đều và chắc",
                   "icon_path", null, "icon_type", "book-open", "badge_text", "Nổi bật", "tag_text", "Dễ theo – thực tế"),
            mapOf("title", "Do một người hướng dẫn", "description", "Tôi tự quay, tự dạy và theo sát từng nội dung học",
                   "icon_path", null, "icon_type", "users", "badge_text", "Nổi bật", "tag_text", "Dễ theo – thực tế"),
            mapOf("title", "Bài tập thực chiến", "description", "Bài luyện nói – viết – phản xạ được cập nhật thường xuyên",
                   "icon_path", null, "icon_type", "check-circle", "badge_text", "Nổi bật", "tag_text", "Dễ theo – thực tế"),
            mapOf("title", "Học theo tốc độ của bạn", "description", "Xem video bất cứ lúc nào, tua lại phần khó và học chậm",
                   "icon_path", null, "icon_type", "clock", "badge_text", "Nổi bật", "tag_text", "Dễ theo – thực tế"),
            mapOf("title", "Tiến bộ đo được", "description", "Theo dõi điểm số và kỹ năng bạn cải thiện mỗi tuần",
                   "icon_path", null, "icon_type", "award", "badge_text", "Nổi bật", "tag_text", "Dễ theo – thực tế"),
            mapOf("title", "Hỗ trợ trực tiếp", "description", "Nhắn mình bất cứ lúc nào khi cần gỡ vướng bài học",
                   "icon_path", null, "icon_type", "message-circle", "badge_text", "Nổi bật", "tag_text", "Dễ theo – thực tế")
        );
        return makePayload("feature", Map.of(
            "eyebrow", "Học theo phong cách dễ hiểu",
            "title", "Vì Sao Nên Học Ở Đây?",
            "description", "Một người làm – một phong cách dạy, nhất quán và dễ theo",
            "items", items
        ));
    }

    // ─── Process ───────────────────────────────────────────────────────────

    private Map<String, Object> makeProcessPayload() {
        var steps = List.of(
            mapOf("label", "Bước 1", "title", "Chọn Khóa Học",
                   "description", "Lựa chọn khóa học phù hợp với mục tiêu và trình độ của bạn",
                   "icon_path", null, "icon_type", "book-open"),
            mapOf("label", "Bước 2", "title", "Học & Thực Hành",
                   "description", "Xem video, làm bài tập và luyện nói theo bài",
                   "icon_path", null, "icon_type", "check-circle"),
            mapOf("label", "Bước 3", "title", "Nhận Phản Hồi",
                   "description", "Gửi bài, mình xem và góp ý cách học nhanh hơn",
                   "icon_path", null, "icon_type", "message-circle"),
            mapOf("label", "Bước 4", "title", "Ghi Nhận Tiến Bộ",
                   "description", "Theo dõi kỹ năng bạn cải thiện mỗi tuần",
                   "icon_path", null, "icon_type", "award")
        );

        return makePayload("process", Map.of(
            "title", "Học Kiểu Thực Tế",
            "description", "Chọn khóa, học theo video, luyện tập và nhận phản hồi",
            "steps", steps,
            "tags", List.of("Học linh hoạt mỗi ngày", "Bài tập vui, dễ nhớ", "Theo dõi tiến bộ rõ ràng")
        ));
    }

    // ─── Testimonials ──────────────────────────────────────────────────────

    private Map<String, Object> makeTestimonialPayload() {
        var reviews = courseReviewRepository.findAll().stream()
            .filter(r -> r.getContent() != null && !r.getContent().isBlank() && r.getRating() >= 4)
            .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
            .limit(3)
            .map(r -> {
                var m = new LinkedHashMap<String, Object>();
                m.put("id", r.getId());
                m.put("course_id", r.getCourseId());
                m.put("user_id", r.getUserId());
                m.put("rating", r.getRating());
                m.put("content", r.getContent());
                // Load user
                userRepository.findById(r.getUserId()).ifPresent(u ->
                    m.put("user", mapOf("id", u.getId(), "name", u.getName(), "avatar", u.getAvatar())));
                // Load course
                courseRepository.findById(r.getCourseId()).ifPresent(c ->
                    m.put("course", mapOf("id", c.getId(), "name", c.getName())));
                return m;
            })
            .collect(Collectors.toList());

        return makePayload("testimonials", Map.of(
            "title", "Người Học Nói Gì?",
            "description", "Những phản hồi thật từ người học sau khi theo lộ trình",
            "items", reviews,
            "reviews", reviews
        ));
    }

    // ─── Statistics ────────────────────────────────────────────────────────

    private Map<String, Object> makeStatisticPayload(Map<String, Object> metrics) {
        var items = List.of(
            Map.of("value", metrics.get("learner_count"), "label", "Người Học Đang Theo"),
            Map.of("value", metrics.get("content_count"), "label", "Video & Bài Luyện"),
            Map.of("value", metrics.get("average_rating"), "label", "Điểm Đánh Giá"),
            Map.of("value", "24/7", "label", "Phản Hồi Linh Hoạt")
        );
        return makePayload("statistics", Map.of("items", items));
    }

    // ─── CTA ───────────────────────────────────────────────────────────────

    private Map<String, Object> makeCtaPayload() {
        var buttons = List.of(
            Map.of("title", "Khám Phá Khóa Học", "action", "/courses", "type", "PRIMARY"),
            Map.of("title", "Xem Câu Hỏi Thường Gặp", "action", "/faq", "type", "SECONDARY")
        );
        return makePayload("cta", Map.of(
            "title", "Sẵn Sàng Học Theo Cách Dễ Hiểu?",
            "description", "Tự học nhưng không cô đơn – mình sẽ theo sát từng bước",
            "action_buttons", buttons
        ));
    }

    // ─── Metrics ───────────────────────────────────────────────────────────

    private Map<String, Object> resolveHomeMetrics() {
        var activeCourses = courseRepository.findAll().stream()
            .filter(c -> Boolean.TRUE.equals(c.getStatus()))
            .collect(Collectors.toList());

        var learnerCount = activeCourses.stream()
            .mapToInt(c -> c.getLearned() != null ? c.getLearned() : 0)
            .sum();
        if (learnerCount <= 0) learnerCount = 10000;

        var contentCount = activeCourses.stream()
            .mapToLong(c -> lessonRepository.findByCourseIdOrderByGroupOrderAscLessonOrderAscIdAsc(c.getId()).size())
            .sum();
        if (contentCount <= 0) contentCount = 50;

        var avgRating = courseReviewRepository.findAll().stream()
            .filter(r -> r.getRating() != null)
            .mapToInt(r -> r.getRating() != null ? r.getRating() : 0)
            .average()
            .orElse(4.8);
        if (avgRating <= 0) avgRating = 4.8;

        return Map.of(
            "learner_count", formatCompactNumber(learnerCount),
            "content_count", formatCompactNumber((int) contentCount),
            "average_rating", formatRating(avgRating)
        );
    }

    // ─── User from token ──────────────────────────────────────────────────

    private Optional<User> resolveCurrentUser(String token) {
        if (token == null || token.isBlank()) return Optional.empty();

        try {
            var hashedToken = sha256(token.trim());
            var optPat = tokenRepository.findByToken(hashedToken);
            if (optPat.isPresent()) {
                var pat = optPat.get();
                if ("user_auth_token".equals(pat.getName())) {
                    return userRepository.findById(pat.getTokenableId());
                }
            }
        } catch (Exception ignored) {}

        return Optional.empty();
    }

    private String sha256(String value) {
        try {
            var digest = MessageDigest.getInstance("SHA-256");
            var hash = digest.digest((value + (char) 0).getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    // ─── Map helper (supports null values, unlike Map.of) ────────────────

    @SuppressWarnings("unchecked")
    private static Map<String, Object> mapOf(Object... entries) {
        var map = new LinkedHashMap<String, Object>();
        for (int i = 0; i < entries.length; i += 2) {
            map.put((String) entries[i], entries[i + 1]);
        }
        return map;
    }

    // ─── Formatters ───────────────────────────────────────────────────────

    private String formatCompactNumber(int number) {
        if (number >= 1000) {
            return BigDecimal.valueOf(number / 1000.0)
                .setScale(0, RoundingMode.FLOOR)
                .toBigInteger() + "K+";
        }
        return number + "+";
    }

    private String formatRating(double rating) {
        var rounded = BigDecimal.valueOf(rating)
            .setScale(1, RoundingMode.HALF_UP)
            .doubleValue();
        return rounded + "★";
    }
}
