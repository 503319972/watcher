package com.keyman.watcher.parser;

import org.junit.Test;

import java.io.File;

public class ExcelFileParserTest {
    @Test
    public void goThrough(){
        ExcelFileParser parser = new ExcelFileParser(ResultFormat.EXCEL2);
        String parse = parser.parse(new File("C:\\Users\\khong\\Desktop\\test2.xls"));
        System.out.println(parse);
    }
}