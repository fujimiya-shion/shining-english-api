package vn.edu.shiningenglish.shiningenglishapi.valueobject;

public class QueryOption {
    private Integer page;
    private int perPage = 15;
    private String[] with = new String[0];
    private String orderBy = "createdAt";
    private String orderDirection = "desc";

    public QueryOption() {}

    public QueryOption(Integer page, int perPage) {
        this.page = page;
        this.perPage = perPage;
    }

    public static QueryOption fromArray(java.util.Map<String, String> params, boolean forcePagination) {
        var dto = new QueryOption();
        if (forcePagination) {
            dto.setPage(1);
            dto.setPerPage(15);
        }
        if (params.containsKey("page")) {
            dto.setPage(Integer.parseInt(params.get("page")));
        }
        if (params.containsKey("perPage")) {
            dto.setPerPage(Integer.parseInt(params.get("perPage")));
        }
        if (params.containsKey("orderBy")) {
            dto.setOrderBy(params.get("orderBy"));
        }
        if (params.containsKey("orderDirection")) {
            dto.setOrderDirection(params.get("orderDirection"));
        }
        return dto;
    }

    public Integer getPage() { return page; }
    public void setPage(Integer page) { this.page = page != null ? Math.max(1, page) : null; }
    public int getPerPage() { return perPage; }
    public void setPerPage(int perPage) { this.perPage = Math.max(1, perPage); }
    public String[] getWith() { return with; }
    public void setWith(String[] with) { this.with = with != null ? with : new String[0]; }
    public String getOrderBy() { return orderBy; }
    public void setOrderBy(String orderBy) {
        if (orderBy != null && !orderBy.isBlank()) {
            this.orderBy = snakeToCamel(orderBy.trim());
        }
    }

    private static String snakeToCamel(String s) {
        var parts = s.split("_");
        var result = new StringBuilder(parts[0]);
        for (int i = 1; i < parts.length; i++) {
            if (!parts[i].isEmpty()) {
                result.append(Character.toUpperCase(parts[i].charAt(0)));
                result.append(parts[i].substring(1));
            }
        }
        return result.toString();
    }
    public String getOrderDirection() { return orderDirection; }
    public void setOrderDirection(String orderDirection) {
        var dir = orderDirection != null ? orderDirection.trim().toLowerCase() : "desc";
        this.orderDirection = dir.equals("asc") ? "asc" : "desc";
    }
}
