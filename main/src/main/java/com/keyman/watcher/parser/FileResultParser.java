package com.keyman.watcher.parser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public interface FileResultParser {
    Logger logger = LoggerFactory.getLogger(FileResultParser.class);
    String parse(File file);

    default String readByLine(File file) {
        if (file.isDirectory()) return "";
        try (InputStream fileStream = this.getClass().getClassLoader().getResourceAsStream(file.getAbsolutePath());
             InputStreamReader inputStreamReader = new InputStreamReader(fileStream);
             BufferedReader in = new BufferedReader(inputStreamReader)) {
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                stringBuilder.append(line);
            }
            return stringBuilder.toString();
        } catch (Exception e) {
            logger.error("fail to load file.");
            return "";
        }
    }

    default List<String> readLines(File file) {
        if (file.isDirectory()) return Collections.emptyList();
        try (InputStream fileStream = new FileInputStream(file);
             InputStreamReader inputStreamReader = new InputStreamReader(fileStream);
             BufferedReader in = new BufferedReader(inputStreamReader)) {
            ArrayList<String> list = new ArrayList<>();
            String line;
            while ((line = in.readLine()) != null) {
                list.add(line);
            }
            return list;
        } catch (Exception e) {
            logger.error("fail to load file.");
            return Collections.emptyList();
        }
    }

    default ObjectHolder<String, List<String>> readStrAndLines(File file) {
        if (file.isDirectory()) return ObjectHolder.of("", Collections.emptyList());
        StringBuilder stringBuilder = new StringBuilder();
        try (InputStream fileStream = new FileInputStream(file);
             InputStreamReader inputStreamReader = new InputStreamReader(fileStream);
             BufferedReader in = new BufferedReader(inputStreamReader)) {
            ArrayList<String> list = new ArrayList<>();
            String line;
            while ((line = in.readLine()) != null) {
                list.add(line);
                stringBuilder.append(line);
            }
            return ObjectHolder.of(stringBuilder.toString(), list);
        } catch (Exception e) {
            logger.error("fail to load file.");
            return ObjectHolder.of("", Collections.emptyList());
        }
    }
}
