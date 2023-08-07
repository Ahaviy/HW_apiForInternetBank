package ru.skillfactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.skillfactory.project.ConnectionSettings;
import ru.skillfactory.project.DataBaseCreator;
import ru.skillfactory.project.MyUtils;

import java.io.File;
import java.math.BigDecimal;
import java.sql.*;
import java.util.Calendar;
import java.util.GregorianCalendar;


public class DemoBaseGenerator {

    private static Logger log = LogManager.getLogger(DemoBaseGenerator.class);
    private DataBaseCreator dbCreator;
    private static final String SETTINGS_FILE_NAME = "./src/main/resources/settings.xml";
    private static final String DEFAULT_DATABASE_NAME = "projectbase";
    private static final int NUMBER_OF_USERS = 100;
    private static final int NUMBER_OF_DAYS = 100;

    public static void main(String[] args) {
        DemoBaseGenerator demoBaseGenerator = new DemoBaseGenerator();
        demoBaseGenerator.GenerateDemoDB();
    }

    public DemoBaseGenerator() {
        dbCreator = new DataBaseCreator(SETTINGS_FILE_NAME, DEFAULT_DATABASE_NAME);
    }

    private static Calendar date;

    private void GenerateDemoDB() {

        if (dbCreator.isValidConnection()) {
            if (dbCreator.isDatabaseExists()) {
                log.info("БД с именем: " + dbCreator.getSettings().getDatabaseName() + " уже создана. ");
            } else {
                log.info("БД " + dbCreator.getSettings().getDatabaseName() + " не найдена, будет создана новая база");
                dbCreator.createDB();
                FillDB();
            }
        }
    }

    private void FillDB() {
        log.info("добавляем " + NUMBER_OF_USERS + " пользователей в таблицу balance");
        for (int userId = 1; userId <= NUMBER_OF_USERS; userId++) {
            dbCreator.addNewUserToDB();
        }
        log.info("Пользователи успешно добавлены");
        log.info("Начинаем заполнять историю опреаций:");
        date = new GregorianCalendar();
        date.roll(Calendar.DAY_OF_YEAR, -(NUMBER_OF_DAYS + 1));
        for (int day = 0; day < NUMBER_OF_DAYS; day++) {
            log.info("Добавляем записи для дня: " + day + " из " + NUMBER_OF_DAYS);
            for (int userId = 1; userId <= NUMBER_OF_USERS; userId++) {
                int choice = (int) (Math.random() * 100);
                if (choice > 90) {
                    dbCreator.addNewHistoryEntry(userId, 1, BigDecimal.valueOf(300 + Math.random() * 900),
                            MyUtils.generateDateTimeFromDate(date.getTime()));
                } else {
                    if (choice > 60) {
                        dbCreator.addNewHistoryEntry(userId, 2, BigDecimal.valueOf(100 + Math.random() * 300),
                                MyUtils.generateDateTimeFromDate(date.getTime()));
                    }
                }
            }
            date.roll(Calendar.DAY_OF_YEAR, 1);
        }
        log.info("Заполнение базы успешно завершино");
    }
}
