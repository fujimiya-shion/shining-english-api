package vn.edu.shiningenglish.shiningenglishapi.repository.quiz;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.shiningenglish.shiningenglishapi.model.entity.QuizAnswer;
import java.util.List;

public interface QuizAnswerRepository extends JpaRepository<QuizAnswer, Long> {
    List<QuizAnswer> findByQuizQuestionId(Long quizQuestionId);
}
