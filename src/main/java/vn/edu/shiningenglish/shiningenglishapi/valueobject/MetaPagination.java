package vn.edu.shiningenglish.shiningenglishapi.valueobject;

import org.springframework.data.domain.Page;

public class MetaPagination {
    private int page;
    private int perPage;
    private long total;
    private int pageCount;

    public MetaPagination(int page, int perPage, long total, int pageCount) {
        this.page = page;
        this.perPage = perPage;
        this.total = total;
        this.pageCount = pageCount;
    }

    public static MetaPagination fromPage(Page<?> page) {
        return new MetaPagination(
            page.getNumber() + 1,
            page.getSize(),
            page.getTotalElements(),
            page.getTotalPages()
        );
    }

    public java.util.Map<String, Object> toArray() {
        return java.util.Map.of(
            "page", page,
            "per_page", perPage,
            "total", total,
            "page_count", pageCount
        );
    }

    public int getPage() { return page; }
    public int getPerPage() { return perPage; }
    public long getTotal() { return total; }
    public int getPageCount() { return pageCount; }
}
