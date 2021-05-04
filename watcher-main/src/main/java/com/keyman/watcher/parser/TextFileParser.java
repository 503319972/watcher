package com.keyman.watcher.parser;

import com.keyman.watcher.exception.UnknownResultFormatException;
import com.keyman.watcher.parser.util.JsonUtil;
import com.keyman.watcher.parser.util.XmlUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextFileParser implements FileJsonParser, FileXmlParser {

    private final String separator;
    private final ResultFormat resultFormat;
    private boolean firstLine = true;
    private Integer numberOfSeparator = 0;
    private boolean trueFormat = true;
    private String contentStr;
    private static final Pattern SPECIAL_WORD = Pattern.compile("(?=[\\x21-\\x7e\\s]+)[^A-Za-z0-9]+");
    private Pattern wordPattern = null;
    private final List<String> specialWords = new ArrayList<>();
    private final List<Map<String, String>> content = new ArrayList<>();


    public TextFileParser(String separator) {
        this(separator, ResultFormat.JSON);
    }

    public TextFileParser(String separator, ResultFormat resultFormat) {
        this.separator = separator;
        this.resultFormat = resultFormat;
    }

    public TextFileParser(ResultFormat resultFormat) {
        this(null, resultFormat);
    }

    public TextFileParser() {
        this("\t", ResultFormat.JSON);
    }

    @Override
    public String parseJson(File file) {
        return buildResult(file);
    }

    private String buildResult(File file) {
        ObjectHolder<String, List<String>> objectHolder = readStrAndLines(file);
        if (JsonUtil.isJsonFormat(objectHolder.getLeft())) {
            return objectHolder.getLeft();
        }
        StringBuilder builder = new StringBuilder();
        ArrayList<String> params = new ArrayList<>();
        objectHolder.getRight().forEach(line -> {
            checkLineFormat(line);
            if (trueFormat) {
                String[] values = handleLine(line);
                if (firstLine) {
                    params.addAll(Arrays.asList(values));
                    firstLine = false;
                }
                else {
                    HashMap<String, String> map = new HashMap<>();
                    for (int i = 0; i < values.length; i++) {
                        map.put(params.get(i), values[i]);
                    }
                    content.add(map);
                }
            }
            builder.append(line);
        });

        contentStr = builder.toString();
        if (trueFormat) {
            return JsonUtil.writeToString(content);
        } else {
            return "{ \"results\": \"" + contentStr + "\"}";
        }
    }

    private String[] handleLine(String line) {
        if (separator != null) {
            return line.split(separator);
        }
        ArrayList<String> values = new ArrayList<>();
        if (!specialWords.isEmpty() && line != null) {
            for (int i = 0; i < specialWords.size(); i++) {
                String sword = specialWords.get(i);
                String[] words = line.split(escapeRegex(specialWords.get(i)));
                if (words.length > 0) {
                    values.add(words[0]);
                    if (words.length > 1) {
                        line = line.substring(words[0].length() + sword.length());
                        if (i == specialWords.size() - 1) {
                            values.add(line);
                        }
                    }
                }
            }
        }
        return values.toArray(new String[]{});
    }

    @Override
    public String parse(File path) {
        switch (resultFormat) {
            case JSON:
            case TXT:
            case CSV:
                return parseJson(path);
            case XML:
                return parseXml(path);
            default:
                throw new UnknownResultFormatException();
        }
    }


    protected void checkLineFormat(String line) {
        if (firstLine) {
            if (separator != null && !separator.equals(""))
                numberOfSeparator = line.split(separator).length;
            else {
                Matcher matcher = SPECIAL_WORD.matcher(line);
                while (matcher.find()) {
                    String curWord = matcher.group();
                    specialWords.add(curWord);
                }
                StringBuilder regex = new StringBuilder();
                specialWords.forEach(word -> regex.append(word.startsWith("\\") ? ".*\\" : ".*").append(word));
                regex.append(".*");
                wordPattern = Pattern.compile(regex.toString());
            }
        } else {
            if (separator != null && !separator.equals("")) {
                if (numberOfSeparator != line.split(separator).length) {
                    trueFormat = false;
                }
            }
            else if (specialWords.size() > 0) {
                trueFormat = wordPattern.matcher(line).find();
            } else {
                trueFormat = false;
            }
        }
    }

    @Override
    public String parseXml(File path) {
        String result = content.size() == 0 && firstLine ? buildResult(path) : contentStr;
        if (content.size() > 0){
            return XmlUtil.toXml(content);
        } else if (XmlUtil.isXMLFormat(result)) {
            return result;
        }
        return "<result>" + result + "</result>";
    }

    public static String escapeRegex(String regex) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < regex.length(); i++) {
            char w = regex.charAt(i);
            if (w == '!' || w == '$' || w == '%' || w == '^' || w == '*' || w == '&') {
                stringBuilder.append("\\");
            }
            stringBuilder.append(w);
        }
        return stringBuilder.toString();
    }
}
