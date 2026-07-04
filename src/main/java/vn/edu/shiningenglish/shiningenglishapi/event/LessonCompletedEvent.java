package vn.edu.shiningenglish.shiningenglishapi.event;

public class LessonCompletedEvent {
    private final Long userId;
    private final Long courseId;
    private final Long lessonId;
    private final int starRewardAmount;
    private final String lessonName;

    public LessonCompletedEvent(Long userId, Long courseId, Long lessonId, int starRewardAmount, String lessonName) {
        this.userId = userId;
        this.courseId = courseId;
        this.lessonId = lessonId;
        this.starRewardAmount = starRewardAmount;
        this.lessonName = lessonName;
    }

    public Long getUserId() { return userId; }
    public Long getCourseId() { return courseId; }
    public Long getLessonId() { return lessonId; }
    public int getStarRewardAmount() { return starRewardAmount; }
    public String getLessonName() { return lessonName; }
}
