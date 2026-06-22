package com.channel.analysis.dto;

import lombok.Data;
import java.util.List;

@Data
public class ImportResult {
    private int totalRows;
    private int successCount;
    private int skippedCount;
    private int failedCount;
    private String message;
    private List<ImportRowResult> rows;

    public static ImportResult of(List<ImportRowResult> rows, boolean rolledBack) {
        ImportResult result = new ImportResult();
        result.setRows(rows);
        result.setTotalRows(rows.size());
        result.setSuccessCount((int) rows.stream().filter(r -> "SUCCESS".equals(r.getStatus())).count());
        result.setSkippedCount((int) rows.stream().filter(r -> "SKIPPED".equals(r.getStatus())).count());
        result.setFailedCount((int) rows.stream().filter(r -> "FAILED".equals(r.getStatus())).count());
        if (rolledBack) {
            result.setMessage("校验失败率超过20%，整批回滚。失败数: " + result.getFailedCount() + "/" + result.getTotalRows());
        } else {
            result.setMessage("导入完成。成功: " + result.getSuccessCount() + "，跳过(重复): " + result.getSkippedCount() + "，失败: " + result.getFailedCount());
        }
        return result;
    }
}
