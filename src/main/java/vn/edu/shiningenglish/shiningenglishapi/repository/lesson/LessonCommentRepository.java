package vn.edu.shiningenglish.shiningenglishapi.repository.lesson;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.shiningenglish.shiningenglishapi.model.entity.LessonComment;

public interface LessonCommentRepository extends JpaRepository<LessonComment, Long> {
}
