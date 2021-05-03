package com.keyman.watcher.parser.util;

import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class GZIPUtil {
    private static final Logger log = LoggerFactory.getLogger(GZIPUtil.class);
    public static byte[] compress(byte[] contents){
        byte[] result;
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             GZIPOutputStream gzipOutputStream = new GZIPOutputStream(byteArrayOutputStream)) {
            gzipOutputStream.write(contents);
            result = byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            log.error("compress fail, return origin byte array.", e);
            return contents;
        }
        return result;
    }

    public static byte[] decompress(byte[] compress){
        byte[] decompress = new byte[0];
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(compress);
             GZIPInputStream gzipInputStream = new GZIPInputStream(byteArrayInputStream)){
            IOUtils.copy(gzipInputStream, byteArrayOutputStream);
            decompress = byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            log.error("decompress fail", e);
        }
        return decompress;
    }
}
