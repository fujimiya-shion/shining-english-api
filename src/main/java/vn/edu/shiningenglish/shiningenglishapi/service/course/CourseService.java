package vn.edu.shiningenglish.shiningenglishapi.service.course;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import jakarta.persistence.criteria.Predicate;
import vn.edu.shiningenglish.shiningenglishapi.model.entity.Course;
import vn.edu.shiningenglish.shiningenglishapi.repository.CategoryRepository;
import vn.edu.shiningenglish.shiningenglishapi.repository.LevelRepository;
import vn.edu.shiningenglish.shiningenglishapi.repository.course.CourseRepository;
import vn.edu.shiningenglish.shiningenglishapi.repository.lesson.LessonRepository;
import vn.edu.shiningenglish.shiningenglishapi.valueobject.CourseFilter;
import vn.edu.shiningenglish.shiningenglishapi.valueobject.QueryOption;

@Service
public class CourseService {

    private final CourseRepository courseRepository;
    private final CategoryRepository categoryRepository;
    private final LevelRepository levelRepository;
    private final LessonRepository lessonRepository;

    public CourseService(CourseRepository courseRepository, CategoryRepository categoryRepository,
                         LevelRepository levelRepository, LessonRepository lessonRepository) {
        this.courseRepository = courseRepository;
        this.categoryRepository = categoryRepository;
        this.levelRepository = levelRepository;
        this.lessonRepository = lessonRepository;
    }

    public Optional<Course> getById(Long id) {
        return courseRepository.findWithLessonsById(id);
    }

    public Optional<Course> getBySlug(String slug) {
        return courseRepository.findWithLessonsBySlugAndStatus(slug, true);
    }

    public Page<Course> getFree(QueryOption options) {
        var pageable = PageRequest.of(
            options.getPage() != null ? options.getPage() - 1 : 0,
            options.getPerPage()
        );
        Specification<Course> spec = (root, query, cb) -> {
            var predicates = new ArrayList<jakarta.persistence.criteria.Predicate>();
            predicates.add(cb.isTrue(root.get("status")));
            predicates.add(cb.or(cb.isNull(root.get("price")), cb.equal(root.get("price"), 0)));
            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
        return courseRepository.findAll(spec, pageable);
    }

    public Page<Course> getAll(QueryOption options) {
        var pageable = PageRequest.of(
            options.getPage() != null ? options.getPage() - 1 : 0,
            options.getPerPage(),
            org.springframework.data.domain.Sort.by(
                options.getOrderDirection().equals("asc") ? org.springframework.data.domain.Sort.Direction.ASC : org.springframework.data.domain.Sort.Direction.DESC,
                options.getOrderBy()
            )
        );
        return courseRepository.findAll(pageable);
    }

    public Page<Course> filter(CourseFilter filters) {
        var options = filters.getOptions() != null ? filters.getOptions() : new QueryOption();
        var pageable = PageRequest.of(
            options.getPage() != null ? options.getPage() - 1 : 0,
            options.getPerPage()
        );

        Specification<Course> spec = (root, query, cb) -> {
            var predicates = new ArrayList<Predicate>();

            predicates.add(cb.isTrue(root.get("status")));

            if (filters.getCategoryId() != null) {
                predicates.add(cb.equal(root.get("categoryId"), filters.getCategoryId()));
            }
            if (filters.getLevelId() != null) {
                predicates.add(cb.equal(root.get("levelId"), filters.getLevelId()));
            }
            if (filters.getPriceMin() != null && filters.getPriceMax() != null) {
                predicates.add(cb.between(root.get("price"), filters.getPriceMin(), filters.getPriceMax()));
            } else if (filters.getPriceMin() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("price"), filters.getPriceMin()));
            } else if (filters.getPriceMax() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("price"), filters.getPriceMax()));
            }
            if (filters.getRatingMin() != null && filters.getRatingMax() != null) {
                predicates.add(cb.between(root.get("rating"), filters.getRatingMin(), filters.getRatingMax()));
            } else if (filters.getRatingMin() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("rating"), filters.getRatingMin()));
            } else if (filters.getRatingMax() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("rating"), filters.getRatingMax()));
            }
            if (filters.getLearnedMin() != null && filters.getLearnedMax() != null) {
                predicates.add(cb.between(root.get("learned"), filters.getLearnedMin(), filters.getLearnedMax()));
            } else if (filters.getLearnedMin() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("learned"), filters.getLearnedMin()));
            } else if (filters.getLearnedMax() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("learned"), filters.getLearnedMax()));
            }
            if (filters.getKeyword() != null && !filters.getKeyword().isBlank()) {
                var keyword = filters.getKeyword().trim();
                var likePattern = "%" + keyword + "%";
                predicates.add(cb.or(
                    cb.like(cb.lower(root.get("name")), likePattern.toLowerCase()),
                    cb.like(cb.lower(root.get("slug")), likePattern.toLowerCase())
                ));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return courseRepository.findAll(spec, pageable);
    }

    public Map<String, Object> getFilterProps() {
        var categories = categoryRepository.findAll().stream()
            .map(c -> Map.of("id", c.getId(), "name", c.getName(), "slug", c.getSlug()))
            .collect(Collectors.toList());

        var activeCourses = courseRepository.findAll().stream()
            .filter(c -> Boolean.TRUE.equals(c.getStatus()))
            .collect(Collectors.toList());

        var priceMin = activeCourses.stream().mapToInt(c -> c.getPrice() != null ? c.getPrice() : 0).min().orElse(0);
        var priceMax = activeCourses.stream().mapToInt(c -> c.getPrice() != null ? c.getPrice() : 0).max().orElse(0);
        var ratingMin = activeCourses.stream().mapToDouble(c -> c.getRating() != null ? c.getRating() : 0.0).min().orElse(0.0);
        var ratingMax = activeCourses.stream().mapToDouble(c -> c.getRating() != null ? c.getRating() : 0.0).max().orElse(5.0);
        var learnedMin = activeCourses.stream().mapToInt(c -> c.getLearned() != null ? c.getLearned() : 0).min().orElse(0);
        var learnedMax = activeCourses.stream().mapToInt(c -> c.getLearned() != null ? c.getLearned() : 0).max().orElse(1000);

        var levels = levelRepository.findAll().stream()
            .map(l -> {
                var count = activeCourses.stream().filter(c -> l.getId().equals(c.getLevelId())).count();
                return Map.of("value", l.getId(), "label", l.getName(), "count", count);
            })
            .collect(Collectors.toList());

        var durationOptions = List.of(
            Map.of("min_hours", 0, "max_hours", 3, "label", "< 4 giờ", "count", 0),
            Map.of("min_hours", 4, "max_hours", 8, "label", "4 - 8 giờ", "count", 0),
            Map.of("min_hours", 9, "max_hours", 9999, "label", "> 8 giờ", "count", 0)
        );

        var result = new LinkedHashMap<String, Object>();
        result.put("categories", categories);
        result.put("price", Map.of("min", priceMin, "max", priceMax));
        result.put("rating", Map.of("min", ratingMin, "max", ratingMax));
        result.put("learned", Map.of("min", learnedMin, "max", learnedMax));
        result.put("levels", levels);
        result.put("duration_hours", durationOptions);
        return result;
    }
}
