package pers.clare.hisql.query;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * 解析SQL需要被替換的資料，建造不需要重新解析SQL的SQL替換工廠
 */
public class SQLQueryReplaceBuilder {
    private static final char startFlag = '{';
    private static final char endFlag = '}';

    // SQL 字串切割陣列
    private final char[][] sqlParts;

    // 需要被替換成SQL的key陣列
    private final Map<String, Integer> keyIndex;

    public SQLQueryReplaceBuilder(String sql) {
        this(sql.toCharArray());
    }

    public SQLQueryReplaceBuilder(char[] sqlChars) {
        int count = findKeyCount(sqlChars);
        keyIndex = new HashMap<>(count);
        sqlParts = new char[count + count + 1][];
        char c;
        int l = sqlChars.length, partCount = 0, tempLength = 0, keyLength;
        char[] temp = new char[l];
        char[] key = new char[l];
        for (int i = 0; i < l; i++) {
            c = sqlChars[i];
            if (c == startFlag) {
                sqlParts[partCount] = new char[tempLength];
                System.arraycopy(temp, 0, sqlParts[partCount++], 0, tempLength);
                sqlParts[partCount] = null;
                tempLength = 0;
                keyLength = 0;
                i++;
                for (; i < l; i++) {
                    c = sqlChars[i];
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

    public SQLQueryReplace build() {
        return new SQLQueryReplace(sqlParts, keyIndex);
    }

    public int getKeySize(){
        return keyIndex.size();
    }

    public boolean hasKey(String key){
        return keyIndex.containsKey(key);
    }

    public static int findKeyCount(char[] sqlChars) {
        int count = 0;
        for (char sqlChar : sqlChars) {
            if (sqlChar == startFlag) {
                count++;
            }
        }
        return count;
    }

    public static void main(String[] args) {
        System.out.println(
                new SQLQueryReplaceBuilder("select * from user where {id} {name} " +
                        " and age in :age" +
                        " and bb in :bb" +
                        " and cc in :cc"
                ).build()
                        .replace("id", "id=:id")
                        .replace("name", "and name like :name")
                        .buildQuery()
                        .value("id", 1)
                        .value("name", "tes%")
                        .value("age", 1)
                        .values("bb", new int[]{1, 2}, new int[]{1, 2})
                        .value("cc", Arrays.asList(new int[]{1, 2}, new int[]{1, 2})).toString()
        );
    }
}
