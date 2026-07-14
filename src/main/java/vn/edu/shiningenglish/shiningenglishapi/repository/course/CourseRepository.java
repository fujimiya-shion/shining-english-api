package vn.edu.shiningenglish.shiningenglishapi.repository.course;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import vn.edu.shiningenglish.shiningenglishapi.model.entity.Course;
import java.util.Optional;

public interface CourseRepository extends JpaRepository<Course, Long>, JpaSpecificationExecutor<Course> {
    Optional<Course> findBySlugAndStatus(String slug, Boolean status);
    Optional<Course> findBySlug(String slug);

    @EntityGraph(attributePaths = "lessons")
    Optional<Course> findWithLessonsById(Long id);

    @EntityGraph(attributePaths = "lessons")
    Optional<Course> findWithLessonsBySlugAndStatus(String slug, Boolean status);
}
