package ru.skillfactory.project;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.math.BigDecimal;
import java.sql.*;


import static org.junit.jupiter.api.Assertions.assertEquals;

class OperationsTest {

    private static Logger log = LogManager.getLogger(OperationsTest.class);
    private static boolean isDBWasCreated;

    private static final String DEFAULT_IP = "localhost";
    private static final String DEFAULT_PORT = "5432";
    private static final String DEFAULT_LOGIN = "postgres";
    private static final String DEFAULT_PASSWORD = "test";
    private static final String DEFAULT_DATABASE_NAME = "testbase";
    private static final String SETTINGS_FILE_NAME = "./src/test/resources/settings.xml";

    private static ConnectionSettings settings;
    private static ConnectionSettings fakeSettings;


    @BeforeAll
    static void setUp() {
        loadOrSetDefaultConnectionSettings();
        if (isValidConnection()) {
            if (isDatabaseExists()) {
                log.info("найдена БД для тестов:" + settings.getDatabaseName());
                isDBWasCreated = false;
            } else {
                log.info("БД " + settings.getDatabaseName() + " не найдена, будет создана новая база");
                createDB();
                isDBWasCreated = true;
            }
        }
        generateFakeSettings();
    }

    @AfterAll
    static void tearDown() {
        /*if (isDBWasCreated) {
            deleteDB();
            log.info("созданая ранее БД для теста была удалена");
        }*/
    }

    @Test
    void getBalance() {
        Operations operations = new Operations(settings);
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
        Operations operations = new Operations(settings);
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
    }

    @Test
    void putMoney() {
        Operations operations = new Operations(settings);
        Operations fakeOperations = new Operations(fakeSettings);
        BigDecimal value1 = BigDecimal.valueOf(120.9);
        BigDecimal value2 = operations.getBalance(5).getResult().add(value1);
        BigDecimal value3 = BigDecimal.valueOf(-20);
        assertEquals(0, BigDecimal.valueOf(1).compareTo(operations.putMoney(5, value1).getResult()));
        assertEquals(0, value2.compareTo(operations.getBalance(5).getResult()));
        //проверка
        assertEquals(0, BigDecimal.valueOf(-1).compareTo(operations.putMoney(5, value3).getResult()));
        assertEquals(0, BigDecimal.valueOf(-1).compareTo(fakeOperations.putMoney(5, value1).getResult()));
        assertEquals(0, BigDecimal.valueOf(-1).compareTo(operations.putMoney(500, value1).getResult()));
    }

    private static void loadOrSetDefaultConnectionSettings() {
        settings = new ConnectionSettings();
        if ((new File(SETTINGS_FILE_NAME)).exists()) {
            log.info("найден файл настроек соединения с БД: " + SETTINGS_FILE_NAME);
            settings = ConnectionSettings.loadSettings(SETTINGS_FILE_NAME);
        } else {
            log.warn("файл с настройками соединения с БД не найден, взяты значения по умолчанию ");
            settings.setIp(DEFAULT_IP);
            settings.setPort(DEFAULT_PORT);
            settings.setLogin(DEFAULT_LOGIN);
            settings.setPassword(DEFAULT_PASSWORD);
            settings.setDatabaseName(DEFAULT_DATABASE_NAME);
            ConnectionSettings.saveSettings(SETTINGS_FILE_NAME, settings);
            log.warn("настройки сохранены в " + SETTINGS_FILE_NAME + ", отредактируйте файл для корректной работы");
        }
    }

    private static void generateFakeSettings() {
        fakeSettings = new ConnectionSettings();
        fakeSettings.setIp(DEFAULT_IP);
        fakeSettings.setPort("843");
        fakeSettings.setLogin("none");
        fakeSettings.setPassword(DEFAULT_PASSWORD);
        fakeSettings.setDatabaseName(DEFAULT_DATABASE_NAME);
    }

    private static boolean isValidConnection() {
        boolean result = false;
        try (Connection connection = DriverManager.getConnection(settings.getUrl(), settings.getLogin(), settings.getPassword())) {
            if (connection != null) {
                result = true;
                log.info("соединение с БД успешно установлено");
            }
        } catch (SQLException e) {
            log.error("ошибка соединения с БД");
            log.error(e.getMessage());
        }
        return result;
    }

    private static boolean isDatabaseExists() {
        Boolean isExists = false;
        try (Connection connection = DriverManager.getConnection(settings.getUrl(), settings.getLogin(), settings.getPassword())) {

            Statement statement = connection.createStatement();
            ResultSet result = statement.executeQuery("select datname from pg_database");

            while (result.next()) {
                if (result.getString(1).equals(settings.getDatabaseName())) {
                    isExists = true;
                    break;
                }
            }
            statement.close();
        } catch (SQLException e) {
            log.error("произошла ошибка при работе с БД");
            log.error(e.getMessage());
        }
        return isExists;
    }

    private static void createDB() {
        try (Connection connection = DriverManager.getConnection(settings.getUrl(), settings.getLogin(), settings.getPassword())) {
            String command = "CREATE DATABASE " + settings.getDatabaseName() + " WITH OWNER = " + settings.getLogin()
                    + " ENCODING = 'UTF8' CONNECTION LIMIT = -1 IS_TEMPLATE = False;";
            Statement statement = connection.createStatement();
            statement.execute(command);
            statement.close();
        } catch (SQLException e) {
            log.error("произошла ошибка при работе с БД");
            log.error(e.getMessage());
        }
        try (Connection connection = DriverManager.getConnection(settings.getUrl() + settings.getDatabaseName(),
                settings.getLogin(), settings.getPassword())) {
            Statement statement = connection.createStatement();
            String command = "CREATE TABLE public.balance\n" +
                    "(\n" +
                    "\tid SERIAL PRIMARY KEY,\n" +
                    "\tamount numeric(14, 2)\n" +
                    ");";
            statement.execute(command);
            command = "CREATE TABLE public.operation_type\n" +
                    "(\n" +
                    "\tid SERIAL PRIMARY KEY,\n" +
                    "\toperation_name VARCHAR(10) NOT NULL\n" +
                    ");";
            statement.execute(command);
            command = "CREATE TABLE public.history_of_operation\n" +
                    "(\n" +
                    "\tid SERIAL PRIMARY KEY,\n" +
                    "\tbalance_id integer NOT NULL,\n" +
                    "\toperation_type integer NOT NULL,\n" +
                    "\tamount numeric(14, 2) NOT NULL,\n" +
                    "\tdatetime TIMESTAMPTZ DEFAULT NOW(),\n" +
                    "\tCONSTRAINT type_id FOREIGN KEY (operation_type)\n" +
                    "\t\tREFERENCES public.operation_type (id),\n" +
                    "\tCONSTRAINT balance_id FOREIGN KEY (balance_id)\n" +
                    "\tREFERENCES public.balance (id) \n" +
                    ");";
            statement.execute(command);
            command = "insert into public.operation_type (operation_name) values\n" +
                    "\t('put'),\n" +
                    "\t('withdraw')";
            statement.execute(command);
            command = "INSERT INTO public.balance (id, amount) VALUES\n" +
                    "(1, 8500000000),\n" +
                    "(2, 1000000320.58),\n" +
                    "(3, 1120),\n" +
                    "(4, 1185.34),\n" +
                    "(5, 150),\n" +
                    "(6, 342.43);";
            statement.execute(command);
            statement.close();
            log.info("БД успешно создана");
        } catch (SQLException e) {
            log.error("произошла ошибка при работе с БД");
            log.error(e.getMessage());
        }
    }

    private static void deleteDB() {
        try (Connection connection = DriverManager.getConnection(settings.getUrl(), settings.getLogin(), settings.getPassword())) {
            String command = "DROP DATABASE " + settings.getDatabaseName();
            Statement statement = connection.createStatement();
            statement.execute(command);
            statement.close();
        } catch (SQLException e) {
            log.error("произошла ошибка при удалении БД");
            log.error(e.getMessage());
        }
    }


}