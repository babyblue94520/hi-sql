package pers.clare.hisql.exception;

import lombok.Getter;

import java.sql.SQLException;

@Getter
@SuppressWarnings("unused")
public class HiSqlException extends RuntimeException {
    private final SQLException sqlException;

    public HiSqlException(String message, Object... args) {
        super(String.format(message, args));
        sqlException = null;
    }

    public HiSqlException(String message) {
        super(message);
        sqlException = null;
    }

    public HiSqlException(String sql, Throwable cause) {
        super(sql == null ? cause.getMessage() : String.format("%s sql-> %s", cause.getMessage(), sql), cause);
        if (cause instanceof SQLException) {
            this.sqlException = (SQLException) cause;
        } else {
            sqlException = null;
        }
    }

    public HiSqlException(Throwable cause) {
        super(cause);
        if (cause instanceof SQLException) {
            this.sqlException = (SQLException) cause;
        } else {
            sqlException = null;
        }
    }

}
