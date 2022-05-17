package pers.clare.hisql.query;

import java.util.Map;

/**
 * SQL動態替換SQL工廠，是執行緒不安全的class，所以多執行續環境下，必須透過SQLQueryReplaceBuilder重新建造.
 */
public class SQLQueryReplace {
    private final char[][] sqlParts;

    private final Map<String, Integer> keyIndex;

    private final String[] values;


    public SQLQueryReplace(char[][] sqlParts, Map<String, Integer> keyIndex) {
        this.sqlParts = sqlParts;
        this.keyIndex = keyIndex;
        this.values = new String[sqlParts.length];
    }

    public SQLQueryReplace replace(String key, String sql) {
        if (key == null || sql == null || sql.length() == 0) return this;
        Integer index = keyIndex.get(key);
        if (index == null) return this;
        values[index] = sql;
        return this;
    }

    public SQLQueryBuilder buildQueryBuilder() {
        return new SQLQueryBuilder(toString());
    }

    public SQLQuery buildQuery() {
        return buildQueryBuilder().build();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        char[] cs;
        String str;
        for (int i = 0, l = sqlParts.length; i < l; i++) {
            cs = sqlParts[i];
            if (cs == null) {
                str = values[i];
                if (str == null) continue;
                sb.append(str);
            } else {
                sb.append(cs);
            }
        }
        return sb.toString();
    }
}
