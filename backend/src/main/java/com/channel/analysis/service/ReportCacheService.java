package com.channel.analysis.service;

import com.channel.analysis.entity.ChannelStats;
import com.channel.analysis.repository.ChannelStatsRepository;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class ReportCacheService implements ReportCacheOperations {

    private static final long CACHE_TTL_MINUTES = 5L;

    private final Cache<String, List<ChannelStats>> cache;
    private final ChannelStatsRepository statsRepository;

    public ReportCacheService(ChannelStatsRepository statsRepository) {
        this.statsRepository = statsRepository;
        this.cache = Caffeine.newBuilder()
                .expireAfterWrite(CACHE_TTL_MINUTES, TimeUnit.MINUTES)
                .maximumSize(500)
                .build();
    }

    public List<ChannelStats> getStatsByDateRange(LocalDate start, LocalDate end) {
        String key = buildKey(start, end);
        return cache.get(key, k -> statsRepository.findByDateRange(start, end));
    }

    public void evict(LocalDate start, LocalDate end) {
        cache.invalidate(buildKey(start, end));
    }

    public void evictAll() {
        cache.invalidateAll();
    }

    private String buildKey(LocalDate start, LocalDate end) {
        return start.toString() + "_" + end.toString();
    }
}
