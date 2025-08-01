package pers.clare.hisql.page;

import pers.clare.hisql.exception.HiSqlException;
import pers.clare.hisql.service.SQLTypeService;

import java.util.Map;

@SuppressWarnings("unused")
public class MySQLPaginationMode implements PaginationMode {
    public void appendPaginationSQL(
            StringBuilder sql
            , Pagination pagination
    ) {
        appendSortSQL(sql, pagination.getSorts());
        sql.append(" LIMIT ")
                .append(pagination.getSize() * pagination.getPage())
                .append(',')
                .append(pagination.getSize());
    }

    @Override
    public long getVirtualTotal(
            SQLTypeService service
            , String sql
            , Object[] parameters
    ) {
        String virtualTotalSql = "EXPLAIN " + sql;
        Map<String, Object> result = service.findMap(Object.class, virtualTotalSql, parameters);
        Long total = (Long) result.get("ROWS");
        if (total == null) {
            throw new HiSqlException(String.format("Query virtual total error.(%s)", virtualTotalSql));
        }
        return total;
    }

}
