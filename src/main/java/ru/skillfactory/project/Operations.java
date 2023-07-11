package ru.skillfactory.project;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.math.BigDecimal;
import java.sql.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

public class Operations {

    private final Logger log = LogManager.getLogger(Operations.class);
    private final ConnectionSettings settings;

    public Operations(ConnectionSettings settings) {
        this.settings = settings;
    }

    public OperationResult getBalance(int id) {
        try (Connection connection = DriverManager.getConnection(settings.getUrl() + settings.getDatabaseName(),
                settings.getLogin(), settings.getPassword())) {
            Statement statement = connection.createStatement();
            String command = "select amount from public.balance where id=" + id;
            statement.executeQuery(command);
            ResultSet resultSet = statement.getResultSet();
            if (resultSet.next()) {
                return new OperationResult(resultSet.getBigDecimal(1), "");
            } else {
                return new OperationResult(BigDecimal.valueOf(-1), "not found id=" + id);
            }
        } catch (SQLException e) {
            log.warn("произошла ошибка при работе с БД");
            log.warn(e.getMessage());
            return new OperationResult(BigDecimal.valueOf(-1), "database error");
        }
    }

    public OperationResult takeMoney(int id, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.valueOf(0)) < 0) {
            return new OperationResult(BigDecimal.valueOf(-1), "incorrect amount");
        }
        OperationResult operationResult = getBalance(id);
        BigDecimal currentAmount = operationResult.getResult();
        if (currentAmount.compareTo(BigDecimal.valueOf(-1)) == 0) {
            if(operationResult.errorMessage.startsWith("not found id")){
                return new OperationResult(BigDecimal.valueOf(-1), "not found id=" + id);
            }
            return new OperationResult(BigDecimal.valueOf(-1), "database error");
        }
        currentAmount = currentAmount.subtract(amount);
        if (currentAmount.compareTo(BigDecimal.valueOf(0)) < 0) {
            return new OperationResult(BigDecimal.valueOf(0), "");
        }
        Locale locale = new Locale("en", "UK");
        String pattern = "###.##";
        DecimalFormat decimalFormat = (DecimalFormat) NumberFormat.getNumberInstance(locale);
        decimalFormat.applyPattern(pattern);
        try (Connection connection = DriverManager.getConnection(settings.getUrl() + settings.getDatabaseName(),
                settings.getLogin(), settings.getPassword())) {
            Statement statement = connection.createStatement();
            String command = "update public.balance set amount= " + decimalFormat.format(currentAmount) + " where id=" + id;
            statement.execute(command);
            return new OperationResult(BigDecimal.valueOf(1),"");
        } catch (SQLException e) {
            log.error("произошла ошибка при работе с БД");
            log.error(e.getMessage());
            return new OperationResult(BigDecimal.valueOf(-1), "database error");
        }
    }

    public OperationResult putMoney(int id, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.valueOf(0)) < 0) {
            return new OperationResult(BigDecimal.valueOf(-1), "incorrect amount");
        }
        OperationResult operationResult = getBalance(id);
        BigDecimal currentAmount = operationResult.getResult();
        if (currentAmount.compareTo(BigDecimal.valueOf(-1)) == 0) {
            if(operationResult.errorMessage.startsWith("not found id")){
                return new OperationResult(BigDecimal.valueOf(-1), "not found id=" + id);
            }
            return new OperationResult(BigDecimal.valueOf(-1), "database error");
        }
        currentAmount = currentAmount.add(amount);
        Locale locale = new Locale("en", "UK");
        String pattern = "###.##";
        DecimalFormat decimalFormat = (DecimalFormat) NumberFormat.getNumberInstance(locale);
        decimalFormat.applyPattern(pattern);
        try (Connection connection = DriverManager.getConnection(settings.getUrl() + settings.getDatabaseName(),
                settings.getLogin(), settings.getPassword())) {
            Statement statement = connection.createStatement();
            String command = "update public.balance set amount= " + decimalFormat.format(currentAmount) + " where id=" + id;
            statement.execute(command);
            return new OperationResult(BigDecimal.valueOf(1),"");
        } catch (SQLException e) {
            log.error("произошла ошибка при работе с БД");
            log.error(e.getMessage());
            return new OperationResult(BigDecimal.valueOf(-1), "database error");
        }
    }

}