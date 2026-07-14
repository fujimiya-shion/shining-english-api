package vn.edu.shiningenglish.shiningenglishapi.controller.v1.lesson;

import java.util.LinkedHashMap;
import java.util.Map;

import jakarta.validation.Valid;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import vn.edu.shiningenglish.shiningenglishapi.common.BaseController;
import vn.edu.shiningenglish.shiningenglishapi.model.entity.User;
import vn.edu.shiningenglish.shiningenglishapi.model.dto.request.CreateCommentRequest;
import vn.edu.shiningenglish.shiningenglishapi.repository.quiz.QuizAnswerRepository;
import vn.edu.shiningenglish.shiningenglishapi.repository.quiz.QuizQuestionRepository;
import vn.edu.shiningenglish.shiningenglishapi.repository.quiz.QuizRepository;
import vn.edu.shiningenglish.shiningenglishapi.service.lesson.LessonAccessService;
import vn.edu.shiningenglish.shiningenglishapi.service.lesson.LessonCommentService;
import vn.edu.shiningenglish.shiningenglishapi.service.lesson.LessonService;
import vn.edu.shiningenglish.shiningenglishapi.valueobject.QueryOption;

@RestController
@RequestMapping("/api/v1/lessons")
public class LessonController extends BaseController {

    private final LessonService lessonService;
    private final LessonAccessService lessonAccessService;
    private final LessonCommentService lessonCommentService;
    private final QuizRepository quizRepository;
    private final QuizQuestionRepository quizQuestionRepository;
    private final QuizAnswerRepository quizAnswerRepository;

    public LessonController(LessonService lessonService, LessonAccessService lessonAccessService,
                            LessonCommentService lessonCommentService, QuizRepository quizRepository,
                            QuizQuestionRepository quizQuestionRepository, QuizAnswerRepository quizAnswerRepository) {
        this.lessonService = lessonService;
        this.lessonAccessService = lessonAccessService;
        this.lessonCommentService = lessonCommentService;
        this.quizRepository = quizRepository;
        this.quizQuestionRepository = quizQuestionRepository;
        this.quizAnswerRepository = quizAnswerRepository;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> index(@RequestParam Map<String, String> params) {
        var options = QueryOption.fromArray(params, true);
        return success(lessonService.getAll(options));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> show(@PathVariable Long id) {
        var lesson = lessonService.getById(id);
        if (lesson.isEmpty()) return notfound();
        return success(lesson.get());
    }

    @GetMapping("/{id}/video")
    public ResponseEntity<?> video(Authentication auth, @PathVariable Long id) {
        var lesson = lessonService.getById(id);
        if (lesson.isEmpty()) return notfound();

        var userId = auth != null ? ((User) auth.getPrincipal()).getId() : null;
        if (!lessonAccessService.canWatchLessonVideo(userId, lesson.get())) {
            return unauthorized("Lesson video access denied");
        }

        var path = lesson.get().getVideoUrl();
        if (path == null || path.isBlank()) return notfound();

        try {
            var file = new java.io.File(path);
            if (!file.exists()) return notfound();
            var resource = new FileSystemResource(file);
            return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + file.getName() + "\"")
                .body(resource);
        } catch (Exception e) {
            return notfound();
        }
    }

    @GetMapping("/{id}/documents/{documentIndex}/download")
    public ResponseEntity<?> downloadDocument(Authentication auth, @PathVariable Long id, @PathVariable int documentIndex) {
        var lesson = lessonService.getById(id);
        if (lesson.isEmpty()) return notfound();

        var userId = auth != null ? ((User) auth.getPrincipal()).getId() : null;
        if (!lessonAccessService.canAccessLessonProtectedContent(userId, lesson.get())) {
            return unauthorized("Lesson access denied");
        }

        var paths = lesson.get().getDocuments();
        if (paths == null || paths.isEmpty()) return notfound();
        if (documentIndex < 0 || documentIndex >= paths.size()) return notfound();
        var path = paths.get(documentIndex);
        if (path == null || path.isBlank()) return notfound();

        var file = new java.io.File(path);
        if (!file.exists()) return notfound();
        var resource = new FileSystemResource(file);

        var documentNames = lesson.get().getDocumentNames();
        var fileName = file.getName();
        if (documentNames != null && documentNames.containsKey(path)) {
            fileName = documentNames.get(path);
        }

        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
            .body(resource);
    }

    @GetMapping("/{id}/quiz")
    public ResponseEntity<Map<String, Object>> quiz(Authentication auth, @PathVariable Long id) {
        var lesson = lessonService.getById(id);
        if (lesson.isEmpty()) return notfound();

        var userId = auth != null ? ((User) auth.getPrincipal()).getId() : null;
        if (!lessonAccessService.canAccessLessonProtectedContent(userId, lesson.get())) {
            return unauthorized("Lesson access denied");
        }

        var quizOpt = quizRepository.findByLessonId(lesson.get().getId());
        if (quizOpt.isEmpty()) return notfound("Quiz not found");

        var quiz = quizOpt.get();
        var questions = quizQuestionRepository.findByQuizId(quiz.getId());

        var questionList = new java.util.ArrayList<Map<String, Object>>();
        for (var question : questions) {
            var answers = quizAnswerRepository.findByQuizQuestionId(question.getId());
            var answerList = new java.util.ArrayList<Map<String, Object>>();
            for (var answer : answers) {
                var answerMap = new LinkedHashMap<String, Object>();
                answerMap.put("id", answer.getId());
                answerMap.put("content", answer.getContent());
                answerMap.put("is_correct", answer.getIsCorrect());
                answerList.add(answerMap);
            }
            var questionMap = new LinkedHashMap<String, Object>();
            questionMap.put("id", question.getId());
            questionMap.put("content", question.getContent());
            questionMap.put("answers", answerList);
            questionList.add(questionMap);
        }

        var quizData = new LinkedHashMap<String, Object>();
        quizData.put("id", quiz.getId());
        quizData.put("lesson_id", quiz.getLessonId());
        quizData.put("pass_percent", quiz.getPassPercent());
        quizData.put("questions", questionList);

        return success("Get Quiz Successfully", Map.of("quiz", quizData));
    }

    @PostMapping("/{id}/comments")
    public ResponseEntity<Map<String, Object>> storeComment(Authentication auth, @PathVariable Long id,
                                                            @Valid @RequestBody CreateCommentRequest request) {
        var lesson = lessonService.getById(id);
        if (lesson.isEmpty()) return notfound();

        var user = (User) auth.getPrincipal();
        if (!lessonAccessService.canWatchLessonVideo(user.getId(), lesson.get())) {
            return unauthorized("Lesson access denied");
        }

        var comment = lessonCommentService.createForUser(id, user.getId(), request.content());
        return created(comment, "Comment submitted");
    }
}
