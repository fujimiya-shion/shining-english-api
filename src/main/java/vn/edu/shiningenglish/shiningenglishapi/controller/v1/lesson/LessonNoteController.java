package vn.edu.shiningenglish.shiningenglishapi.controller.v1.lesson;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import vn.edu.shiningenglish.shiningenglishapi.common.BaseController;
import vn.edu.shiningenglish.shiningenglishapi.model.entity.User;
import vn.edu.shiningenglish.shiningenglishapi.service.lesson.LessonNoteService;
import vn.edu.shiningenglish.shiningenglishapi.service.lesson.LessonService;

@RestController
@RequestMapping("/api/v1")
public class LessonNoteController extends BaseController {

    private final LessonNoteService lessonNoteService;
    private final LessonService lessonService;

    public LessonNoteController(LessonNoteService lessonNoteService, LessonService lessonService) {
        this.lessonNoteService = lessonNoteService;
        this.lessonService = lessonService;
    }

    @GetMapping("/notes")
    public ResponseEntity<Map<String, Object>> index(Authentication auth) {
        var user = (User) auth.getPrincipal();
        return success("Get lesson notes successfully", lessonNoteService.listByUserId(user.getId()));
    }

    @GetMapping("/lessons/{id}/notes")
    public ResponseEntity<Map<String, Object>> indexByLesson(Authentication auth, @PathVariable Long id) {
        var user = (User) auth.getPrincipal();
        var lesson = lessonService.getById(id);
        if (lesson.isEmpty()) return notfound("Lesson not found");
        return success("Get lesson notes successfully", lessonNoteService.listByLessonId(user.getId(), id));
    }

    @PostMapping("/lessons/{id}/notes")
    public ResponseEntity<Map<String, Object>> store(Authentication auth, @PathVariable Long id, @RequestBody Map<String, Object> body) {
        var user = (User) auth.getPrincipal();
        var lesson = lessonService.getById(id);
        if (lesson.isEmpty()) return notfound("Lesson not found");

        var note = lessonNoteService.createForUser(user.getId(), id, (String) body.get("content"));
        return created(note, "Note created");
    }

    @DeleteMapping("/notes/{id}")
    public ResponseEntity<Map<String, Object>> delete(Authentication auth, @PathVariable Long id) {
        var user = (User) auth.getPrincipal();
        if (!lessonNoteService.deleteByUserId(user.getId(), id)) {
            return notfound("Note not found");
        }
        return deleted("Note deleted");
    }
}
