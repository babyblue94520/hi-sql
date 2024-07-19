package pers.clare.hisql.query;

import org.springframework.lang.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SQLQueryBuilder {

    private final char[][] sqlParts;

    private final Map<String, List<Integer>> keyIndex;

    SQLQueryBuilder(char[] sql, int count) {
        keyIndex = new HashMap<>();
        sqlParts = new char[count + count + 1][];
        char c, p = 0;
        int l = sql.length, partCount = 0, tempLength = 0;
        char[] temp = new char[l];
        boolean b;
        for (int i = 0; i < l; i++) {
            c = sql[i];
            if (c == ':' && sql[i + 1] != '=') {
                sqlParts[partCount] = new char[tempLength];
                System.arraycopy(temp, 0, sqlParts[partCount++], 0, tempLength);
                sqlParts[partCount] = null;
                tempLength = 0;
                i++;
                b = false;
                for (; i < l; i++) {
                    c = sql[i];
                    switch (c) {
                        case ' ':
                        case ',':
                        case ')':
                        case '\n':
                            put(keyIndex, new String(temp, 0, tempLength), partCount++);
                            tempLength = 0;
                            temp[tempLength++] = c;
                            b = true;
                            break;
                        default:
                            temp[tempLength++] = c;
                    }
                    if (b) break;
                }
                if (!b) {
                    put(keyIndex, new String(temp, 0, tempLength), partCount++);
                    tempLength = 0;
                }
                continue;
            } else if (c == ' ' && p == ' ') {
                continue;
            }
            temp[tempLength++] = p = c;
        }
        if (tempLength > 0) {
            sqlParts[partCount] = new char[tempLength];
            System.arraycopy(temp, 0, sqlParts[partCount], 0, tempLength);
        } else {
            sqlParts[partCount] = null;
        }
    }

    public static SQLQueryBuilder create(@NonNull String sql) {
        return create(sql.toCharArray());
    }

    public static SQLQueryBuilder create(@NonNull char[] cs) {
        int count = getKeyCount(cs);
        return new SQLQueryBuilder(cs, count);
    }

    public static boolean hasKey(char[] cs) {
        for (int i = 0, l = cs.length; i < l; i++) {
            if (cs[i] == ':' && cs[++i] != '=') return true;
        }
        return false;
    }

    private static int getKeyCount(char[] cs) {
        int count = 0;
        for (int i = 0, l = cs.length; i < l; i++) {
            if (cs[i] == ':' && cs[++i] != '=') count++;
        }
        return count;
    }

    private static void put(Map<String, List<Integer>> keyIndex, String key, Integer index) {
        keyIndex.computeIfAbsent(key, k -> new ArrayList<>()).add(index);
    }

    public SQLQuery build() {
        return new SQLQuery(sqlParts, keyIndex);
    }

}
