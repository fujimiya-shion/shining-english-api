package vn.edu.shiningenglish.shiningenglishapi.model.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import vn.edu.shiningenglish.shiningenglishapi.model.converter.JsonStringListConverter;
import vn.edu.shiningenglish.shiningenglishapi.model.converter.JsonStringMapConverter;

@Entity
@Table(name = "lessons")
public class Lesson {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String slug;

    @Column(name = "course_id", nullable = false)
    private Long courseId;

    @Column(name = "lesson_group_id")
    private Long lessonGroupId;

    @Column(name = "group_name")
    private String groupName;

    @Column(name = "group_order")
    private Integer groupOrder = 0;

    @Column(name = "lesson_order")
    private Integer lessonOrder = 0;

    @Column(name = "video_url")
    private String videoUrl;

    @Convert(converter = JsonStringListConverter.class)
    @Column(columnDefinition = "JSON")
    private List<String> documents = new ArrayList<>();

    @Convert(converter = JsonStringMapConverter.class)
    @Column(name = "document_names", columnDefinition = "JSON")
    private Map<String, String> documentNames = new HashMap<>();

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "duration_minutes")
    private Integer durationMinutes = 0;

    @Column(name = "star_reward_video")
    private Integer starRewardVideo = 0;

    @Column(name = "star_reward_quiz")
    private Integer starRewardQuiz = 0;

    @Column(name = "has_quiz")
    private Boolean hasQuiz = false;

    @Column(name = "is_preview_free")
    private Boolean isPreviewFree = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() { updatedAt = LocalDateTime.now(); }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }
    public Long getCourseId() { return courseId; }
    public void setCourseId(Long courseId) { this.courseId = courseId; }
    public Long getLessonGroupId() { return lessonGroupId; }
    public void setLessonGroupId(Long lessonGroupId) { this.lessonGroupId = lessonGroupId; }
    public String getGroupName() { return groupName; }
    public void setGroupName(String groupName) { this.groupName = groupName; }
    public Integer getGroupOrder() { return groupOrder; }
    public void setGroupOrder(Integer groupOrder) { this.groupOrder = groupOrder; }
    public Integer getLessonOrder() { return lessonOrder; }
    public void setLessonOrder(Integer lessonOrder) { this.lessonOrder = lessonOrder; }
    public String getVideoUrl() { return videoUrl; }
    public void setVideoUrl(String videoUrl) { this.videoUrl = videoUrl; }
    public List<String> getDocuments() { return documents; }
    public void setDocuments(List<String> documents) { this.documents = documents; }
    public Map<String, String> getDocumentNames() { return documentNames; }
    public void setDocumentNames(Map<String, String> documentNames) { this.documentNames = documentNames; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Integer getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }
    public Integer getStarRewardVideo() { return starRewardVideo; }
    public void setStarRewardVideo(Integer starRewardVideo) { this.starRewardVideo = starRewardVideo; }
    public Integer getStarRewardQuiz() { return starRewardQuiz; }
    public void setStarRewardQuiz(Integer starRewardQuiz) { this.starRewardQuiz = starRewardQuiz; }
    public Boolean getHasQuiz() { return hasQuiz; }
    public void setHasQuiz(Boolean hasQuiz) { this.hasQuiz = hasQuiz; }
    public Boolean getIsPreviewFree() { return isPreviewFree; }
    public void setIsPreviewFree(Boolean isPreviewFree) { this.isPreviewFree = isPreviewFree; }
    public LocalDateTime getDeletedAt() { return deletedAt; }
    public void setDeletedAt(LocalDateTime deletedAt) { this.deletedAt = deletedAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
