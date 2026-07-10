package vn.edu.shiningenglish.shiningenglishapi.controller.v1.quiz;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import vn.edu.shiningenglish.shiningenglishapi.common.BaseController;
import vn.edu.shiningenglish.shiningenglishapi.model.dto.request.CreateAttemptRequest;
import vn.edu.shiningenglish.shiningenglishapi.model.entity.User;
import vn.edu.shiningenglish.shiningenglishapi.service.quiz.UserQuizAttemptService;
import vn.edu.shiningenglish.shiningenglishapi.valueobject.MetaPagination;
import vn.edu.shiningenglish.shiningenglishapi.valueobject.QueryOption;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/quizzes/{quizId}/attempts")
public class QuizAttemptController extends BaseController {

    private final UserQuizAttemptService userQuizAttemptService;

    public QuizAttemptController(UserQuizAttemptService userQuizAttemptService) {
        this.userQuizAttemptService = userQuizAttemptService;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> index(Authentication auth, @PathVariable Long quizId,
                                                     @RequestParam Map<String, String> params) {
        var user = (User) auth.getPrincipal();
        var options = QueryOption.fromArray(params, true);
        var page = userQuizAttemptService.paginateByUserIdAndQuizId(user.getId(), quizId, options);
        var meta = MetaPagination.fromPage(page);
        return success("OK", page.getContent(), 200, meta.toArray());
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> store(Authentication auth, @PathVariable Long quizId,
                                                     @Valid @RequestBody CreateAttemptRequest request) {
        var user = (User) auth.getPrincipal();
        var attempt = userQuizAttemptService.recordAttempt(
            user.getId(), quizId,
            request.scorePercent(),
            request.passed(),
            request.submittedAt()
        );
        return created(attempt, "Attempt recorded");
    }

    @GetMapping("/latest")
    public ResponseEntity<Map<String, Object>> latest(Authentication auth, @PathVariable Long quizId) {
        var user = (User) auth.getPrincipal();
        var attempt = userQuizAttemptService.latestAttempt(user.getId(), quizId);
        if (attempt == null) return notfound("Attempt not found");
        return success(attempt);
    }
}
