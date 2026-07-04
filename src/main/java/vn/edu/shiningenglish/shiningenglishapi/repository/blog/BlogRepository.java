package vn.edu.shiningenglish.shiningenglishapi.repository.blog;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.shiningenglish.shiningenglishapi.model.entity.Blog;
import java.util.List;
import java.util.Optional;

public interface BlogRepository extends JpaRepository<Blog, Long> {
    List<Blog> findByStatusTrueOrderByCreatedAtDesc();
    Optional<Blog> findBySlugAndStatusTrue(String slug);
}
