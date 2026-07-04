package vn.edu.shiningenglish.shiningenglishapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.shiningenglish.shiningenglishapi.model.entity.PersonalAccessToken;
import java.util.Optional;

public interface PersonalAccessTokenRepository extends JpaRepository<PersonalAccessToken, Long> {
    Optional<PersonalAccessToken> findByToken(String token);
}
