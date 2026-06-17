package com.channel.analysis.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class ChannelStatsDTO {
    private Long id;
    private Long channelId;
    private String channelName;
    private LocalDate statDate;
    private Long impressions;
    private Long clicks;
    private Long conversions;
    private BigDecimal cost;
    private BigDecimal revenue;
    private LocalDateTime createdAt;
}
