package pers.clare.hisql.support;

public class ConnectionReuseHolder {

    private static final ThreadLocal<ConnectionReuseManager> cache = new NamedThreadLocal<>("Connection Cache Holder");

    ConnectionReuseHolder() {
    }

    public static ConnectionReuseManager init(boolean transaction, int isolation, boolean readonly) {
        ConnectionReuseManager manager = cache.get();
        if (manager == null) {
            cache.set((manager = new ConnectionReuseManager()));
        }
        manager.init(transaction, isolation, readonly);
        return manager;
    }

    public static ConnectionReuse get() {
        ConnectionReuseManager manager = cache.get();
        return manager == null ? null : manager.getCurrent();
    }

    static class NamedThreadLocal<T> extends ThreadLocal<T> {
        private final String name;

        public NamedThreadLocal(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return this.name;
        }
    }
}


