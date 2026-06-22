package com.channel.analysis.exception;

import com.channel.analysis.dto.ApiResponse;
import com.channel.analysis.dto.ImportResult;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(StatsAccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleStatsAccessDenied(StatsAccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error(403, ex.getMessage()));
    }

    @ExceptionHandler(BatchImportException.class)
    public ResponseEntity<ApiResponse<ImportResult>> handleBatchImport(BatchImportException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(400, ex.getMessage(), ex.getImportResult()));
    }
}
