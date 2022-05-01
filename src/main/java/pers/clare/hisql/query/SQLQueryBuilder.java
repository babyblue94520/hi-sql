package pers.clare.hisql.query;

import java.util.*;

public class SQLQueryBuilder {

    // SQL 字串切割陣列
    private final char[][] sqlParts;

    // 需要被替換成SQL的key陣列
    private final Map<String, List<Integer>> keyIndex;

    public SQLQueryBuilder(String sql) {
        this(sql.toCharArray());
    }

    public SQLQueryBuilder(char[] sqlChars) {
        int count = findKeyCount(sqlChars);
        keyIndex = new HashMap<>(count);
        sqlParts = new char[count + count + 1][];
        char c, p = 0;
        int l = sqlChars.length, listCount = 0, tempLength = 0;
        char[] temp = new char[l];
        boolean b;
        for (int i = 0; i < l; i++) {
            c = sqlChars[i];
            switch (c) {
                case ':':
                    if (sqlChars[i + 1] != '=') {
                        sqlParts[listCount] = new char[tempLength];
                        System.arraycopy(temp, 0, sqlParts[listCount++], 0, tempLength);
                        sqlParts[listCount] = null;
                        tempLength = 0;
                        i++;
                        b = false;
                        for (; i < l; i++) {
                            c = sqlChars[i];
                            switch (c) {
                                case ' ':
                                case ',':
                                case ')':
                                    put(keyIndex, new String(temp, 0, tempLength), listCount++);
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
                            put(keyIndex, new String(temp, 0, tempLength), listCount++);
                            tempLength = 0;
                        }
                        break;
                    }
                case ' ':
                    if (p == ' ') break;
                default:
                    temp[tempLength++] = p = c;
            }
        }
        if (tempLength > 0) {
            sqlParts[listCount] = new char[tempLength];
            System.arraycopy(temp, 0, sqlParts[listCount], 0, tempLength);
        } else {
            sqlParts[listCount] = null;
        }
    }

    public SQLQuery build() {
        return new SQLQuery(sqlParts, keyIndex);
    }

    public static int findKeyCount(char[] sqlChars) {
        int count = 0;
        for (int i = 0, l = sqlChars.length; i < l; i++) {
            if (sqlChars[i] == ':' && sqlChars[i + 1] != '=') {
                count++;
            }
        }
        return count;
    }

    private static void put(Map<String, List<Integer>> keyIndex, String key, Integer index) {
        keyIndex.computeIfAbsent(key, k -> new ArrayList<>()).add(index);
    }

    public static void main(String[] args) {
        System.out.println(
                new SQLQueryBuilder("select * from user where id=:id and id=:test and name like :name" +
                        " and age in :age" +
                        " and bb in :bb" +
                        " and cc in :cc"
                )
                        .build()
                        .value("id", 1)
                        .value("name", "tes%")
                        .value("test", "' or ''='")
                        .value("age", new int[]{1})
                        .values("bb", new int[]{1, 2}, new int[]{1, 2})
                        .value("cc", Arrays.asList(new int[]{1, 2}, new int[]{1, 2}))
        );
    }

}
