package pers.clare.hisql.page;

@SuppressWarnings("unused")
public class MySQLPaginationMode implements PaginationMode {

    public String buildTotalSQL(String sql) {
        return "select count(*) from(" + sql + ")t";
    }

    public String buildSortSQL(
            Sort sort
            , String sql
    ) {
        StringBuilder sb = new StringBuilder(sql);
        appendSortSQL(sb, sort.getSorts());
        return sb.toString();
    }

    public String buildPaginationSQL(
            Pagination pagination
            , String sql
    ) {
        StringBuilder sb = new StringBuilder(sql);
        appendPaginationSQL(sb, pagination);
        return sb.toString();
    }

    public void appendPaginationSQL(
            StringBuilder sql
            , Pagination pagination
    ) {
        appendSortSQL(sql, pagination.getSorts());
        sql.append(" limit ")
                .append(pagination.getSize() * pagination.getPage())
                .append(',')
                .append(pagination.getSize());
    }
}
