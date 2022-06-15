package pers.clare.hisql.naming;

@SuppressWarnings("unused")
public class LowerCaseNamingStrategy implements NamingStrategy {

    @Override
    public StringBuilder turnCamelCase(StringBuilder sb, String name) {
        int l = name.length();
        char[] cs = name.toCharArray();
        char c = cs[0];
        sb.append(Character.toLowerCase(c));
        for (int i = 1; i < l; i++) {
            c = cs[i];
            if (Character.isUpperCase(c)) {
                c = Character.toLowerCase(c);
                sb.append('_');
            }
            sb.append(c);
        }
        return sb;
    }
}
