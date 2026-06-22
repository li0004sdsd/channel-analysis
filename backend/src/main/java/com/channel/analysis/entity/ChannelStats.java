package com.channel.analysis.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "channel_stats", uniqueConstraints = @UniqueConstraint(columnNames = {"channel_id", "stat_date"}))
public class ChannelStats {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "channel_id", nullable = false)
    private Channel channel;

    @Column(name = "stat_date", nullable = false)
    private LocalDate statDate;

    @Column(name = "impressions")
    private Long impressions = 0L;

    @Column(name = "clicks")
    private Long clicks = 0L;

    @Column(name = "conversions")
    private Long conversions = 0L;

    @Column(name = "cost", precision = 15, scale = 2)
    private BigDecimal cost = BigDecimal.ZERO;

    @Column(name = "revenue", precision = 15, scale = 2)
    private BigDecimal revenue = BigDecimal.ZERO;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}
