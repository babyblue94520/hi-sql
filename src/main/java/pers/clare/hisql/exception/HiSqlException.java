package pers.clare.hisql.exception;

public class HiSqlException extends RuntimeException {

    public HiSqlException(String message, Object... args) {
        super(String.format(message, args));
    }

    public HiSqlException(String message) {
        super(message);
    }

    public HiSqlException(String message, Throwable cause) {
        super(message, cause);
    }

    public HiSqlException(Throwable cause) {
        super(cause);
    }
}
