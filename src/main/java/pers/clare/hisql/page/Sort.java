package pers.clare.hisql.page;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@SuppressWarnings("unused")
public class Sort {
    public static final String[] EMPTY = new String[]{};

    private String[] sorts = EMPTY;

    public Sort() {
    }

    public Sort(String... sorts) {
        this.sorts = sorts;
    }

    public static Sort of(String... sorts) {
        return new Sort(sorts);
    }

}
