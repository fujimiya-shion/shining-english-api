package vn.edu.shiningenglish.shiningenglishapi.repository.blog;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.shiningenglish.shiningenglishapi.model.entity.BlogTag;

public interface BlogTagRepository extends JpaRepository<BlogTag, Long> {
}
