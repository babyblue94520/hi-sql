package pers.clare.hisql.query;

import pers.clare.hisql.constant.KeyCache;

import java.util.List;
import java.util.Map;

/**
 * SQL動態替換SQL工廠，是執行緒不安全的class，所以多執行續環境下，必須透過SQLQueryReplaceBuilder重新建造.
 */
public class SQLQueryReplace {
    private final char[][] sqlParts;

    private final Map<String, List<Integer>> keyIndexes;

    private final String[] values;


    public SQLQueryReplace(char[][] sqlParts, Map<String, List<Integer>> keyIndexes) {
        this.sqlParts = sqlParts;
        this.keyIndexes = keyIndexes;
        this.values = new String[sqlParts.length];
    }

    public SQLQueryReplace replace(String key, String sql) {
        key = KeyCache.get(key);
        if (key == null || sql == null || sql.isEmpty()) return this;
        List<Integer> indexes = keyIndexes.get(key);
        if (indexes == null) return this;
        for (Integer index : indexes) {
            values[index] = sql;
        }
        return this;
    }

    public SQLQueryBuilder buildQueryBuilder() {
        return SQLQueryBuilder.create(toString());
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
                sb.append(cs, 0, cs.length);
            }
        }
        return sb.toString();
    }
}
