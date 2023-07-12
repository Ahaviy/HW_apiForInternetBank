package ru.skillfactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import ru.skillfactory.project.ConnectionSettings;


import java.io.File;
import java.sql.*;

@SpringBootApplication
public class App {

    private static final Logger log = LogManager.getLogger(App.class);

    private static final String DEFAULT_IP = "localhost";
    private static final String DEFAULT_PORT = "5432";
    private static final String DEFAULT_LOGIN = "postgres";
    private static final String DEFAULT_PASSWORD = "test";
    private static final String DEFAULT_DATABASE_NAME = "projectbase";
    private static final String SETTINGS_FILE_NAME = "./src/main/resources/settings.xml";

    static private ConnectionSettings settings;
    static private boolean isValidConnection;

    public static ConnectionSettings getSettings() {
        return settings;
    }

    public static void main(String[] args) {
        loadOrSetDefaultConnectionSettings();
        if (checkValidConnection()) {
            SpringApplication.run(App.class, args);
        } else {
            log.error("Нет соединения с БД, дальнейшая работа невозможна");
        }
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

    private static boolean checkValidConnection() {
        boolean result = false;
        try (Connection connection = DriverManager.getConnection(settings.getUrl() + settings.getDatabaseName(),
                settings.getLogin(), settings.getPassword())) {
            if (connection != null) {
                result = true;
                log.info("тестовое соединение с БД успешно установлено");
            }
        } catch (SQLException e) {
            log.error("ошибка соединения с БД");
            log.error(e.getMessage());
        }
        return result;
    }

}
