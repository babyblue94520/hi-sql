package pers.clare.hisql.page;

import pers.clare.hisql.exception.HiSqlException;
import pers.clare.hisql.util.ConnectionUtil;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

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

    @Override
    public long getVirtualTotal(
            Pagination pagination
            , Connection connection
            , String sql
            , Object[] parameters
    ) throws SQLException {
        String totalSql = "explain " + sql;
        ResultSet rs = ConnectionUtil.query(connection, totalSql, parameters);
        if (rs.next()) {
            return rs.getLong("rows");
        } else {
            throw new HiSqlException(String.format("query total error.(%s)", totalSql));
        }
    }

}
