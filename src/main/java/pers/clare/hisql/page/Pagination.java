package pers.clare.hisql.page;

@SuppressWarnings("unused")
public class Pagination {
    private int page;
    private int size;
    private String[] sorts;

    public Pagination() {
    }

    public Pagination(int page, int size, String[] sorts) {
        this.page = page;
        this.size = size;
        this.sorts = sorts;
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

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public String[] getSorts() {
        return sorts;
    }

    public void setSorts(String[] sorts) {
        this.sorts = sorts;
    }

    public void setSorts(Sort sort) {
        if (sort != null) {
            this.sorts = sort.getSorts();
        }
    }

    public Pagination next() {
        return Pagination.of(page + 1, size, sorts);
    }
}
