package pers.clare.hisql.page.impl;

import pers.clare.hisql.page.Pagination;
import pers.clare.hisql.page.PaginationMode;

@SuppressWarnings("unused")
public class MSSQLPaginationMode implements PaginationMode {
    public void appendPaginationSQL(
            StringBuilder sql
            , Pagination pagination
    ) {
        appendSortSQL(sql, pagination.getSorts());
        sql.append(" OFFSET ")
                .append(pagination.getSize() * pagination.getPage())
                .append(" ROWS FETCH NEXT ")
                .append(pagination.getSize())
                .append(" ROWS ONLY");
    }
}
