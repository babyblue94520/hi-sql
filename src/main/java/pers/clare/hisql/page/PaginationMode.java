package pers.clare.hisql.page;

import pers.clare.hisql.exception.HiSqlException;
import pers.clare.hisql.util.ConnectionUtil;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

public interface PaginationMode {

    default String buildTotalSQL(String sql) {
        return "select count(*) from(" + sql + ")t";
    }

    default String buildSortSQL(
            Sort sort
            , String sql
    ) {
        if (sort == null || sort.getSorts().length == 0) return sql;
        StringBuilder sb = new StringBuilder(sql);
        appendSortSQL(sb, sort.getSorts());
        return sb.toString();
    }

    default String buildPaginationSQL(
            Pagination pagination
            , String sql
    ) {
        if (pagination == null) return sql;
        StringBuilder sb = new StringBuilder(sql);
        appendPaginationSQL(sb, pagination);
        return sb.toString();
    }

    void appendPaginationSQL(StringBuilder sql, Pagination pagination);

    default void appendSortSQL(StringBuilder sql, String[] sorts) {
        if (sorts == null || sorts.length == 0) return;
        sql.append(" order by ");
        for (String sort : sorts) {
            if (sort == null || sort.length() == 0) continue;
            if (Character.isUpperCase(sort.charAt(0))) {
                sql.append(sort);
            } else {
                sortTurnCamelCase(sql, sort);
            }
            sql.append(',');
        }
        sql.delete(sql.length() - 1, sql.length());
    }


    default void sortTurnCamelCase(StringBuilder sb, String name) {
        int l = name.length();
        char[] cs = name.toCharArray();
        // 避開開頭空白或者換行
        int start = 0;
        for (char c : cs) {
            if (c != ' ' && c != '\n') break;
            start++;
        }
        char c = cs[start++];
        sb.append(toLowerCase(c));
        boolean turn = true;
        for (int i = start; i < l; i++) {
            c = cs[i];
            if (c == ' ') turn = false; // stop when blank
            if (turn && Character.isUpperCase(c)) {
                c = toLowerCase(c);
                sb.append('_');
            }
            sb.append(c);
        }
    }

    default char toLowerCase(char c) {
        return Character.toLowerCase(check(c));
    }


    default char check(char c) {
        if (c == ';') {
            throw new HiSqlException("Not a legal character ';'");
        }
        return c;
    }

    default long getTotal(
            Pagination pagination
            , Connection connection
            , String sql
            , Object[] parameters
    ) throws SQLException {
        String totalSql = buildTotalSQL(sql);
        ResultSet rs = ConnectionUtil.query(connection, totalSql, parameters);
        if (rs.next()) {
            return rs.getLong(1);
        } else {
            throw new HiSqlException(String.format("query total error.(%s)", totalSql));
        }
    }

    default long getVirtualTotal(
            Pagination pagination
            , Connection connection
            , String sql
            , Object[] parameters
    ) throws SQLException {
        return getTotal(pagination, connection, sql, parameters);
    }
}
