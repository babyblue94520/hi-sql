package pers.clare.hisql.exception;

import java.sql.SQLException;

@SuppressWarnings("unused")
public class HiSqlException extends RuntimeException {
    private SQLException sqlException;

    public HiSqlException(String message, Object... args) {
        super(String.format(message, args));
    }

    public HiSqlException(String message) {
        super(message);
    }

    public HiSqlException(String sql, Throwable cause) {
        super(String.format("%s sql-> %s", cause.getMessage(), sql), cause);

        if (cause instanceof SQLException) {
            this.sqlException = (SQLException) cause;
        }
    }

    public HiSqlException(Throwable cause) {
        super(cause);
        if (cause instanceof SQLException) {
            this.sqlException = (SQLException) cause;
        }
    }

    public SQLException getSqlException() {
        return sqlException;
    }
}
