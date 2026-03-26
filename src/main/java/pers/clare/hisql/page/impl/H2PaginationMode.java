package pers.clare.hisql.page.impl;

import pers.clare.hisql.exception.HiSqlException;
import pers.clare.hisql.page.Pagination;
import pers.clare.hisql.page.PaginationMode;
import pers.clare.hisql.service.SQLTypeService;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("unused")
public class H2PaginationMode implements PaginationMode {
    private final Pattern scanCountPattern = Pattern.compile("scanCount: (\\d+)");

    public void appendPaginationSQL(
            StringBuilder sql
            , Pagination pagination
    ) {
        appendSortSQL(sql, pagination.getSorts());
        int start = pagination.isCursor() ? 0 : pagination.getSize() * pagination.getPage();
        sql.append(" LIMIT ")
                .append(start)
                .append(',')
                .append(pagination.getSize());
    }

    @Override
    public long getVirtualTotal(
            SQLTypeService service
            , String sql
            , Object[] parameters
    ) {
        String virtualTotalSql = "EXPLAIN ANALYZE " + sql;
        String result = service.find(String.class, virtualTotalSql, parameters);
        Matcher matcher = scanCountPattern.matcher(result);
        if (matcher.find()) {
            String countString = matcher.group(1);
            return Long.parseLong(countString);
        }
        throw new HiSqlException(String.format("Query virtual total error.(%s)", virtualTotalSql));
    }
}
