package vn.edu.shiningenglish.shiningenglishapi.repository.lesson;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.shiningenglish.shiningenglishapi.model.entity.LessonNote;
import java.util.List;

public interface LessonNoteRepository extends JpaRepository<LessonNote, Long> {
    List<LessonNote> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<LessonNote> findByUserIdAndLessonIdOrderByCreatedAtDesc(Long userId, Long lessonId);
    void deleteByUserIdAndId(Long userId, Long id);
}
