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
        int start = pagination.isCursor() ? 0 : pagination.getSize() * pagination.getPage();
        sql.append(" OFFSET ")
                .append(start)
                .append(" ROWS FETCH NEXT ")
                .append(pagination.getSize())
                .append(" ROWS ONLY");
    }
}
