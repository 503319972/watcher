package com.keyman.watcher.parser.util;

public class StringUtil {
    private StringUtil() {}
    public static String escapeRegex(String regex) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < regex.length(); i++) {
            char w = regex.charAt(i);
            if (w == '!' || w == '$' || w == '%' || w == '^' || w == '*' || w == '&') {
                stringBuilder.append("\\").append(w);
            } else if (w == '\\') {
                stringBuilder.append('\\');
            }
            stringBuilder.append(w);
        }
        return stringBuilder.toString();
    }

    public static boolean isEmpty(String str) {
        return str == null || str.equals("");
    }

    public static boolean isBlank(String str) {
        if (isEmpty(str)) return true;
        else {
            for (int i = 0; i < str.length(); i++) {
                if (str.charAt(i) != ' ' && str.charAt(i) != '\t') {
                    return false;
                }
            }
        }
        return true;
    }
}
