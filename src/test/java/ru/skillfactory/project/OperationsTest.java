package ru.skillfactory.project;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.math.BigDecimal;
import java.sql.*;
import java.util.Calendar;
import java.util.GregorianCalendar;


import static org.junit.jupiter.api.Assertions.assertEquals;

class OperationsTest {

    private static Logger log = LogManager.getLogger(OperationsTest.class);

    public static DataBaseCreator dbCreator;
    private static boolean isDBWasCreated;
    private static final String SETTINGS_FILE_NAME = "./src/test/resources/settings.xml";
    private static final String DEFAULT_DATABASE_NAME = "testbase";

    private static ConnectionSettings fakeSettings;


    @BeforeAll
    static void setUp() {
        dbCreator = new DataBaseCreator(SETTINGS_FILE_NAME, DEFAULT_DATABASE_NAME);
        if (dbCreator.isValidConnection()) {
            if (dbCreator.isDatabaseExists()) {
                log.info("найдена БД для тестов:" + dbCreator.getSettings().getDatabaseName());
                isDBWasCreated = false;
            } else {
                log.info("БД " + dbCreator.getSettings().getDatabaseName() + " не найдена, будет создана новая база");
                dbCreator.createDB();
                fillTestData();
                isDBWasCreated = true;
            }
        }
        generateFakeSettings();
    }

    @AfterAll
    static void tearDown() {
        if (isDBWasCreated) {
            dbCreator.deleteDB();
            log.info("созданая ранее БД для теста была удалена");
        }
    }

    @Test
    void getBalance() {
        Operations operations = new Operations(dbCreator.getSettings());
        Operations fakeOperations = new Operations(fakeSettings);
        BigDecimal value1 = BigDecimal.valueOf(8500000000.00);
        BigDecimal value2 = BigDecimal.valueOf(1000000320.58);
        BigDecimal value3 = BigDecimal.valueOf(-1);
        assertEquals(0, value1.compareTo(operations.getBalance(1).getResult()));
        assertEquals(0, value3.compareTo(fakeOperations.getBalance(1).getResult()));
        assertEquals(0, value2.compareTo(operations.getBalance(2).getResult()));
        assertEquals(0, value3.compareTo(operations.getBalance(500).getResult()));
        assertEquals(0, value3.compareTo(operations.getBalance(-109).getResult()));
    }

    @Test
    void takeMoney() {
        Operations operations = new Operations(dbCreator.getSettings());
        Operations fakeOperations = new Operations(fakeSettings);
        BigDecimal value1 = BigDecimal.valueOf(120.9);
        BigDecimal value2 = BigDecimal.valueOf(10000);
        BigDecimal value3 = operations.getBalance(3).getResult().subtract(value1);
        BigDecimal value4 = BigDecimal.valueOf(-20);
        assertEquals(0, BigDecimal.valueOf(1).compareTo(operations.takeMoney(3, value1).getResult()));
        assertEquals(0, BigDecimal.valueOf(0).compareTo(operations.takeMoney(4, value2).getResult()));
        assertEquals(0, value3.compareTo(operations.getBalance(3).getResult()));
        // проверка
        assertEquals(0, BigDecimal.valueOf(-1).compareTo(fakeOperations.takeMoney(3, value1).getResult()));
        assertEquals(0, BigDecimal.valueOf(-1).compareTo(operations.takeMoney(3, value4).getResult()));
        assertEquals(0, BigDecimal.valueOf(-1).compareTo(operations.takeMoney(235, value1).getResult()));
        Calendar fromD = new GregorianCalendar();
        Calendar endD = new GregorianCalendar();
        endD.roll(Calendar.DAY_OF_YEAR,1);
        OperationHistoryResult result = operations.getOperationList(3, MyUtils.getDateTimeFromDate(fromD.getTime()),
                MyUtils.getDateTimeFromDate(endD.getTime()));
        assertEquals(result.getResult().toArray()[0].toString().substring(0,10),
                MyUtils.getDateTimeFromDate(fromD.getTime()).substring(0,10));
        assertEquals(result.getResult().toArray()[0].toString().substring(20,28), "withdraw");
        assertEquals(result.getResult().toArray()[0].toString().substring(29,34),
                MyUtils.getStringFromBigDecimal(value1));
    }

    @Test
    void putMoney() {
        Operations operations = new Operations(dbCreator.getSettings());
        Operations fakeOperations = new Operations(fakeSettings);
        BigDecimal value1 = BigDecimal.valueOf(120.9);
        BigDecimal value2 = operations.getBalance(5).getResult().add(value1);
        BigDecimal value3 = BigDecimal.valueOf(-20);
        assertEquals(0, BigDecimal.valueOf(1).compareTo(operations.putMoney(5, value1).getResult()));
        assertEquals(0, value2.compareTo(operations.getBalance(5).getResult()));
        assertEquals(0, BigDecimal.valueOf(-1).compareTo(operations.putMoney(5, value3).getResult()));
        assertEquals(0, BigDecimal.valueOf(-1).compareTo(fakeOperations.putMoney(5, value1).getResult()));
        assertEquals(0, BigDecimal.valueOf(-1).compareTo(operations.putMoney(500, value1).getResult()));
        Calendar fromD = new GregorianCalendar();
        Calendar endD = new GregorianCalendar();
        endD.roll(Calendar.DAY_OF_YEAR,1);
        OperationHistoryResult result = operations.getOperationList(5, MyUtils.getDateTimeFromDate(fromD.getTime()),
                MyUtils.getDateTimeFromDate(endD.getTime()));
        assertEquals(result.getResult().toArray()[0].toString().substring(0,10),
                MyUtils.getDateTimeFromDate(fromD.getTime()).substring(0,10));
        assertEquals(result.getResult().toArray()[0].toString().substring(20,23), "put");
        assertEquals(result.getResult().toArray()[0].toString().substring(24,29),
                MyUtils.getStringFromBigDecimal(value1));
    }

    @Test
    void getOperationList() {
        String[] dates = {"2023-01-15 18:07:11.000 +0300", "2023-01-16 13:17:33.000 +0300", "2023-01-17 12:27:22.000 +0300",
                "2023-01-18 16:22:26.000 +0300", "2023-02-15 17:03:24.000 +0300"};
        dbCreator.addNewHistoryEntry(7, 1, BigDecimal.valueOf(100), dates[0]);
        dbCreator.addNewHistoryEntry(8, 1, BigDecimal.valueOf(200), dates[1]);
        dbCreator.addNewHistoryEntry(7, 1, BigDecimal.valueOf(300), dates[2]);
        dbCreator.addNewHistoryEntry(7, 2, BigDecimal.valueOf(400), dates[3]);
        dbCreator.addNewHistoryEntry(7, 1, BigDecimal.valueOf(500), dates[4]);
        Operations operations = new Operations(dbCreator.getSettings());
        OperationHistoryResult result = operations.getOperationList(7, "2023-01-01", "2023-01-31");
        assertEquals(result.getResult().size(), 3);
        assertEquals(result.getResult().toArray()[0].toString().substring(0,19), dates[0].substring(0,19));
        assertEquals(result.getResult().toArray()[1].toString().substring(0,19), dates[2].substring(0,19));
        assertEquals(result.getResult().toArray()[2].toString().substring(0,19), dates[3].substring(0,19));
        assertEquals(result.getResult().toArray()[0].toString().substring(20,23), "put");
        assertEquals(result.getResult().toArray()[1].toString().substring(20,23), "put");
        assertEquals(result.getResult().toArray()[2].toString().substring(20,28), "withdraw");
        assertEquals(result.getResult().toArray()[0].toString().substring(24), "100.00");
        assertEquals(result.getResult().toArray()[1].toString().substring(24), "300.00");
        assertEquals(result.getResult().toArray()[2].toString().substring(29), "400.00");
    }

    private static void generateFakeSettings() {
        fakeSettings = new ConnectionSettings();
        fakeSettings.setIp("localhost");
        fakeSettings.setPort("843");
        fakeSettings.setLogin("none");
        fakeSettings.setPassword("zzz");
        fakeSettings.setDatabaseName("testbase");
    }

    private static void fillTestData() {
        String[] balanceValues = {"8500000000", "1000000320.58", "1120", "1185.34", "150", "342.43", "20180", "10012.56"};
        for (String balanceValue : balanceValues) {
            dbCreator.addNewUserToDB(balanceValue);
        }


    }

}