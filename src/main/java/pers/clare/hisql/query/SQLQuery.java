package pers.clare.hisql.query;

import pers.clare.hisql.constant.KeyCache;
import pers.clare.hisql.util.SQLQueryUtil;

import java.util.List;
import java.util.Map;

/**
 * 建造動態產生PreparedStatement，主要是因為Mysql不支持 setArray，
 * 當條件中有in時，則無法透過PreparedStatement優化。
 * 是執行緒不安全的class，所以多執行續環境下，必須透過SQLQueryBuilder重新建造
 */
public class SQLQuery {
    final Object[] values;
    private final char[][] sqlParts;
    private final Map<String, List<Integer>> keyIndexes;

    SQLQuery(char[][] sqlParts, Map<String, List<Integer>> keyIndexes) {
        this.sqlParts = sqlParts;
        this.keyIndexes = keyIndexes;
        this.values = new Object[sqlParts.length];
    }

    public SQLQuery value(String key, Object value) {
        key = KeyCache.get(key);
        if (key == null) return this;
        List<Integer> list = keyIndexes.get(key);
        if (list == null || list.isEmpty()) return this;
        if (value == null) {
            value = SQLQueryUtil.NULL;
        }
        for (Integer index : list) {
            values[index] = value;
        }
        return this;
    }

    @Override
    public String toString() {
        return toSQL().toString();
    }

    public StringBuilder toSQL() {
        StringBuilder sb = new StringBuilder();
        char[] cs;
        for (int i = 0, l = sqlParts.length; i < l; i++) {
            cs = sqlParts[i];
            if (cs.length == 0) {
                Object value = values[i];
                if (value == null) continue;
                SQLQueryUtil.append(sb, value);
            } else {
                sb.append(cs, 0, cs.length);
            }
        }
        return sb;
    }
}
