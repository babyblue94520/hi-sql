package pers.clare.hisql.page;

import pers.clare.hisql.exception.HiSqlException;

public interface PaginationMode {

    String buildTotalSQL(String sql);

    String buildSortSQL(Sort sort, String sql);

    String buildPaginationSQL(Pagination pagination, String sql);

    void appendPaginationSQL(StringBuilder sql, Pagination pagination);


    default void appendSortSQL(StringBuilder sql, String[] sorts) {
        if (sorts == null) return;
        sql.append(" order by ");
        for (String sort : sorts) {
            if (sort == null || sort.length() == 0) continue;
            sortTurnCamelCase(sql, sort);
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
            if (turn && c > 64 && c < 91) {
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
}
