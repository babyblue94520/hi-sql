package pers.clare.hisql.page;

@SuppressWarnings("unused")
public class MSSQLPaginationMode implements PaginationMode {
    public void appendPaginationSQL(
            StringBuilder sql
            , Pagination pagination
    ) {
        appendSortSQL(sql, pagination.getSorts());
        sql.append(" OFFSET ")
                .append(pagination.getSize() * pagination.getPage())
                .append(" FETCH NEXT ")
                .append(pagination.getSize())
                .append(" ROWS ONLY");
    }
}
