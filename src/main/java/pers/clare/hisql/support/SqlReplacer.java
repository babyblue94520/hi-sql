package pers.clare.hisql.support;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class SqlReplacer {

    private static final SqlReplaceCondition<Object> DEFAULT_CONDITION = v -> {
        if (v == null) return false;
        if (v instanceof String) {
            return !((String) v).isEmpty();
        } else if (Collection.class.isAssignableFrom(v.getClass())) {
            return !((Collection<?>) v).isEmpty();
        } else if (v.getClass().isArray()) {
            return Array.getLength(v) != 0;
        }
        return true;
    };

    private final Map<String, Rule> map = new HashMap<>();

    public static SqlReplacer create() {
        return new SqlReplacer();
    }

    public SqlReplacer add(String name, String sql) {
        return add(name, name, sql);
    }

    public SqlReplacer add(String replaceName, String valueName, String sql) {
        map.put(replaceName, new Rule(replaceName, valueName, sql, DEFAULT_CONDITION));
        return this;
    }

    public <T> SqlReplacer add(String name, String sql, SqlReplaceCondition<T> condition) {
        return add(name, name, sql, condition);
    }

    @SuppressWarnings("unchecked")
    public <T> SqlReplacer add(String replaceName, String valueName, String sql, SqlReplaceCondition<T> condition) {
        map.put(replaceName, new Rule(replaceName, valueName, sql, (SqlReplaceCondition<Object>) condition));
        return this;
    }

    public Collection<Rule> getRules() {
        return map.values();
    }

    @AllArgsConstructor
    public static class Rule {
        @Getter
        private final String replaceName;
        @Getter
        private final String valueName;

        private final String sql;
        private final SqlReplaceCondition<Object> condition;

        public String getSql(Object value) {
            if (condition.match(value)) {
                return sql;
            } else {
                return "";
            }
        }
    }
}
