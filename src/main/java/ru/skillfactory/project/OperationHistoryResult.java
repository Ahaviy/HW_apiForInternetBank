package ru.skillfactory.project;

import lombok.Getter;

import java.util.ArrayList;

public class OperationHistoryResult {
    @Getter
    private ArrayList<String> result;

    @Getter
    private String errorMessage;

    public OperationHistoryResult(String errorMessage) {
        result = new ArrayList<>();
        this.errorMessage = errorMessage;
    }

    public OperationHistoryResult() {
        result = new ArrayList<>();
        errorMessage = "";
    }
}
