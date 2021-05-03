package pers.clare.hisql.naming;

public class UpperCaseNamingStrategy implements NamingStrategy {
    @Override
    public String turnCamelCase(String name) {
        return turnCamelCase(new StringBuilder(), name).toString();
    }

    @Override
    public StringBuilder turnCamelCase(StringBuilder sb, String name) {
        int l = name.length();
        char[] cs = name.toCharArray();
        char c = cs[0];
        sb.append(Character.toUpperCase(c));
        for (int i = 1; i < l; i++) {
            c = cs[i];
            if (c > 64 && c < 91) {
                c = Character.toUpperCase(c);
                sb.append('_');
            }
            sb.append(Character.toUpperCase(c));
        }
        return sb;
    }
}
