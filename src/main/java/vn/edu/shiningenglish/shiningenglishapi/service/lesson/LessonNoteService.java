package vn.edu.shiningenglish.shiningenglishapi.service.lesson;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.shiningenglish.shiningenglishapi.model.entity.LessonNote;
import vn.edu.shiningenglish.shiningenglishapi.repository.lesson.LessonNoteRepository;

import java.util.List;

@Service
public class LessonNoteService {

    private final LessonNoteRepository lessonNoteRepository;

    public LessonNoteService(LessonNoteRepository lessonNoteRepository) {
        this.lessonNoteRepository = lessonNoteRepository;
    }

    public List<LessonNote> listByUserId(Long userId) {
        return lessonNoteRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public List<LessonNote> listByLessonId(Long userId, Long lessonId) {
        return lessonNoteRepository.findByUserIdAndLessonIdOrderByCreatedAtDesc(userId, lessonId);
    }

    @Transactional
    public LessonNote createForUser(Long userId, Long lessonId, String content) {
        var note = new LessonNote();
        note.setUserId(userId);
        note.setLessonId(lessonId);
        note.setContent(content);
        return lessonNoteRepository.save(note);
    }

    @Transactional
    public boolean deleteByUserId(Long userId, Long noteId) {
        var note = lessonNoteRepository.findById(noteId);
        if (note.isEmpty() || !note.get().getUserId().equals(userId)) return false;
        lessonNoteRepository.delete(note.get());
        return true;
    }
}
