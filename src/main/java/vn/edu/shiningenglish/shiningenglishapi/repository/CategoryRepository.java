package vn.edu.shiningenglish.shiningenglishapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import vn.edu.shiningenglish.shiningenglishapi.model.entity.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}
