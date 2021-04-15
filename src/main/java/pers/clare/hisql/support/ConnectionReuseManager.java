package pers.clare.hisql.support;

import pers.clare.hisql.exception.HiSqlException;

import java.util.Stack;

public class ConnectionReuseManager {
    private final Stack<ConnectionReuse> stack = new Stack<>();

    private ConnectionReuse prev = null;

    private ConnectionReuse current = null;


    public void init(boolean transaction, int isolation, boolean readonly) {
        if (transaction && readonly) {
            throw new HiSqlException("Cannot execute statement in a READ ONLY transaction.");
        }
        stack.push(current);
        if (current == null) {
            current = new ConnectionReuse(transaction, isolation, readonly);
        } else {
            if (transaction != current.transaction || isolation != current.isolation) {
                current = new ConnectionReuse(transaction, isolation, readonly);
            }
        }

        prev = null;
    }

    public ConnectionReuse get() {
        return current;
    }

    public void commit() {
        if (check()) {
            prev.commit();
        }
    }

    public void rollback() {
        if (check()) {
            prev.rollback();
        }
    }

    public void close() {
        if (check()) {
            prev.close();
        }
        prev = null;
    }

    private boolean check() {
        if (stack.size() == 0) return false;
        if (prev == null) {
            prev = current;
            current = stack.pop();
        }
        return prev != null && (prev != current || stack.size() <= 0);
    }

    public ConnectionReuse getCurrent() {
        return current;
    }
}
