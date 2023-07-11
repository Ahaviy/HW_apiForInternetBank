package ru.skillfactory.project;

import lombok.Getter;

import java.math.BigDecimal;

public class OperationResult {

    @Getter
    private BigDecimal result;
    @Getter
    String errorMessage;

    public OperationResult(BigDecimal result, String errorMessage) {
        this.result = result;
        this.errorMessage = errorMessage;
    }

    public OperationResult(BigDecimal result) {
        this.result = result;
        errorMessage = null;
    }

}
