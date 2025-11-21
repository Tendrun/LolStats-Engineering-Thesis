package com.backendwebsite.DatabaseBuilder.DTO.AppApi.Summary;

import com.backendwebsite.DatabaseBuilder.Step.Log.StepLog;
import com.backendwebsite.DatabaseBuilder.Step.StepsOrder;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public record StepLogSummary(
        String stepName,
        long averageExecutionTimeMs,
        long totalExecutionTimeMs,
        int successCount,
        int failedCount
) {
    public static StepLogSummary fromStepLogs(String stepName, List<StepLog> logs) {
        long totalExecutionTime = logs.stream()
                .mapToLong(StepLog::executionTimeMs)
                .sum();

        long averageExecutionTime = logs.isEmpty() ? 0 : totalExecutionTime / logs.size();

        int successCount = (int) logs.stream()
                .filter(log -> log.requestStatus() == StepsOrder.RequestStatus.SUCCESSFUL)
                .count();

        int failedCount = (int) logs.stream()
                .filter(log -> log.requestStatus() == StepsOrder.RequestStatus.FAILED)
                .count();


        return new StepLogSummary(
                stepName,
                averageExecutionTime,
                totalExecutionTime,
                successCount,
                failedCount
        );
    }
}