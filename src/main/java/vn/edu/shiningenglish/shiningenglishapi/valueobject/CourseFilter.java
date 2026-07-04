package vn.edu.shiningenglish.shiningenglishapi.valueobject;

import java.util.Map;

public class CourseFilter {
    private Long categoryId;
    private Long levelId;
    private Integer priceMin;
    private Integer priceMax;
    private Float durationMinHours;
    private Float durationMaxHours;
    private Float ratingMin;
    private Float ratingMax;
    private Integer learnedMin;
    private Integer learnedMax;
    private String keyword;
    private QueryOption options;

    public CourseFilter(Long categoryId, Long levelId, Integer priceMin, Integer priceMax,
                       Float durationMinHours, Float durationMaxHours,
                       Float ratingMin, Float ratingMax,
                       Integer learnedMin, Integer learnedMax,
                       String keyword, QueryOption options) {
        this.categoryId = categoryId;
        this.levelId = levelId;
        this.priceMin = priceMin;
        this.priceMax = priceMax;
        this.durationMinHours = durationMinHours;
        this.durationMaxHours = durationMaxHours;
        this.ratingMin = ratingMin;
        this.ratingMax = ratingMax;
        this.learnedMin = learnedMin;
        this.learnedMax = learnedMax;
        this.keyword = keyword;
        this.options = options;
    }

    public static CourseFilter fromArray(Map<String, String> params) {
        var options = QueryOption.fromArray(params, true);
        var q = params.get("q");
        if (q == null || q.isBlank()) {
            q = params.get("name");
        }
        if (q != null && q.isBlank()) q = null;

        return new CourseFilter(
            params.containsKey("category_id") ? Long.parseLong(params.get("category_id")) : null,
            params.containsKey("level_id") ? Long.parseLong(params.get("level_id")) : null,
            params.containsKey("price_min") ? Integer.parseInt(params.get("price_min")) : null,
            params.containsKey("price_max") ? Integer.parseInt(params.get("price_max")) : null,
            params.containsKey("duration_min_hours") ? Float.parseFloat(params.get("duration_min_hours")) : null,
            params.containsKey("duration_max_hours") ? Float.parseFloat(params.get("duration_max_hours")) : null,
            params.containsKey("rating_min") ? Float.parseFloat(params.get("rating_min")) : null,
            params.containsKey("rating_max") ? Float.parseFloat(params.get("rating_max")) : null,
            params.containsKey("learned_min") ? Integer.parseInt(params.get("learned_min")) : null,
            params.containsKey("learned_max") ? Integer.parseInt(params.get("learned_max")) : null,
            q, options
        );
    }

    public Long getCategoryId() { return categoryId; }
    public Long getLevelId() { return levelId; }
    public Integer getPriceMin() { return priceMin; }
    public Integer getPriceMax() { return priceMax; }
    public Float getDurationMinHours() { return durationMinHours; }
    public Float getDurationMaxHours() { return durationMaxHours; }
    public Float getRatingMin() { return ratingMin; }
    public Float getRatingMax() { return ratingMax; }
    public Integer getLearnedMin() { return learnedMin; }
    public Integer getLearnedMax() { return learnedMax; }
    public String getKeyword() { return keyword; }
    public QueryOption getOptions() { return options; }
}
