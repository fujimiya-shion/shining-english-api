package vn.edu.shiningenglish.shiningenglishapi.controller.v1.blog;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.shiningenglish.shiningenglishapi.common.BaseController;
import vn.edu.shiningenglish.shiningenglishapi.model.entity.Blog;
import vn.edu.shiningenglish.shiningenglishapi.repository.blog.BlogRepository;
import vn.edu.shiningenglish.shiningenglishapi.repository.blog.BlogTagRepository;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/blogs")
public class BlogController extends BaseController {

    private final BlogRepository blogRepository;
    private final BlogTagRepository blogTagRepository;

    public BlogController(BlogRepository blogRepository, BlogTagRepository blogTagRepository) {
        this.blogRepository = blogRepository;
        this.blogTagRepository = blogTagRepository;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> index() {
        var blogs = blogRepository.findByStatusTrueOrderByCreatedAtDesc();
        var tagIds = blogs.stream()
            .map(Blog::getTagId)
            .filter(java.util.Objects::nonNull)
            .collect(Collectors.toSet());
        var tags = blogTagRepository.findAllById(tagIds).stream()
            .map(t -> Map.of("id", t.getId(), "name", t.getName(), "slug", t.getSlug()))
            .collect(Collectors.toList());

        var items = blogs.stream()
            .map(b -> formatBlog(b, false))
            .collect(Collectors.toList());

        var data = new LinkedHashMap<String, Object>();
        data.put("items", items);
        data.put("topics", tags);
        return success(data);
    }

    @GetMapping("/slug/{slug}")
    public ResponseEntity<Map<String, Object>> showBySlug(@PathVariable String slug) {
        var blog = blogRepository.findBySlugAndStatusTrue(slug);
        if (blog.isEmpty()) return notfound();
        return success(Map.of("blog", formatBlog(blog.get(), true)));
    }

    private Map<String, Object> formatBlog(Blog blog, boolean includeContent) {
        var result = new LinkedHashMap<String, Object>();
        result.put("id", blog.getId());
        result.put("title", blog.getTitle());
        result.put("slug", blog.getSlug());
        result.put("description", blog.getDescription());
        result.put("short_description", blog.getShortDescription());
        result.put("thumbnail", blog.getThumbnail());
        result.put("read_time_minutes", blog.getReadTimeMinutes());
        result.put("published_at", blog.getCreatedAt() != null ? blog.getCreatedAt().toString() : null);
        if (blog.getTagId() != null) {
            var tag = blogTagRepository.findById(blog.getTagId());
            tag.ifPresent(t -> result.put("tag", Map.of("id", t.getId(), "name", t.getName(), "slug", t.getSlug())));
        } else {
            result.put("tag", null);
        }
        if (includeContent) {
            result.put("content", blog.getContent());
        } else {
            result.put("content", null);
        }
        return result;
    }
}
