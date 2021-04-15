package pers.clare.hisql.function;

@FunctionalInterface
public interface ResultSetValueConverter<T> {
   T apply(Object value) throws Exception;
}
