package vn.edu.shiningenglish.shiningenglishapi.controller.v1.star;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import vn.edu.shiningenglish.shiningenglishapi.common.BaseController;
import vn.edu.shiningenglish.shiningenglishapi.enums.StarTransactionType;
import vn.edu.shiningenglish.shiningenglishapi.model.entity.DailyCheckIn;
import vn.edu.shiningenglish.shiningenglishapi.model.entity.User;
import vn.edu.shiningenglish.shiningenglishapi.repository.DailyCheckInRepository;
import vn.edu.shiningenglish.shiningenglishapi.service.course.CourseService;
import vn.edu.shiningenglish.shiningenglishapi.service.enrollment.EnrollmentService;
import vn.edu.shiningenglish.shiningenglishapi.service.order.OrderService;
import vn.edu.shiningenglish.shiningenglishapi.service.star.StarService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/stars")
public class StarController extends BaseController {

    private final StarService starService;
    private final OrderService orderService;
    private final EnrollmentService enrollmentService;
    private final CourseService courseService;
    private final DailyCheckInRepository dailyCheckInRepository;

    public StarController(StarService starService, OrderService orderService,
                          EnrollmentService enrollmentService, CourseService courseService,
                          DailyCheckInRepository dailyCheckInRepository) {
        this.starService = starService;
        this.orderService = orderService;
        this.enrollmentService = enrollmentService;
        this.courseService = courseService;
        this.dailyCheckInRepository = dailyCheckInRepository;
    }

    @GetMapping("/balance")
    public ResponseEntity<Map<String, Object>> balance(Authentication auth) {
        var user = (User) auth.getPrincipal();
        return success(Map.of("balance", starService.getBalance(user.getId())));
    }

    @Transactional
    @PostMapping("/check-in")
    public ResponseEntity<Map<String, Object>> checkIn(Authentication auth) {
        var user = (User) auth.getPrincipal();
        var todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        var todayEnd = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);

        var alreadyCheckedIn = dailyCheckInRepository.existsByUserIdAndCheckedInAtBetween(
            user.getId(), todayStart, todayEnd);
        if (alreadyCheckedIn) {
            return error("Bạn đã check-in hôm nay rồi.", 422);
        }

        var rewardAmount = 1;
        var checkIn = new DailyCheckIn();
        checkIn.setUserId(user.getId());
        checkIn.setCheckedInAt(LocalDateTime.now());
        checkIn.setRewardAmount(rewardAmount);
        dailyCheckInRepository.save(checkIn);

        starService.addStarByUserId(rewardAmount, user.getId(), "Check-in hàng ngày", StarTransactionType.daily_checkin);

        return success("Check-in thành công!", Map.of("reward", rewardAmount, "balance", starService.getBalance(user.getId())));
    }

    @Transactional
    @PostMapping("/courses/{courseId}/pay")
    public ResponseEntity<Map<String, Object>> payForCourse(Authentication auth, @PathVariable Long courseId) {
        var user = (User) auth.getPrincipal();
        var course = courseService.getById(courseId);
        if (course.isEmpty()) return notfound();

        if (course.get().getAllowStarPayment() == null || !course.get().getAllowStarPayment()
            || course.get().getStarPrice() == null || course.get().getStarPrice() <= 0) {
            return error("Khóa học này không hỗ trợ thanh toán bằng sao.", 422);
        }

        if (enrollmentService.isEnrolled(user.getId(), courseId)) {
            return success("Bạn đã ghi danh khóa học này rồi.", Map.of("enrolled", true));
        }

        var spent = starService.spendStarByUserId(course.get().getStarPrice(), user.getId(),
            "Thanh toán khóa học #" + courseId + ": " + course.get().getName(), StarTransactionType.star_payment);

        if (!spent) {
            return error("Không đủ sao để mở khóa học này.", 422,
                Map.of("required_star", course.get().getStarPrice(), "star_balance", starService.getBalance(user.getId())));
        }

        orderService.createWithStarPayment(user.getId(), courseId);

        return success("Mở khóa học thành công.", Map.of("enrolled", true, "star_balance", starService.getBalance(user.getId())));
    }
}
