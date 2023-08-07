package ru.skillfactory.project;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.math.BigDecimal;
import java.sql.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
            if (operationResult.errorMessage.startsWith("not found id")) {
                return new OperationResult(BigDecimal.valueOf(-1), "not found id=" + id);
            }
            return new OperationResult(BigDecimal.valueOf(-1), "database error");
        }
        currentAmount = currentAmount.subtract(amount);
        if (currentAmount.compareTo(BigDecimal.valueOf(0)) < 0) {
            return new OperationResult(BigDecimal.valueOf(0), "");
        }
        try (Connection connection = DriverManager.getConnection(settings.getUrl() + settings.getDatabaseName(),
                settings.getLogin(), settings.getPassword())) {
            Statement statement = connection.createStatement();
            String command = "update public.balance set amount= " + MyUtils.getStringFromBigDecimal(currentAmount) + " where id=" + id;
            statement.execute(command);
            command = "INSERT INTO public.history_of_operation (balance_id, operation_type, amount) VALUES" +
                    "(" + id + ", 2, " + MyUtils.getStringFromBigDecimal(amount) + ");";
            statement.execute(command);
            statement.close();
            return new OperationResult(BigDecimal.valueOf(1), "");
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
            if (operationResult.errorMessage.startsWith("not found id")) {
                return new OperationResult(BigDecimal.valueOf(-1), "not found id=" + id);
            }
            return new OperationResult(BigDecimal.valueOf(-1), "database error");
        }
        currentAmount = currentAmount.add(amount);
        try (Connection connection = DriverManager.getConnection(settings.getUrl() + settings.getDatabaseName(),
                settings.getLogin(), settings.getPassword())) {
            Statement statement = connection.createStatement();
            String command = "update public.balance set amount= " + MyUtils.getStringFromBigDecimal(currentAmount)+ " where id=" + id;
            statement.execute(command);
            command = "INSERT INTO public.history_of_operation (balance_id, operation_type, amount) VALUES" +
                    "(" + id + ", 1, " + MyUtils.getStringFromBigDecimal(amount) + ");";
            statement.execute(command);
            statement.close();
            return new OperationResult(BigDecimal.valueOf(1), "");
        } catch (SQLException e) {
            log.error("произошла ошибка при работе с БД");
            log.error(e.getMessage());
            return new OperationResult(BigDecimal.valueOf(-1), "database error");
        }
    }

    public OperationHistoryResult getOperationList(int id, String fromDate, String endDate) {
        if (!isIdExists(id)) {
            log.warn("не найден пользовательс с id = " + id);
            return new OperationHistoryResult("not found id=" + id);
        }
        fromDate = fromDate.substring(0,10);
        endDate = endDate.substring(0,10);
        String defaultFromDate = "2000-01-01";
        if (fromDate.isEmpty()) fromDate = defaultFromDate;
        if (endDate.isEmpty()) {
            endDate = MyUtils.getDateTimeFromDate((new GregorianCalendar()).getTime()).substring(0, 10);
        }
        if (MyUtils.isDateTimeValid(fromDate) && MyUtils.isDateTimeValid(endDate)) {
            try (Connection connection = DriverManager.getConnection(settings.getUrl() + settings.getDatabaseName(),
                    settings.getLogin(), settings.getPassword())) {
                Statement statement = connection.createStatement();
                String command = "select datetime, operation_name, amount from public.history_of_operation join " +
                        "public.operation_type on public.history_of_operation.operation_type = public.operation_type.id " +
                        "where balance_id = " + id + " and datetime between '" + fromDate + "' and '" + endDate + "'";
                statement.executeQuery(command);
                ResultSet resultSet = statement.getResultSet();
                OperationHistoryResult result = new OperationHistoryResult();
                while (resultSet.next()) {
                    result.getResult().add(resultSet.getString(1).substring(0,19) + " " + resultSet.getString(2) + " "
                            + resultSet.getString(3));
                }
                return result;


            } catch (SQLException e) {
                log.error("произошла ошибка при работе с БД");
                log.error(e.getMessage());
                return new OperationHistoryResult("database error");
            }

        } else {
            log.warn("Не корректные параметры даты");
            return new OperationHistoryResult("incorect parametr");
        }


        //Проверка корректность данных

        //Выполнение запроса
    }

    public OperationResult transferMoney(int senderID, int recipientID, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.valueOf(0)) < 0) {
            return new OperationResult(BigDecimal.valueOf(0), "incorrect amount");
        }
        if (!isIdExists(senderID)) {
            return new OperationResult(BigDecimal.valueOf(0), "not found sender id");
        }
        if (!isIdExists(recipientID)) {
            return new OperationResult(BigDecimal.valueOf(0), "not found recipient id");
        }
        if (senderID == recipientID) {
            return new OperationResult(BigDecimal.valueOf(0), "senderID = recipientID");
        }
        OperationResult operationResult = getBalance(senderID);
        BigDecimal currentAmount = operationResult.getResult();
        if (currentAmount.compareTo(amount) < 0 ) {
            return new OperationResult(BigDecimal.valueOf(0), "not enough sender balance");
        }
        BigDecimal senderCurrentAmount = currentAmount.subtract(amount);
        operationResult = getBalance(recipientID);
        BigDecimal recipienCurrentAmount = operationResult.getResult().add(amount);
        try (Connection connection = DriverManager.getConnection(settings.getUrl() + settings.getDatabaseName(),
                settings.getLogin(), settings.getPassword())) {
            Statement statement = connection.createStatement();
            String command = "update public.balance set amount= " + MyUtils.getStringFromBigDecimal(senderCurrentAmount) + " where id=" + senderID;
            statement.execute(command);
            command = "INSERT INTO public.history_of_operation (balance_id, operation_type, amount) VALUES" +
                    "(" + senderID + ", 2, " + MyUtils.getStringFromBigDecimal(amount) + ");";
            statement.execute(command);
            command = "update public.balance set amount= " + MyUtils.getStringFromBigDecimal(recipienCurrentAmount) + " where id=" + recipientID;
            statement.execute(command);
            command = "INSERT INTO public.history_of_operation (balance_id, operation_type, amount) VALUES" +
                    "(" + recipientID + ", 1, " + MyUtils.getStringFromBigDecimal(amount)+ ");";
            statement.execute(command);
            statement.close();
            return new OperationResult(BigDecimal.valueOf(1), "");
        } catch (SQLException e) {
            log.error("произошла ошибка при работе с БД");
            log.error(e.getMessage());
            return new OperationResult(BigDecimal.valueOf(0), "database error");
        }


    }

    private boolean isIdExists(int id) {
        OperationResult operationResult = getBalance(id);
        BigDecimal currentAmount = operationResult.getResult();
        if (currentAmount.compareTo(BigDecimal.valueOf(-1)) == 0) {
            if (operationResult.errorMessage.startsWith("not found id")) {
                return false;
            }
            return false;
        }
        return true;

    }


}
