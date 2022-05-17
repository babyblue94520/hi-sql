package pers.clare.hisql.page;

@SuppressWarnings("unused")
public class MySQLPaginationMode implements PaginationMode {
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
