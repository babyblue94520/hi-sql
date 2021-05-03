package pers.clare.hisql.page;

import pers.clare.hisql.exception.HiSqlException;

public class MSSQLPaginationMode implements PaginationMode {

    public String buildTotalSQL(String sql) {
        return "select count(*) from(" + sql + ")t";
    }

    public String buildSortSQL(
            Sort sort
            , String sql
    ) {
        StringBuilder sb = new StringBuilder(sql);
        appendSortSQL(sb, sort.getSorts());
        return sb.toString();
    }

    public String buildPaginationSQL(
            Pagination pagination
            , String sql
    ) {
        StringBuilder sb = new StringBuilder(sql);
        appendPaginationSQL(sb, pagination);
        return sb.toString();
    }

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

    public void appendSortSQL(StringBuilder sql, String[] sorts) {
        if (sorts == null) return;
        sql.append(" order by ");
        for (String sort : sorts) {
            if (sort == null || sort.length() == 0) continue;
            sortTurnCamelCase(sql, sort);
            sql.append(',');
        }
        sql.delete(sql.length() - 1, sql.length());
    }

    private StringBuilder sortTurnCamelCase(StringBuilder sb, String name) {
        int l = name.length();
        char[] cs = name.toCharArray();
        // 避開開頭空白或者換行
        int start = 0;
        for (char c : cs) {
            if (c != ' ' || c != '\n') break;
            start++;
        }
        char c = cs[start++];
        sb.append(toLowerCase(c));
        boolean turn = true;
        for (int i = start; i < l; i++) {
            c = cs[i];
            if (c == ' ') turn = false; // stop when blank
            if (turn && c > 64 && c < 91) {
                c = toLowerCase(c);
                sb.append('_');
            }
            sb.append(c);
        }
        return sb;
    }

    private static char toLowerCase(char c) {
        return Character.toLowerCase(check(c));
    }

    private static char check(char c) {
        switch (c) {
            case ';':
                throw new HiSqlException("Not a legal character ';'");
        }
        return c;
    }
}
