package vn.edu.shiningenglish.shiningenglishapi.controller.v1.user;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import vn.edu.shiningenglish.shiningenglishapi.common.BaseController;
import vn.edu.shiningenglish.shiningenglishapi.repository.blog.BlogRepository;
import vn.edu.shiningenglish.shiningenglishapi.repository.blog.BlogTagRepository;
import vn.edu.shiningenglish.shiningenglishapi.repository.course.CourseRepository;
import vn.edu.shiningenglish.shiningenglishapi.util.UrlBuilder;

@RestController
@RequestMapping("/api/v1/home")
public class HomeController extends BaseController {

    private final CourseRepository courseRepository;
    private final BlogRepository blogRepository;
    private final BlogTagRepository blogTagRepository;
    private final UrlBuilder urlBuilder;

    public HomeController(CourseRepository courseRepository, BlogRepository blogRepository,
                          BlogTagRepository blogTagRepository, UrlBuilder urlBuilder) {
        this.courseRepository = courseRepository;
        this.blogRepository = blogRepository;
        this.blogTagRepository = blogTagRepository;
        this.urlBuilder = urlBuilder;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> index(@RequestHeader(value = "User-Authorization", required = false) String userToken) {
        var courses = courseRepository.findAll().stream()
            .filter(c -> Boolean.TRUE.equals(c.getStatus()))
            .limit(8)
            .map(c -> {
                var m = new LinkedHashMap<String, Object>();
                m.put("id", c.getId());
                m.put("name", c.getName());
                m.put("slug", c.getSlug());
                m.put("price", c.getPrice());
                m.put("thumbnail", urlBuilder.buildThumbnailUrl(c.getThumbnail()));
                m.put("rating", c.getRating());
                m.put("learned", c.getLearned());
                m.put("category_id", c.getCategoryId());
                m.put("level_id", c.getLevelId());
                return m;
            })
            .collect(Collectors.toList());

        var blogs = blogRepository.findByStatusTrueOrderByCreatedAtDesc().stream()
            .limit(3)
            .map(b -> {
                var m = new LinkedHashMap<String, Object>();
                m.put("id", b.getId());
                m.put("title", b.getTitle());
                m.put("slug", b.getSlug());
                m.put("description", b.getDescription());
                m.put("thumbnail", b.getThumbnail());
                m.put("read_time_minutes", b.getReadTimeMinutes());
                return m;
            })
            .collect(Collectors.toList());

        var data = new LinkedHashMap<String, Object>();
        data.put("hero", Map.of(
            "title", "Shining English",
            "subtitle", "Học tiếng Anh tự tin với lộ trình cá nhân hóa",
            "image", urlBuilder.buildThumbnailUrl("hero-image.jpg")
        ));
        data.put("features", java.util.List.of(
            Map.of("icon", "book", "title", "Khóa học chất lượng", "description", "Nội dung được biên soạn bởi chuyên gia"),
            Map.of("icon", "star", "title", "Học mọi lúc", "description", "Truy cập 24/7 từ mọi thiết bị"),
            Map.of("icon", "trophy", "title", "Theo dõi tiến độ", "description", "Theo dõi quá trình học tập của bạn")
        ));
        data.put("courses", courses);
        data.put("blogs", blogs);
        data.put("statistics", Map.of(
            "students", 1500,
            "courses", (int) courseRepository.count(),
            "teachers", 10
        ));
        data.put("cta", Map.of("title", "Bắt đầu học ngay hôm nay", "button_text", "Đăng ký miễn phí"));
        data.put("banners", java.util.List.of());
        return success(data);
    }
}
