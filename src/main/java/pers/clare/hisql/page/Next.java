package pers.clare.hisql.page;

import java.util.List;

/**
 * 不查詢 total
 */
public class Next<T> {
    private final int page;
    private final int size;
    private final List<T> records;

    public static <T> Next<T> of(int page, int size, List<T> records) {
        return new Next<>(page, size, records);
    }

    public Next(int page, int size, List<T> records) {
        this.page = page;
        this.size = size;
        this.records = records;
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

}
