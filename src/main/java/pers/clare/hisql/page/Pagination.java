package pers.clare.hisql.page;

public class Pagination {
    private Integer page;
    private Integer size;
    private String[] sorts;

    public static Pagination of(Integer page, Integer size) {
        return new Pagination(page, size, null);
    }

    public static Pagination of(Integer page, Integer size, String... sorts) {
        return new Pagination(page, size, sorts);
    }

    public Pagination(){}

    public Pagination(Integer page, Integer size, String[] sorts) {
        this.page = page;
        this.size = size;
        this.sorts = sorts;
    }

    public Integer getPage() {
        return page;
    }

    public Integer getSize() {
        return size;
    }

    public String[] getSorts() {
        return sorts;
    }

    public Pagination next() {
        return Pagination.of(page + 1, size, sorts);
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public void setSorts(String[] sorts) {
        this.sorts = sorts;
    }
}
