package pers.clare.hisql.page;

@SuppressWarnings("unused")
public class Sort {
    private String[] sorts;

    public static Sort of(String... sorts) {
        return new Sort(sorts);
    }

    public Sort() {
    }

    public Sort(String[] sorts) {
        this.sorts = sorts;
    }

    public String[] getSorts() {
        return sorts;
    }

    public void setSorts(String[] sorts) {
        this.sorts = sorts;
    }
}
