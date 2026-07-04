package vn.edu.shiningenglish.shiningenglishapi.repository.quiz;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import vn.edu.shiningenglish.shiningenglishapi.model.entity.UserQuizAttempt;

public interface UserQuizAttemptRepository extends JpaRepository<UserQuizAttempt, Long> {
    Page<UserQuizAttempt> findByUserIdAndQuizIdOrderByCreatedAtDesc(Long userId, Long quizId, Pageable pageable);
    
    @Query("SELECT q FROM UserQuizAttempt q WHERE q.userId = :userId AND q.quizId = :quizId ORDER BY q.submittedAt DESC")
    List<UserQuizAttempt> findLatestByUserIdAndQuizId(@Param("userId") Long userId, @Param("quizId") Long quizId, Pageable pageable);

    List<UserQuizAttempt> findByUserIdOrderBySubmittedAtDesc(Long userId, Pageable pageable);
}
