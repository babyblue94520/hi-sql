package pers.clare.hisql.support;

import pers.clare.hisql.constant.CommandType;

public class CommandTypeParser {

    public int parse(String command) {
        int c = command.charAt(0);
        switch (c) {
            case 'i':
            case 'I':
                return CommandType.Insert;
            case 'u':
            case 'U':
            case 'd':
            case 'D':
                return CommandType.Update;
            default:
                return CommandType.Query;
        }
    }
}
