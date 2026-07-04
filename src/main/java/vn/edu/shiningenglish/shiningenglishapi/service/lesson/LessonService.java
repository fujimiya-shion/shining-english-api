package vn.edu.shiningenglish.shiningenglishapi.service.lesson;

import org.springframework.stereotype.Service;
import vn.edu.shiningenglish.shiningenglishapi.model.entity.Lesson;
import vn.edu.shiningenglish.shiningenglishapi.repository.lesson.LessonRepository;
import vn.edu.shiningenglish.shiningenglishapi.valueobject.QueryOption;

import java.util.List;
import java.util.Optional;

@Service
public class LessonService {

    private final LessonRepository lessonRepository;

    public LessonService(LessonRepository lessonRepository) {
        this.lessonRepository = lessonRepository;
    }

    public Optional<Lesson> getById(Long id) {
        return lessonRepository.findById(id);
    }

    public List<Lesson> getByCourseId(Long courseId) {
        return lessonRepository.findByCourseIdOrderByGroupOrderAscLessonOrderAscIdAsc(courseId);
    }

    public List<Lesson> getAll(QueryOption options) {
        return lessonRepository.findAll();
    }
}
