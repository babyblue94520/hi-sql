package pers.clare.hisql.query;

import org.springframework.lang.NonNull;
import pers.clare.hisql.constant.KeyCache;

import java.util.*;

/**
 * Parse replace sql
 */
public class SQLQueryReplaceBuilder {
    private static final char START_FLAG = '{';
    private static final char END_FLAG = '}';

    public static SQLQueryReplaceBuilder create(@NonNull String sql) {
        return create(sql.toCharArray());
    }

    public static SQLQueryReplaceBuilder create(@NonNull char[] sql) {
        Map<String, List<Integer>> keyIndexes = new HashMap<>();
        List<int[]> partRanges = new ArrayList<>();

        int l = sql.length;
        int partStart = 0;
        for (int i = 0; i < l; i++) {
            char c = sql[i];
            if (c == START_FLAG) {
                if (i > partStart) {
                    partRanges.add(new int[]{partStart, i});
                }
                partStart = i + 1;
                while (++i < l) {
                    c = sql[i];
                    if (c == START_FLAG) {
                        if (i > partStart) {
                            partRanges.add(new int[]{partStart - 1, i});
                            partStart = i + 1;
                        }
                    } else if (c == END_FLAG) {
                        int keyLen = i - partStart;
                        if (keyLen > 0) {
                            String key = KeyCache.computeIfAbsent(new String(sql, partStart, keyLen));
                            keyIndexes.computeIfAbsent(key, k -> new ArrayList<>())
                                    .add(partRanges.size());
                            partRanges.add(null);
                        }
                        partStart = i + 1;
                        break;
                    }
                }
            }
        }
        if (l > partStart) {
            partRanges.add(new int[]{partStart, l});
        }
        var parts = new char[partRanges.size()][];
        for (int j = 0; j < partRanges.size(); j++) {
            int[] range = partRanges.get(j);
            if (range == null) {
                parts[j] = null;
            } else {
                parts[j] = Arrays.copyOfRange(sql, range[0], range[1]);
            }
        }
        return new SQLQueryReplaceBuilder(parts, keyIndexes);
    }

    private final char[][] sqlParts;

    private final Map<String, List<Integer>> keyIndexes;

    SQLQueryReplaceBuilder(char[][] sqlParts, Map<String, List<Integer>> keyIndexes) {
        this.sqlParts = sqlParts;
        this.keyIndexes = keyIndexes;
    }

    public boolean hasKey() {
        return keyIndexes != null && !keyIndexes.isEmpty();
    }

    public SQLQueryReplace build() {
        return new SQLQueryReplace(sqlParts, keyIndexes);
    }

    public Set<String> getKeys() {
        return keyIndexes.keySet();
    }

}
