package pers.clare.hisql.page;

import java.util.Collections;
import java.util.List;


@SuppressWarnings("unused")
public class Next<T> {
    private final int page;
    private final int size;
    private final List<T> records;

    public Next(int page, int size, List<T> records) {
        this.page = page;
        this.size = size;
        this.records = records;
    }

    public static <T> Next<T> of(int page, int size, List<T> records) {
        return new Next<>(page, size, records);
    }

    public static <T> Next<T> empty(Pagination pagination) {
        return new Next<>(pagination.getPage(), pagination.getSize(), Collections.emptyList());
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
