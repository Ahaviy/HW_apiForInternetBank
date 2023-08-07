package ru.skillfactory.controlers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.skillfactory.App;
import ru.skillfactory.project.OperationHistoryResult;
import ru.skillfactory.project.OperationResult;
import ru.skillfactory.project.Operations;

import java.math.BigDecimal;

@RestController
public class BankApiController {

    @RequestMapping("/getBalance")
    public OperationResult getBalance(@RequestParam(value = "id", required = true) int id) {
        Operations operations = new Operations(App.getSettings());
        return operations.getBalance(id);
    }

    @RequestMapping("/putMoney")
    public OperationResult putMoney(@RequestParam(value = "id", required = true) int id,
                                    @RequestParam(value = "amount", required = true) double amount){
        Operations operations = new Operations(App.getSettings());
        return operations.putMoney(id, BigDecimal.valueOf(amount));
    }

    @RequestMapping("/takeMoney")
    public OperationResult takeMoney(@RequestParam(value = "id", required = true) int id,
                                     @RequestParam(value = "amount", required = true) double amount){
        Operations operations = new Operations(App.getSettings());
        return operations.takeMoney(id, BigDecimal.valueOf(amount));
    }

    @RequestMapping("/getOperationList")
    public OperationHistoryResult getOperationList(@RequestParam(value = "id", required = true) int id,
                                                   @RequestParam(value = "fromDate", required =false, defaultValue = "") String fromDate,
                                                   @RequestParam(value = "endDate", required =false, defaultValue = "") String endDate) {
        Operations operations = new Operations(App.getSettings());
        return operations.getOperationList(id, fromDate, endDate);
    }



}
