package pers.clare.hisql.page;


import lombok.Getter;

import java.util.Collections;
import java.util.List;

@SuppressWarnings("unused")
@Getter
public class Page<T> {
    private final int page;
    private final int size;
    private final List<T> records;
    private final long total;

    public Page(int page, int size, List<T> records, long total) {
        this.page = page;
        this.size = size;
        this.records = records;
        this.total = total;
    }

    public static <T> Page<T> of(int page, int size, List<T> records, long total) {
        return new Page<>(page, size, records, total);
    }

    public static <T> Page<T> empty() {
        return new Page<>(0, 20, Collections.emptyList(), 0);
    }

    public static <T> Page<T> empty(Pagination pagination) {
        return new Page<>(pagination.getPage(), pagination.getSize(), Collections.emptyList(), 0);
    }
}
