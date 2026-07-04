package vn.edu.shiningenglish.shiningenglishapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.shiningenglish.shiningenglishapi.model.entity.Level;

public interface LevelRepository extends JpaRepository<Level, Long> {
}
