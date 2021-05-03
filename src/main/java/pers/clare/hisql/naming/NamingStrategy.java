package pers.clare.hisql.naming;

public interface NamingStrategy {
    String turnCamelCase(String name);

    StringBuilder turnCamelCase(StringBuilder sb, String name);
}
