package com.keyman.watcher.parser;

import com.keyman.watcher.exception.UnknownResultFormatException;
import com.keyman.watcher.util.DateUtils;
import com.keyman.watcher.util.JsonUtil;
import com.keyman.watcher.util.StringUtil;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ExcelFileParser implements FileResultParser {
    private static final Logger log = LoggerFactory.getLogger(ExcelFileParser.class);
    private final ResultFormat resultFormat;

    public ExcelFileParser(ResultFormat resultFormat) {
        this.resultFormat = resultFormat;
    }

    @Override
    public String parse(File file) {
        Workbook sheets = buildBook(file.getPath());
        if (sheets != null) {
            int numberOfSheets = sheets.getNumberOfSheets();
            if (numberOfSheets == 1) {
                Sheet mainSheet = sheets.getSheetAt(0);
                return JsonUtil.writeToString(goThroughSheet(mainSheet));
            } else {
                LinkedHashMap<String, List<Map<String, String>>> allSheets = new LinkedHashMap<>();
                for (int i = 0; i < numberOfSheets; i++) {
                    Sheet curSheet = sheets.getSheetAt(i);
                    String curSheetName = sheets.getSheetName(i);
                    allSheets.put(curSheetName, goThroughSheet(curSheet));
                }
                return JsonUtil.writeToString(allSheets);
            }
        }
        return null;
    }


    private List<Map<String, String>> goThroughSheet(Sheet sheet) {
        int rowNum = sheet.getPhysicalNumberOfRows();
        Map<String, String> rowTitles = new LinkedHashMap<>();
        List<Map<String, String>> rows = new ArrayList<>();
        int curColNum = -1;
        boolean format = true;
        for (int r = 0; r < rowNum; r++) {
            Row row = sheet.getRow(r);
            int colNum = row.getPhysicalNumberOfCells();
            curColNum = curColNum == -1 ? colNum : curColNum;
            if (curColNum != colNum) {
                format = false;
            }
            Map<String, String> colValue = new LinkedHashMap<>();
            for (int c = 0; c < colNum; c++) {
                String key = "col" + c;
                String value = getCellFormatValue(row.getCell(c));
                if (r == 0) {
                    if (!StringUtil.isEmpty(value))
                        rowTitles.put(key, value);
                } else if (!format && !StringUtil.isBlank(value)) {
                    colValue.put(key, value);
                } else {
                    if (rowTitles.size() > 0)
                        colValue.put(rowTitles.get(key), value);
                }
            }
            if (r != 0) {
                rows.add(colValue);
                if (!format) {
                    rows.add(0, rowTitles);
                }
            }
        }
        return rows;
    }

    private String getCellFormatValue(Cell cell) {
        String cellValue = "";
        if (cell != null) {
            // cell type
            switch (cell.getCellType()) {
                case NUMERIC:
                case FORMULA: {
                    // date format
                    if (DateUtil.isCellDateFormatted(cell)) {
                        cellValue = DateUtils.convertDateToStr(cell.getDateCellValue());
                    } else {
                        // number format
                        cellValue = String.valueOf(cell.getNumericCellValue());
                    }
                    break;
                }
                case STRING: {
                    cellValue = cell.getRichStringCellValue().getString();
                    break;
                }
                default:
                    cellValue = "";
            }
        }
        return cellValue;
    }

    private Workbook buildBook(String filePath){
        Workbook wb = null;
        if (filePath == null) {
            throw new IllegalArgumentException("file path cannot be null");
        }
        try (InputStream is = new FileInputStream(filePath)){
            switch (resultFormat) {
                case EXCEL:
                    wb = new XSSFWorkbook(is);
                    break;
                case EXCEL2:
                    wb = new HSSFWorkbook(is);
                    break;
                default:
                    throw new UnknownResultFormatException("unknown file format");
            }
        } catch (IOException e) {
            log.error("read excel file failed", e);
        }
        return wb;
    }


}
