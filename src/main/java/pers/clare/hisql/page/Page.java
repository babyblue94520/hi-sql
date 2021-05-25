package pers.clare.hisql.page;


import java.util.Collections;
import java.util.List;

@SuppressWarnings("unused")
public class Page<T> {
    private final int page;
    private final int size;
    private final List<T> records;
    private final long total;

    public static <T> Page<T> of(int page, int size, List<T> records, long total) {
        return new Page<>(page, size, records, total);
    }

    public static <T> Page<T> empty(Pagination pagination) {
        return new Page<>(pagination.getPage(), pagination.getSize(), Collections.emptyList(), 0);
    }

    public Page(int page, int size, List<T> records, long total) {
        this.page = page;
        this.size = size;
        this.records = records;
        this.total = total;
    }

    public int getPage() {
        return page;
    }

    public int getSize() {
        return size;
    }

    public List<T> getRecords() {
        return records;
    }

    public long getTotal() {
        return total;
    }
}
