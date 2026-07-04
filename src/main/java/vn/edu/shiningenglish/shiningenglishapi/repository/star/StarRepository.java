package vn.edu.shiningenglish.shiningenglishapi.repository.star;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.edu.shiningenglish.shiningenglishapi.model.entity.Star;
import java.util.Optional;

public interface StarRepository extends JpaRepository<Star, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Star s WHERE s.userId = :userId")
    Optional<Star> findForUpdateByUserId(@Param("userId") Long userId);

    Optional<Star> findByUserId(Long userId);
}
