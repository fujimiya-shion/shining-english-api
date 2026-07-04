package vn.edu.shiningenglish.shiningenglishapi.controller.v1.course;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import vn.edu.shiningenglish.shiningenglishapi.common.BaseController;
import vn.edu.shiningenglish.shiningenglishapi.model.entity.User;
import vn.edu.shiningenglish.shiningenglishapi.service.enrollment.EnrollmentService;
import vn.edu.shiningenglish.shiningenglishapi.service.cart.CartService;
import vn.edu.shiningenglish.shiningenglishapi.service.course.CourseReviewService;
import vn.edu.shiningenglish.shiningenglishapi.service.course.CourseService;
import vn.edu.shiningenglish.shiningenglishapi.service.star.StarService;
import vn.edu.shiningenglish.shiningenglishapi.valueobject.CourseFilter;
import vn.edu.shiningenglish.shiningenglishapi.valueobject.MetaPagination;
import vn.edu.shiningenglish.shiningenglishapi.valueobject.QueryOption;

@RestController
@RequestMapping("/api/v1/courses")
public class CourseController extends BaseController {

    private final CourseService courseService;
    private final CartService cartService;
    private final EnrollmentService enrollmentService;
    private final CourseReviewService courseReviewService;
    private final StarService starService;

    public CourseController(CourseService courseService, CartService cartService,
                            EnrollmentService enrollmentService, CourseReviewService courseReviewService,
                            StarService starService) {
        this.courseService = courseService;
        this.cartService = cartService;
        this.enrollmentService = enrollmentService;
        this.courseReviewService = courseReviewService;
        this.starService = starService;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> index(@RequestParam Map<String, String> params) {
        var options = QueryOption.fromArray(params, true);
        var page = courseService.getAll(options);
        var meta = MetaPagination.fromPage(page);
        return success("OK", page.getContent(), 200, meta.toArray());
    }

    @RequestMapping(path = "/filter", method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseEntity<Map<String, Object>> filter(@RequestParam Map<String, String> params) {
        var filters = CourseFilter.fromArray(params);
        var page = courseService.filter(filters);
        var meta = MetaPagination.fromPage(page);
        return success("OK", page.getContent(), 200, meta.toArray());
    }

    @GetMapping("/filter-props")
    public ResponseEntity<Map<String, Object>> getFilterProps() {
        return success(courseService.getFilterProps());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> show(@PathVariable Long id) {
        var course = courseService.getById(id);
        if (course.isEmpty()) return notfound();
        return success(course.get());
    }

    @GetMapping("/slug/{slug}")
    public ResponseEntity<Map<String, Object>> showBySlug(@PathVariable String slug) {
        var course = courseService.getBySlug(slug);
        if (course.isEmpty()) return notfound();
        return success(course.get());
    }

    @GetMapping("/{id}/access")
    public ResponseEntity<Map<String, Object>> access(Authentication auth, @PathVariable Long id) {
        if (auth == null || !(auth.getPrincipal() instanceof User user)) {
            return unauthorized("Unauthenticated");
        }
        var course = courseService.getById(id);
        if (course.isEmpty()) return notfound();

        var isEnrolled = enrollmentService.isEnrolled(user.getId(), id);
        var isFree = course.get().getPrice() == null || course.get().getPrice() == 0;
        var starBalance = starService.getBalance(user.getId());

        var data = new LinkedHashMap<String, Object>();
        data.put("course_id", id);
        data.put("enrolled", isEnrolled);
        data.put("pending_access", enrollmentService.hasPendingEnrollment(user.getId(), id));
        data.put("in_cart", cartService.hasCourse(user.getId(), id));
        data.put("is_free_course", isFree);
        data.put("can_enroll_free", isFree && !isEnrolled);
        data.put("allow_star_payment", course.get().getAllowStarPayment() != null && course.get().getAllowStarPayment());
        data.put("star_price", course.get().getStarPrice() != null ? course.get().getStarPrice() : 0);
        data.put("star_balance", starBalance);
        return success(data);
    }

    @GetMapping("/{id}/learning-progress")
    public ResponseEntity<Map<String, Object>> learningProgress(Authentication auth, @PathVariable Long id) {
        var user = (User) auth.getPrincipal();
        if (!enrollmentService.isEnrolled(user.getId(), id)) {
            return unauthorized("Course access denied");
        }
        var progress = enrollmentService.getLearningProgress(user.getId(), id);
        if (progress == null) return notfound();
        return success(progress);
    }

    @PostMapping("/{id}/lessons/{lessonId}/complete")
    public ResponseEntity<Map<String, Object>> completeLesson(Authentication auth, @PathVariable Long id, @PathVariable Long lessonId) {
        var user = (User) auth.getPrincipal();
        if (!enrollmentService.isEnrolled(user.getId(), id)) {
            return unauthorized("Course access denied");
        }
        var progress = enrollmentService.completeLesson(user.getId(), id, lessonId);
        if (progress == null) return notfound();
        return success(progress);
    }

    @PostMapping("/{id}/current-lesson")
    public ResponseEntity<Map<String, Object>> setCurrentLesson(Authentication auth, @PathVariable Long id, @RequestBody Map<String, Object> body) {
        var user = (User) auth.getPrincipal();
        var lessonId = ((Number) body.get("lesson_id")).longValue();
        if (!enrollmentService.isEnrolled(user.getId(), id)) {
            return unauthorized("Course access denied");
        }
        var progress = enrollmentService.setCurrentLesson(user.getId(), id, lessonId);
        if (progress == null) return notfound();
        return success(progress);
    }

    @PostMapping("/{id}/reviews")
    public ResponseEntity<Map<String, Object>> storeReview(Authentication auth, @PathVariable Long id, @RequestBody Map<String, Object> body) {
        var user = (User) auth.getPrincipal();
        if (!enrollmentService.isEnrolled(user.getId(), id)) {
            return unauthorized("Course access denied");
        }
        var review = courseReviewService.upsertByUser(id, user.getId(), (int) body.get("rating"), (String) body.get("content"));
        return created(review, "Review submitted");
    }
}
