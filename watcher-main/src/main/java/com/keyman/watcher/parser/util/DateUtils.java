package com.keyman.watcher.parser.util;

import org.apache.logging.log4j.util.Strings;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class DateUtils {
    private DateUtils(){}

    private static final DateTimeFormatter FORMATTER_PATTERN_YYYY_MM_DD = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter FORMATTER_PATTERN_YYYYMMDD = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter FORMATTER_PATTERN_YYYYMMDDHHmmss = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter FORMATTER_PATTERN_MMDDYYYYHHmmss = DateTimeFormatter.ofPattern("MM-dd-yyyy HH:mm:ss");
    public static final LocalDate DATE_1900 = LocalDate.of(1900, 1, 1);

    /**
     *
     * @param timeStamp 44205
     * @return yyyy-MM-dd HH:mm:ss
     */
    public static String convertToTimestamp(Long timeStamp) {
        if (timeStamp != null && timeStamp >= 0) {
            LocalDateTime localDateTime = LocalDateTime.ofEpochSecond(timeStamp, 0, ZoneOffset.ofHours(0));
            return localDateTime.format(FORMATTER_PATTERN_YYYYMMDDHHmmss);
        } else {
            return Strings.EMPTY;
        }
    }

    /**
     *
     * @param timestamp MM-dd-yyyy
     * @return yyyy-MM-dd
     */
    public static String convertMMDDYYYYToYYYYMMDD(String timestamp){
        LocalDateTime localDateTime = LocalDateTime.from(FORMATTER_PATTERN_MMDDYYYYHHmmss.parse(timestamp));
        return localDateTime.format(FORMATTER_PATTERN_YYYYMMDDHHmmss);
    }

    /**
     *
     * @param dateTimeStamp 44205
     * @return yyyy-MM-dd
     */
    public static String convertToYYYYMMDD(Long dateTimeStamp) {
        if (dateTimeStamp != null && dateTimeStamp > 2) {
            return DATE_1900.plusDays(dateTimeStamp - 2).format(FORMATTER_PATTERN_YYYYMMDD);
        } else {
            return Strings.EMPTY;
        }
    }


    /**
     *
     * @param date yyyy-MM-dd
     * @return 44205
     */
    public static Long convertToLong(String date) {
        return convertToLong(date, FORMATTER_PATTERN_YYYYMMDD);
    }

    public static Long convertToLong(String date, DateTimeFormatter dateTimeFormatter){
        LocalDate localDate = LocalDate.parse(date, dateTimeFormatter);
        return localDate.toEpochDay() - DATE_1900.toEpochDay() + 2;
    }

    /**
     *
     * @param dateStr like 44205
     * @return like yyyy-MM-dd
     */
    public static String convertLongStrToDateStr(String dateStr){
        if(StringUtil.isBlank(dateStr))
            return Strings.EMPTY;
        long dateLong = Long.parseLong(dateStr);
        if (dateLong > 2) {
            return DATE_1900.plusDays(dateLong - 2).format(FORMATTER_PATTERN_YYYY_MM_DD);
        } else {
            return Strings.EMPTY;
        }
    }

    public static String convertDateToStr(Date date){
        if (date != null) {
            LocalDateTime localDateTime = LocalDateTime.ofInstant(date.toInstant(), ZoneOffset.ofHours(8));
            return localDateTime.format(FORMATTER_PATTERN_YYYYMMDDHHmmss);
        }
        return Strings.EMPTY;
    }
}