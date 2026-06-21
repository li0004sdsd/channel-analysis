package com.channel.analysis.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ChannelTypeReportDTO {
    private String channelType;
    private Long totalImpressions;
    private Long totalClicks;
    private Long totalConversions;
    private BigDecimal totalCost;
    private BigDecimal totalRevenue;
    private BigDecimal ctr;
    private BigDecimal cvr;
    private BigDecimal roi;
    private BigDecimal cpc;
    private BigDecimal cpa;
}
