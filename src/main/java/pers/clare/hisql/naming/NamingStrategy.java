package pers.clare.hisql.naming;

public interface NamingStrategy {

    default String turnCamelCase(String name) {
        return turnCamelCase(new StringBuilder(), name).toString();
    }

    StringBuilder turnCamelCase(StringBuilder sb, String name);
}
