package pers.clare.hisql.util;

import pers.clare.hisql.exception.HiSqlException;
import pers.clare.hisql.naming.NamingStrategy;
import pers.clare.hisql.page.Next;
import pers.clare.hisql.page.Page;
import pers.clare.hisql.store.FieldColumn;

import java.lang.reflect.Type;
import java.util.*;

public class CommandUtil {
    private static final Set<Class<?>> parameterizedTypes = new HashSet<>();

    static {

        parameterizedTypes.add(Optional.class);
        parameterizedTypes.add(Page.class);
        parameterizedTypes.add(Next.class);
    }

    public static String clearCommand(String command) {
        char[] cs = command.toCharArray();
        char c;
        int count = 0;
        char[] temp = new char[cs.length];
        boolean pause = false;
        boolean space = false;
        for (int i = 0; i < cs.length; i++) {
            c = cs[i];
            switch (c) {
                case '\t':
                case '\n':
                case '\r':
                case ' ':
                    if (!pause) {
                        space = count > 0;
                        break;
                    }
                default:
                    if (space) {
                        temp[count++] = ' ';
                        space = false;
                    }
                    temp[count++] = c;
                    if (c == '\'') {
                        pause = !pause;
                    } else if (c == '\\' && cs[i + 1] == '\'') {
                        temp[count++] = '\'';
                        i++;
                    }
            }
        }
        return new String(temp, 0, count);
    }

    public static String appendSelectColumns(NamingStrategy naming, Type returnType, String command) {
        Class<?> returnClass = ClassUtil.toClassType(returnType);
        if (
                parameterizedTypes.contains(returnClass)
                || returnClass.isArray()
                || Collection.class.isAssignableFrom(returnClass)
        ) {
            returnClass = ClassUtil.getValueClass(returnType, 0);
        }
        if (returnClass == Map.class) {
            return "select * " + command;
        } else {
            if (FieldColumnFactory.isIgnore(returnClass)) {
                throw new HiSqlException("Select return type not support type. %s", returnClass);
            }
            FieldColumn[] fields = FieldColumnFactory.get(naming, returnClass);
            StringBuilder sb = new StringBuilder("select ");
            for (FieldColumn field : fields) {
                sb.append(field.getColumnName()).append(',');
            }
            sb.deleteCharAt(sb.length() - 1);
            sb.append(' ').append(command);
            return sb.toString();
        }
    }
}
