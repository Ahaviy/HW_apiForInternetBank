package ru.skillfactory.project;

import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.skillfactory.DemoBaseGenerator;

import java.io.File;
import java.math.BigDecimal;
import java.sql.*;

public class DataBaseCreator {
    private Logger log = LogManager.getLogger(DemoBaseGenerator.class);
    private String settingsFileName;
    private final String DEFAULT_IP = "localhost";
    private final String DEFAULT_PORT = "5432";
    private final String DEFAULT_LOGIN = "postgres";
    private final String DEFAULT_PASSWORD = "test";
    private final String DEFAULT_DATABASE_NAME = "projectbase";
    @Getter
    private ConnectionSettings settings;

    public DataBaseCreator(String settingsFileName) {
        this.settingsFileName = settingsFileName;
        loadOrSetDefaultConnectionSettings();
    }

    private void loadOrSetDefaultConnectionSettings() {
        settings = new ConnectionSettings();
        if ((new File(settingsFileName)).exists()) {
            log.info("найден файл настроек соединения с БД: " + settingsFileName);
            settings = ConnectionSettings.loadSettings(settingsFileName);
        } else {
            log.warn("файл с настройками соединения с БД не найден, взяты значения по умолчанию ");
            settings.setIp(DEFAULT_IP);
            settings.setPort(DEFAULT_PORT);
            settings.setLogin(DEFAULT_LOGIN);
            settings.setPassword(DEFAULT_PASSWORD);
            settings.setDatabaseName(DEFAULT_DATABASE_NAME);
            ConnectionSettings.saveSettings(settingsFileName, settings);
            log.warn("настройки сохранены в " + settingsFileName + ", отредактируйте файл для корректной работы");
        }
    }

    public boolean isDatabaseExists() {
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

    public boolean isValidConnection() {
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

    public void createDB() {
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

    public void addNewUserToDB() {
        String balance = MyUtils.getStringFromBigDecimal(BigDecimal.valueOf(40000 + Math.random() * 10000));
        try (Connection connection = DriverManager.getConnection(settings.getUrl() + settings.getDatabaseName(),
                settings.getLogin(), settings.getPassword())) {
            Statement statement = connection.createStatement();
            String command = "INSERT INTO public.balance (amount) values (" + balance + ")";
            statement.execute(command);
        } catch (SQLException e) {
            log.warn("произошла ошибка при добавлении нового пользователя в БД");
            log.warn(e.getMessage());
        }
    }

    public void addNewHistoryEntry(int userId, int opetationType, BigDecimal amount, String datetime) {
        try (Connection connection = DriverManager.getConnection(settings.getUrl() + settings.getDatabaseName(),
                settings.getLogin(), settings.getPassword())) {
            Statement statement = connection.createStatement();
            String command = "INSERT INTO public.history_of_operation (balance_id, operation_type, amount, datetime) VALUES" +
                    "(" + userId + ", " + opetationType + ", " + MyUtils.getStringFromBigDecimal(amount) + ", " + datetime + ");";
            System.out.println(command);
            statement.execute(command);
            statement.close();
        } catch (SQLException e) {
            log.error("произошла ошибка при работе с БД");
            log.error(e.getMessage());
        }
    }

    public void deleteDB() {
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
