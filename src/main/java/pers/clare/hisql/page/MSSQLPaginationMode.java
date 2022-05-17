package pers.clare.hisql.page;

@SuppressWarnings("unused")
public class MSSQLPaginationMode implements PaginationMode {
    public void appendPaginationSQL(
            StringBuilder sql
            , Pagination pagination
    ) {
        appendSortSQL(sql, pagination.getSorts());
        sql.append(" offset ")
                .append(pagination.getSize() * pagination.getPage())
                .append(" fetch next ")
                .append(pagination.getSize())
                .append(" rows only");
    }
}
