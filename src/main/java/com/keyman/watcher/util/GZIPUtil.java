package com.keyman.watcher.util;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class GZIPUtil {
    private static final Logger log = LoggerFactory.getLogger(GZIPUtil.class);
    private GZIPUtil() { }
    private static final Charset ENCODING  = StandardCharsets.UTF_8;
    public static byte[] compress(String str) {
        try (ByteArrayOutputStream baOS = new ByteArrayOutputStream()) {
            GZIPOutputStream gzipOS = new GZIPOutputStream(baOS);
            gzipOS.write(str.getBytes(ENCODING));
            gzipOS.close();
            return baOS.toByteArray();
        } catch (Exception ex) {
            log.error("compress fail, return origin byte array.", ex);
            return str.getBytes();
        }
    }

    public static String decompress(byte[] bytes) {
        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            GZIPInputStream gzipIS = new GZIPInputStream(byteArrayInputStream);
            byte[] buffer = new byte[1024];
            int n;
            while ((n = gzipIS.read(buffer)) >= 0) {
                out.write(buffer, 0, n);
            }
            gzipIS.close();
            return out.toString();
        } catch (Exception ex) {
            log.error("decompress fail", ex);
        }
        return null;
    }
}
