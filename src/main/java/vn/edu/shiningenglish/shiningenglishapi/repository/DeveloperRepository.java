package vn.edu.shiningenglish.shiningenglishapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.shiningenglish.shiningenglishapi.model.entity.Developer;
import java.util.Optional;

public interface DeveloperRepository extends JpaRepository<Developer, Long> {
    Optional<Developer> findByEmail(String email);
}
