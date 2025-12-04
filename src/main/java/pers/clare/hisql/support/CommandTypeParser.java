package pers.clare.hisql.support;

import pers.clare.hisql.constant.CommandType;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommandTypeParser {
    private static final Pattern SQL_COMMAND_PATTERN = Pattern.compile(
            "(?i)^\\s*(SELECT|WITH|INSERT|UPDATE|DELETE|CREATE|ALTER|DROP|TRUNCATE)\\b"
    );

    public int parse(String command) {
        Matcher matcher = SQL_COMMAND_PATTERN.matcher(command);
        if (matcher.find()) {
            // 獲取匹配到的 SQL 關鍵字並轉為大寫
            String result = matcher.group(1).toUpperCase();

            switch (result) {
                // 查詢語句 -> executeQuery
                case "SELECT":
                case "WITH":
                    return CommandType.QUERY;

                case "INSERT":
                case "UPDATE":
                case "DELETE":
                case "CREATE":
                case "ALTER":
                case "DROP":
                case "TRUNCATE":
                    return CommandType.UPDATE;
            }
        }

        return CommandType.QUERY;
    }
}
