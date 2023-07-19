package pers.clare.hisql.constant;

public class CommandType {
    /**
     * executeQuery
     */
    public static final int Query = 1;
    /**
     * executeUpdate and Statement.RETURN_GENERATED_KEYS
     */
    public static final int Insert = 2;

    /**
     * executeUpdate
     */
    public static final int Update = 3;
}
