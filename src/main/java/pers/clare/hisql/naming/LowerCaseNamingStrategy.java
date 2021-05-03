package pers.clare.hisql.naming;

public class LowerCaseNamingStrategy implements NamingStrategy{

    public String turnCamelCase(String name) {
        return turnCamelCase(new StringBuilder(), name).toString();
    }

    public StringBuilder turnCamelCase(StringBuilder sb, String name) {
        int l = name.length();
        char[] cs = name.toCharArray();
        char c = cs[0];
        sb.append(Character.toLowerCase(c));
        for (int i = 1; i < l; i++) {
            c = cs[i];
            if (c > 64 && c < 91) {
                c = Character.toLowerCase(c);
                sb.append('_');
            }
            sb.append(c);
        }
        return sb;
    }
}
