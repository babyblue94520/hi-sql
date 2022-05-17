package pers.clare.hisql.page;

@SuppressWarnings("unused")
public class Sort {
    public static final String[] EMPTY = new String[]{};

    private String[] sorts = EMPTY;

    public Sort() {
    }

    public Sort(String[] sorts) {
        this.sorts = sorts;
    }

    public static Sort of(String... sorts) {
        return new Sort(sorts);
    }

    public String[] getSorts() {
        return sorts;
    }

    public void setSorts(String[] sorts) {
        this.sorts = sorts;
    }
}
