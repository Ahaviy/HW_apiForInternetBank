package ru.skillfactory.project;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.math.BigDecimal;
import java.sql.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

public class MyUtils {
    public static String getDateTimeFromDate(Date date) {
        String pattern = "yyyy-MM-dd HH:mm:ss";
        SimpleDateFormat dateFormat = new SimpleDateFormat(pattern);
        return "'" + dateFormat.format(date) + ".000 +0300'";
    }

    public static String generateDateTimeFromDate(Date date) {
        date.setHours((int) (10 + Math.random() * 12));
        date.setMinutes((int) (Math.random() * 59));
        date.setSeconds((int) (Math.random() * 59));
        return getDateTimeFromDate(date);
    }

    public static String getStringFromBigDecimal(BigDecimal value) {
        Locale locale = new Locale("en", "UK");
        String pattern = "###.##";
        DecimalFormat decimalFormat = (DecimalFormat) NumberFormat.getNumberInstance(locale);
        decimalFormat.applyPattern(pattern);
        return decimalFormat.format(value);
    }

    public static boolean isDateTimeValid(String date) {
        String pattern = "yyyy-MM-dd";
        Calendar calendar = new GregorianCalendar();
        calendar.setLenient(false);
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat(pattern);
            calendar.setTime(dateFormat.parse(date));
            if (dateFormat.format(calendar.getTime()).equals(date)) return true;
        } catch (Exception e) {}
        return false;
    }





}
