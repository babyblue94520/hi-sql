package pers.clare.hisql.page;

import lombok.Getter;
import lombok.Setter;

@Getter
@SuppressWarnings("unused")
public class Pagination {
    @Setter
    private int page;
    @Setter
    private int size;

    /**
     * If total > 0, no more select count(*) will be executed.
     */
    @Setter
    private long total;

    @Setter
    private boolean virtualTotal;

    private String[] sorts;

    public Pagination() {
    }

    public Pagination(int page, int size, String[] sorts) {
        this(page, size, sorts, 0);
    }

    public Pagination(int page, int size, String[] sorts, long total) {
        this.page = page;
        this.size = size;
        this.sorts = sorts;
        this.total = total;
    }

    public static Pagination of(int page, int size) {
        return new Pagination(page, size, null);
    }

    public static Pagination of(int page, int size, String... sorts) {
        return new Pagination(page, size, sorts);
    }

    public static Pagination of(int page, int size, Sort sort) {
        return new Pagination(page, size, sort.getSorts());
    }

    public static Pagination of(int page, int size, String[] sorts, long total) {
        return new Pagination(page, size, sorts, total);
    }

    public static Pagination of(int page, int size, long total) {
        return new Pagination(page, size, null, total);
    }

    public static Pagination of(int page, int size, Sort sort, long total) {
        return new Pagination(page, size, sort.getSorts(), total);
    }

    public void setSorts(String... sorts) {
        this.sorts = sorts;
    }

    public void setSorts(Sort sort) {
        if (sort != null) {
            this.sorts = sort.getSorts();
        }
    }

    public Pagination next() {
        return Pagination.of(page + 1, size, sorts, total);
    }
}
