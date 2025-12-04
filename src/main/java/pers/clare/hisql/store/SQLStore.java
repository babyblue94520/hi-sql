package pers.clare.hisql.store;


import lombok.Getter;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

@Getter
public class SQLStore<T> {
    private static final Pattern backtickPattern = Pattern.compile("`");

    protected final Constructor<T> constructor;
    protected final SQLStoreColumn[] columns;
    protected final Map<String, SQLStoreColumn> nameMapping;

    public SQLStore(Constructor<T> constructor, SQLStoreColumn[] columns
    ) {
        this.constructor = constructor;
        this.columns = columns;
        this.nameMapping = new HashMap<>();
        for (SQLStoreColumn column : columns) {
            String name = column.getName();
            nameMapping.put(name, column);
            nameMapping.put(backtickPattern.matcher(name).replaceAll(""), column);
            nameMapping.put(column.getName(), column);
            nameMapping.put(name.toUpperCase(), column);
            nameMapping.put(name.toLowerCase(), column);
        }
    }

}
