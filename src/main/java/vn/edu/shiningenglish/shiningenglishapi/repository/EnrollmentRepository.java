package vn.edu.shiningenglish.shiningenglishapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.edu.shiningenglish.shiningenglishapi.model.entity.Enrollment;
import java.util.List;
import java.util.Optional;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    Optional<Enrollment> findByUserIdAndCourseId(Long userId, Long courseId);
    
    @Query("SELECT e FROM Enrollment e WHERE e.userId = :userId AND e.courseId = :courseId")
    Optional<Enrollment> findByUserIdAndCourseIdWithTrashed(@Param("userId") Long userId, @Param("courseId") Long courseId);

    List<Enrollment> findByUserId(Long userId);
}
