package com.keyman.watcher.file;

import com.keyman.watcher.parser.FileResultParser;
import com.keyman.watcher.parser.ResultFormat;
import com.keyman.watcher.parser.TextFileParser;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.keyman.watcher.parser.ResultFormat.JSON;
import static com.keyman.watcher.parser.ResultFormat.TXT;


public class FilePathHierarchyParser {

    private final Map<String, String> hierarchy = new HashMap<>();
    private final String rootPath;

    public FilePathHierarchyParser(String rootPath) {
        this.rootPath = rootPath;
    }

    public Map<String, String> buildHierarchy() {
        File top = new File(rootPath);
        if (!top.exists() || !top.isDirectory()) return null;
        goThroughPath(rootPath);
        return hierarchy;
    }

    private void goThroughPath(String top){
        File topFile = new File(top);
        String[] list = topFile.list();
        Arrays.stream(Optional.ofNullable(list).orElse(new String[]{})).forEach(p -> {
            String absPath = top + "/" + p;
            File file = new File(absPath);
            if (file.isDirectory()) {
                goThroughPath(absPath);
            } else {
                String[] blocks = p.split("\\.");
                FileResultParser parser = chooseFileParser(blocks[blocks.length - 1]);
                Optional.ofNullable(parser).ifPresent(e ->
                        hierarchy.put(file.getPath(), parser.parse(file)));
            }
        });
    }

    private FileResultParser chooseFileParser(String postfix) {
        ResultFormat resultFormat = ResultFormat.Of(postfix);
        switch (resultFormat) {
            case JSON:
                return new TextFileParser(JSON);
            case TXT:
                return new TextFileParser(TXT);
        }
        return null;
    }


}
