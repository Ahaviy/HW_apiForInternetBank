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
            command = "INSERT INTO public.history_of_operation (balance_id, operation_type, amount) VALUES" +
                    "(" + id + ", 2, " + decimalFormat.format(amount) + ");";
            statement.execute(command);
            statement.close();
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
            command = "INSERT INTO public.history_of_operation (balance_id, operation_type, amount) VALUES" +
                    "(" + id + ", 1, " + decimalFormat.format(amount) + ");";
            statement.execute(command);
            statement.close();
            return new OperationResult(BigDecimal.valueOf(1),"");
        } catch (SQLException e) {
            log.error("произошла ошибка при работе с БД");
            log.error(e.getMessage());
            return new OperationResult(BigDecimal.valueOf(-1), "database error");
        }
    }

/*    public ArrayList<String> getOperationList(int id, String start, String stop) {
        //Проверка корректность данных
        //Выполнение запроса
    }*/

    private boolean isDateTimeValid(String date) {
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

    public void testHistory (int id, BigDecimal amount, int type) {
        try (Connection connection = DriverManager.getConnection(settings.getUrl() + settings.getDatabaseName(),
                settings.getLogin(), settings.getPassword())) {
            String command ="INSERT INTO public.history_of_operation (balance_id, operation_type, amount)";

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}
