package pers.clare.hisql.page.impl;

import pers.clare.hisql.exception.HiSqlException;
import pers.clare.hisql.page.Pagination;
import pers.clare.hisql.page.PaginationMode;
import pers.clare.hisql.service.SQLTypeService;

import java.math.BigInteger;
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
        BigInteger rows = (BigInteger) result.get("rows");
        if (rows == null) {
            throw new HiSqlException(String.format("Query virtual total error.(%s)", virtualTotalSql));
        }
        return rows.longValue();
    }

}
