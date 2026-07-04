package vn.edu.shiningenglish.shiningenglishapi.repository;

import java.time.LocalDateTime;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import vn.edu.shiningenglish.shiningenglishapi.model.entity.DailyCheckIn;

public interface DailyCheckInRepository extends JpaRepository<DailyCheckIn, Long> {
    @Query("SELECT COUNT(d) > 0 FROM DailyCheckIn d WHERE d.userId = :userId AND d.checkedInAt >= :start AND d.checkedInAt < :end")
    boolean existsByUserIdAndCheckedInAtBetween(@Param("userId") Long userId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
