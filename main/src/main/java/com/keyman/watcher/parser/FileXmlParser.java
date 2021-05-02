package com.keyman.watcher.parser;

import java.io.File;

public interface FileXmlParser extends FileResultParser {
    String parseXml(File path);
}
