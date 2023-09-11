package pers.clare.hisql.query;

import org.springframework.lang.NonNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Parse replace sql
 */
public class SQLQueryReplaceBuilder {
    private static final char startFlag = '{';
    private static final char endFlag = '}';

    private final char[][] sqlParts;

    private final Map<String, Integer> keyIndex;

    SQLQueryReplaceBuilder(char[] sql, int keyCount) {
        keyIndex = new HashMap<>(keyCount);
        sqlParts = new char[keyCount + keyCount + 1][];
        char c;
        int l = sql.length, partCount = 0, tempLength = 0, keyLength;
        char[] temp = new char[l];
        char[] key = new char[l];
        for (int i = 0; i < l; i++) {
            c = sql[i];
            if (c == startFlag) {
                sqlParts[partCount] = new char[tempLength];
                System.arraycopy(temp, 0, sqlParts[partCount++], 0, tempLength);
                sqlParts[partCount] = null;
                tempLength = 0;
                keyLength = 0;
                i++;
                for (; i < l; i++) {
                    c = sql[i];
                    if (c == endFlag) {
                        keyIndex.put(new String(key, 0, keyLength), partCount++);
                        keyLength = 0;
                        break;
                    }
                    key[keyLength++] = c;
                }
                if (keyLength > 0) {
                    sqlParts[partCount] = new char[keyLength];
                    System.arraycopy(key, 0, sqlParts[partCount++], 0, keyLength);
                }
            } else {
                temp[tempLength++] = c;
            }
        }
        if (tempLength > 0) {
            sqlParts[partCount] = new char[tempLength];
            System.arraycopy(temp, 0, sqlParts[partCount], 0, tempLength);
        } else {
            sqlParts[partCount] = null;
        }
    }

    public static SQLQueryReplaceBuilder create(@NonNull String sql) {
        return create(sql.toCharArray());
    }

    public static SQLQueryReplaceBuilder create(@NonNull char[] cs) {
        int count = getKeyCount(cs);
        return new SQLQueryReplaceBuilder(cs, count);
    }

    public static boolean hasKey(char[] cs) {
        for (char c : cs) if (c == startFlag) return true;
        return false;
    }

    private static int getKeyCount(char[] cs) {
        int count = 0;
        for (char c : cs) if (c == startFlag) count++;
        return count;
    }

    public SQLQueryReplace build() {
        return new SQLQueryReplace(sqlParts, keyIndex);
    }

    public Set<String> getKeys() {
        return keyIndex.keySet();
    }
}
