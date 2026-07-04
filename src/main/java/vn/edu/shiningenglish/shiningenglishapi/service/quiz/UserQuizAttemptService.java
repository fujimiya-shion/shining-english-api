package vn.edu.shiningenglish.shiningenglishapi.service.quiz;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.shiningenglish.shiningenglishapi.model.entity.UserQuizAttempt;
import vn.edu.shiningenglish.shiningenglishapi.repository.quiz.UserQuizAttemptRepository;
import vn.edu.shiningenglish.shiningenglishapi.valueobject.QueryOption;

import java.time.LocalDateTime;

@Service
public class UserQuizAttemptService {

    private final UserQuizAttemptRepository userQuizAttemptRepository;

    public UserQuizAttemptService(UserQuizAttemptRepository userQuizAttemptRepository) {
        this.userQuizAttemptRepository = userQuizAttemptRepository;
    }

    public Page<UserQuizAttempt> paginateByUserIdAndQuizId(Long userId, Long quizId, QueryOption options) {
        var pageable = org.springframework.data.domain.PageRequest.of(
            options.getPage() != null ? options.getPage() - 1 : 0,
            options.getPerPage(),
            org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "createdAt")
        );
        return userQuizAttemptRepository.findByUserIdAndQuizIdOrderByCreatedAtDesc(userId, quizId, pageable);
    }

    public UserQuizAttempt latestAttempt(Long userId, Long quizId) {
        var results = userQuizAttemptRepository.findLatestByUserIdAndQuizId(userId, quizId, 
            org.springframework.data.domain.PageRequest.of(0, 1));
        return results.isEmpty() ? null : results.get(0);
    }

    @Transactional
    public UserQuizAttempt recordAttempt(Long userId, Long quizId, double scorePercent, boolean passed, LocalDateTime submittedAt) {
        var attempt = new UserQuizAttempt();
        attempt.setUserId(userId);
        attempt.setQuizId(quizId);
        attempt.setScorePercent(scorePercent);
        attempt.setPassed(passed);
        attempt.setSubmittedAt(submittedAt != null ? submittedAt : LocalDateTime.now());
        return userQuizAttemptRepository.save(attempt);
    }
}
