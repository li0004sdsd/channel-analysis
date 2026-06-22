package com.channel.analysis.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ChannelDTO {
    private Long id;
    @NotBlank
    private String name;
    private String type;
    private String description;
    private BigDecimal budget;
    private BigDecimal monthlyBudget;
    private BigDecimal alertRatio;
    private BigDecimal monthlySpent;
    private BigDecimal budgetUtilizationRate;
    private String budgetStatus;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
