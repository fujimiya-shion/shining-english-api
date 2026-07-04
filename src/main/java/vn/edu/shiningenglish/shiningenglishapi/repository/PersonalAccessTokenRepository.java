package vn.edu.shiningenglish.shiningenglishapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.edu.shiningenglish.shiningenglishapi.model.entity.PersonalAccessToken;
import java.util.Optional;

public interface PersonalAccessTokenRepository extends JpaRepository<PersonalAccessToken, Long> {
    Optional<PersonalAccessToken> findByToken(String token);

    @Modifying
    @Query("DELETE FROM PersonalAccessToken t WHERE t.tokenableId = :userId AND t.tokenableType = 'App\\\\Model\\\\User'")
    void deleteAllByUserId(@Param("userId") Long userId);
}
