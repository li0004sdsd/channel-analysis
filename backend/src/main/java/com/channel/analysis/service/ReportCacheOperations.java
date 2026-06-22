package com.channel.analysis.service;

import com.channel.analysis.entity.ChannelStats;
import java.time.LocalDate;
import java.util.List;

public interface ReportCacheOperations {

    List<ChannelStats> getStatsByDateRange(LocalDate start, LocalDate end);

    void evict(LocalDate start, LocalDate end);

    void evictAll();
}
