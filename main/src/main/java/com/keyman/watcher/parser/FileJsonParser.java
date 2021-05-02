package com.keyman.watcher.parser;

import java.io.File;

public interface FileJsonParser extends FileResultParser {
    String parseJson(File path);
}
