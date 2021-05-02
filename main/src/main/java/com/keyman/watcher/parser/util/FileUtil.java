package com.keyman.watcher.parser.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class FileUtil {
    private static final Logger log = LoggerFactory.getLogger(FileUtil.class);
    private static final Class<FileUtil> clazz = FileUtil.class;
    public static String loadFile(String fileName) {
        try (InputStream fileStream = clazz.getClassLoader().getResourceAsStream(fileName);
             InputStreamReader inputStreamReader = new InputStreamReader(fileStream);
             BufferedReader in = new BufferedReader(inputStreamReader)) {
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                stringBuilder.append(line);
            }
            return stringBuilder.toString();
        } catch (Exception e) {
            log.error("fail to load file.");
            return "";
        }
    }

    public static void writeFile(String fileName, byte[] body) {
        try(FileChannel fc = new FileOutputStream(new File(fileName)).getChannel()) {
            ByteBuffer buffer = ByteBuffer.wrap(body);
            fc.write(buffer);
        } catch (Exception e) {
            log.error("fail to write file.", e);
        }
    }
}
