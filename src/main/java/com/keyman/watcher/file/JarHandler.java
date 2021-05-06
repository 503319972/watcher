package com.keyman.watcher.file;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.Optional;
import java.util.UUID;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@Component
public class JarHandler {
    private static final Logger log = LoggerFactory.getLogger(JarHandler.class);
    private String unJarPath;

    private String unJar() {
        URL resource = JarHandler.class.getClassLoader().getResource("");
        Optional.ofNullable(resource).ifPresent(url -> {
            String path = url.getFile();
            File file = new File(path);
            String jarPath = file.getParentFile().getPath();
            jarPath = jarPath.substring(0, jarPath.lastIndexOf("!" + File.separator + "BOOT-INF"));
            jarPath = jarPath.replace("file:" + File.separator, "");
            unJarPath = jarPath.replace(".jar", "") + "-" + UUID.randomUUID();
            decompress(jarPath, unJarPath);
        });
        return unJarPath;
    }

    public void decompress(String fileName, String outputPath) {
        if (!outputPath.endsWith(File.separator)) {
            outputPath += File.separator;
        }
        try(JarFile jf = new JarFile(fileName)) {
            for (Enumeration<JarEntry> e = jf.entries(); e.hasMoreElements();)
            {
                JarEntry je = e.nextElement();
                String outFileName = outputPath + je.getName();
                Path path = Paths.get(outFileName);
                makeSupDir(outFileName);
                if(!Files.isDirectory(path))
                {
                    try (InputStream in = jf.getInputStream(je);
                         OutputStream out = new BufferedOutputStream(Files.newOutputStream(path))) {
                        byte[] buffer = new byte[2048];
                        int nBytes;
                        while ((nBytes = in.read(buffer)) > 0)
                        {
                            out.write(buffer, 0, nBytes);
                        }
                        out.flush();
                    }
                }
            }
        } catch (Exception e) {
            log.error("unzip jar error", e);
            clean(outputPath);
        }
    }

    private void makeSupDir(String outFileName) {
        try {
            Pattern p = Pattern.compile("[/\\" + File.separator + "]");
            Matcher m = p.matcher(outFileName);
            while (m.find()) {
                int index = m.start();
                String subDir = outFileName.substring(0, index);
                Path path = Paths.get(subDir);
                if (!Files.exists(path))
                    Files.createDirectory(path);
            }
        } catch (IOException e) {
            log.error("make directory failed", e);
        }
    }


    public void clean(String path) {
        Path filePath = Paths.get(path);
        try {
            if(Files.isDirectory(filePath)) {
                try (Stream<Path> fileList = Files.list(filePath)) {
                    fileList.forEach(p -> clean(path + File.separator + p.getFileName()));
                }
            }
            Files.delete(filePath);
        }
        catch (IOException e) {
            log.error("delete file failed", e);
        }
    }

    @PreDestroy
    public void clean() {
        if (unJarPath != null) {
            clean(unJarPath);
        }
    }

    public String getLibsInJar() {
        StringBuilder lib = new StringBuilder();
        String jarTempPath = unJar();
        String libs = jarTempPath + File.separator + "BOOT-INF" + File.separator + "lib";
        Path libPath = Paths.get(libs);
        if (Files.exists(libPath)) {
            try(Stream<Path> list = Files.list(libPath)) {
                list.forEach(p -> lib.append(p).append(File.pathSeparator));
            } catch (IOException e) {
                log.error("cannot get libs in jar", e);
            }
        }
        return lib.toString();
    }
}
