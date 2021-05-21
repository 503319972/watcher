package com.keyman.watcher.file;

import com.keyman.watcher.exception.UnknownResultFormatException;
import com.keyman.watcher.netty.client.Client;
import com.keyman.watcher.parser.ExcelFileParser;
import com.keyman.watcher.parser.FileResultParser;
import com.keyman.watcher.parser.ResultFormat;
import com.keyman.watcher.parser.TextFileParser;
import com.keyman.watcher.util.GZIPUtil;
import org.apache.commons.compress.compressors.gzip.GzipUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.keyman.watcher.parser.ResultFormat.EXCEL;
import static com.keyman.watcher.parser.ResultFormat.EXCEL2;
import static com.keyman.watcher.parser.ResultFormat.JSON;
import static com.keyman.watcher.parser.ResultFormat.TXT;


public class FilePathHierarchyParser {
    private static final Logger log = LoggerFactory.getLogger(FilePathHierarchyParser.class);
    private final Map<String, String> hierarchy = new HashMap<>();
    private final Map<String, byte[]> compactedHierarchy = new HashMap<>();
    private final String rootPath;

    public FilePathHierarchyParser(String rootPath) {
        this.rootPath = rootPath;
    }

    public Map<String, String> buildHierarchy() {
        File top = new File(rootPath);
        if (!top.exists() || !top.isDirectory()) {
            log.error("file path: {} is not a directory", rootPath);
            return null;
        }
        goThroughPath(rootPath, false);
        return hierarchy;
    }

    public Map<String, byte[]> buildHierarchy(boolean compact) {
        File top = new File(rootPath);
        if (!top.exists() || !top.isDirectory()) {
            log.error("file path: {} is not a directory", rootPath);
            return null;
        }
        goThroughPath(rootPath, compact);
        return compactedHierarchy;
    }

    private void goThroughPath(String top, boolean compact){
        File topFile = new File(top);
        String[] list = topFile.list();
        Arrays.stream(Optional.ofNullable(list).orElse(new String[]{})).forEach(p -> {
            String absPath = top + "/" + p;
            File file = new File(absPath);
            if (file.isDirectory()) {
                goThroughPath(absPath, compact);
            } else {
                String[] blocks = p.split("\\.");
                FileResultParser parser = chooseFileParser(blocks[blocks.length - 1]);
                Optional.ofNullable(parser).ifPresent(e -> {
                    String fileKey = file.getPath().replace(rootPath, "");
                    fileKey = fileKey.substring(0, fileKey.lastIndexOf('.')).
                            replaceAll("\\s", "");
                    if (fileKey.startsWith("\\")) {
                        fileKey = fileKey.substring(1).replace("\\", "/");
                    } else if(fileKey.startsWith("/")) {
                        fileKey = fileKey.substring(1);
                    }
                    if (compact) {
                        compactedHierarchy.put(fileKey, GZIPUtil.compress(parser.parse(file)));
                    } else
                        hierarchy.put(fileKey, parser.parse(file));
                });
            }
        });
    }

    private FileResultParser chooseFileParser(String postfix) {
        ResultFormat resultFormat = ResultFormat.Of(postfix);
        switch (resultFormat) {
            case JSON:
            case CSV:
                return new TextFileParser(JSON);
            case TXT:
                return new TextFileParser(TXT);
            case EXCEL:
                return new ExcelFileParser(EXCEL);
            case EXCEL2:
                return new ExcelFileParser(EXCEL2);
            default:
                return null;
        }
    }


}
