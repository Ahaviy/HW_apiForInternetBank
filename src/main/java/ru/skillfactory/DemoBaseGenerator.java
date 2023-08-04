package ru.skillfactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.skillfactory.project.ConnectionSettings;

import java.io.File;
import java.sql.*;


public class DemoBaseGenerator {

    private static Logger log = LogManager.getLogger(DemoBaseGenerator.class);

    private static final String DEFAULT_IP = "localhost";
    private static final String DEFAULT_PORT = "5432";
    private static final String DEFAULT_LOGIN = "postgres";
    private static final String DEFAULT_PASSWORD = "test";
    private static final String DEFAULT_DATABASE_NAME = "projectbase";
    private static final String SETTINGS_FILE_NAME = "./src/test/resources/settings.xml";

    private static ConnectionSettings settings;

    public static void main(String[] args) {


        GenerateDemoDB();
    }

    private static void GenerateDemoDB() {
        loadOrSetDefaultConnectionSettings();
        if (isValidConnection()) {
            if (isDatabaseExists()) {
                log.info("БД с именем: " + settings.getDatabaseName() + " уже создана. ");
            } else {
                log.info("БД " + settings.getDatabaseName() + " не найдена, будет создана новая база");
                createDB();
                FillDB();
            }
        }

    }

    private static void FillDB() {
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
            statement.close();
            log.info("БД успешно создана");
        } catch (SQLException e) {
            log.error("произошла ошибка при работе с БД");
            log.error(e.getMessage());
        }
    }

    
}
