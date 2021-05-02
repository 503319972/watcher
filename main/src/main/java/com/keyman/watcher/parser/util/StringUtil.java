package com.keyman.watcher.parser.util;

public class StringUtil {

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
}
