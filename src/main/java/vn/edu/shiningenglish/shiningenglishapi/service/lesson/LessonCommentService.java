package vn.edu.shiningenglish.shiningenglishapi.service.lesson;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.shiningenglish.shiningenglishapi.model.entity.LessonComment;
import vn.edu.shiningenglish.shiningenglishapi.repository.lesson.LessonCommentRepository;

@Service
public class LessonCommentService {

    private final LessonCommentRepository lessonCommentRepository;

    public LessonCommentService(LessonCommentRepository lessonCommentRepository) {
        this.lessonCommentRepository = lessonCommentRepository;
    }

    @Transactional
    public LessonComment createForUser(Long lessonId, Long userId, String content) {
        var comment = new LessonComment();
        comment.setLessonId(lessonId);
        comment.setUserId(userId);
        comment.setContent(content);
        return lessonCommentRepository.save(comment);
    }
}
