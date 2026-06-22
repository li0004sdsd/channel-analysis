package com.channel.analysis.dto;

import lombok.Data;

@Data
public class ImportRowResult {
    private int rowNumber;
    private Long channelId;
    private String statDate;
    private String status;
    private String reason;

    public static ImportRowResult success(int rowNumber, Long channelId, String statDate) {
        ImportRowResult r = new ImportRowResult();
        r.setRowNumber(rowNumber);
        r.setChannelId(channelId);
        r.setStatDate(statDate);
        r.setStatus("SUCCESS");
        return r;
    }

    public static ImportRowResult skipped(int rowNumber, Long channelId, String statDate) {
        ImportRowResult r = new ImportRowResult();
        r.setRowNumber(rowNumber);
        r.setChannelId(channelId);
        r.setStatDate(statDate);
        r.setStatus("SKIPPED");
        r.setReason("该渠道该日期的数据已存在");
        return r;
    }

    public static ImportRowResult failed(int rowNumber, Long channelId, String statDate, String reason) {
        ImportRowResult r = new ImportRowResult();
        r.setRowNumber(rowNumber);
        r.setChannelId(channelId);
        r.setStatDate(statDate);
        r.setStatus("FAILED");
        r.setReason(reason);
        return r;
    }
}
