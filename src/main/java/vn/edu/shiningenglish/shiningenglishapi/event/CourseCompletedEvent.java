package vn.edu.shiningenglish.shiningenglishapi.event;

public class CourseCompletedEvent {
    private final Long userId;
    private final Long courseId;
    private final int completionRewardAmount;

    public CourseCompletedEvent(Long userId, Long courseId, int completionRewardAmount) {
        this.userId = userId;
        this.courseId = courseId;
        this.completionRewardAmount = completionRewardAmount;
    }

    public Long getUserId() { return userId; }
    public Long getCourseId() { return courseId; }
    public int getCompletionRewardAmount() { return completionRewardAmount; }
}
