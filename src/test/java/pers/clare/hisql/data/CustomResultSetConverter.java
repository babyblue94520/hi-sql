package pers.clare.hisql.data;

import pers.clare.hisql.support.ResultSetConverter;

import java.util.regex.Pattern;

public class CustomResultSetConverter extends ResultSetConverter {
    {
        register(Pattern.class, (rs, i) -> Pattern.compile(rs.getString(i)));
    }
}
