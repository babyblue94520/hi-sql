package pers.clare.hisql.util;

import lombok.experimental.UtilityClass;
import lombok.extern.log4j.Log4j2;
import pers.clare.hisql.function.ConnectionCallback;
import pers.clare.hisql.function.PreparedStatementCallback;
import pers.clare.hisql.function.ResultSetCallback;
import pers.clare.hisql.page.Pagination;
import pers.clare.hisql.page.Sort;

import java.io.InputStream;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@UtilityClass
@Log4j2
@SuppressWarnings("UnusedReturnValue")
public class PreparedStatementUtil {


    public static void setValue(
            PreparedStatement ps
            , Object... parameters
    ) throws SQLException {
        int index = 1;
        if (parameters == null || parameters.length == 0 || ps.getParameterMetaData().getParameterCount() == 0) return;
        for (Object value : parameters) {
            if (value instanceof Pagination
                || value instanceof Sort
                || value instanceof ConnectionCallback
                || value instanceof PreparedStatementCallback
                || value instanceof ResultSetCallback
            ) continue;
            if (value instanceof InputStream) {
                ps.setBinaryStream(index++, (InputStream) value);
            } else {
                ps.setObject(index++, value);
            }
        }
    }

}
