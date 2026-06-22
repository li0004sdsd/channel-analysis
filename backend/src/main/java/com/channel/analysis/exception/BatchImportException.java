package com.channel.analysis.exception;

import com.channel.analysis.dto.ImportResult;
import lombok.Getter;

@Getter
public class BatchImportException extends RuntimeException {

    private final ImportResult importResult;

    public BatchImportException(ImportResult importResult) {
        super(importResult.getMessage());
        this.importResult = importResult;
    }
}
