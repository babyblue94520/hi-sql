package pers.clare.hisql.page;

import pers.clare.hisql.exception.HiSqlException;
import pers.clare.hisql.util.ConnectionUtil;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
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
        sql.append(" limit ")
                .append(pagination.getSize() * pagination.getPage())
                .append(',')
                .append(pagination.getSize());
    }

    @Override
    public long getVirtualTotal(
            Connection connection
            , String sql
            , Object[] parameters
    ) throws SQLException {
        ResultSet rs = ConnectionUtil.query(connection, "explain analyze " + sql, parameters);
        if (rs.next()) {
            String plan = rs.getString(1);
            Matcher matcher = scanCountPattern.matcher(plan);
            if (matcher.find()) {
                String countString = matcher.group(1);
                return Long.parseLong(countString);
            }
        }
        throw new HiSqlException(String.format("query total error.(%s)", sql));
    }
}
