package com.channel.analysis.service;

import com.channel.analysis.dto.ChannelStatsDTO;
import com.channel.analysis.dto.PageResult;
import com.channel.analysis.entity.Channel;
import com.channel.analysis.entity.ChannelStats;
import com.channel.analysis.exception.StatsAccessDeniedException;
import com.channel.analysis.repository.ChannelRepository;
import com.channel.analysis.repository.ChannelStatsRepository;
import com.channel.analysis.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChannelStatsService {

    private static final String CHANNEL_STATUS_ACTIVE = "ACTIVE";

    private final ChannelStatsRepository statsRepository;
    private final ChannelRepository channelRepository;
    private final ReportCacheService reportCacheService;

    public PageResult<ChannelStatsDTO> listStats(Long channelId, int page, int size) {
        checkChannelAccess(channelId);
        Page<ChannelStats> pageData = statsRepository.findByChannelId(channelId,
                PageRequest.of(page - 1, size, Sort.by("statDate").descending()));
        List<ChannelStatsDTO> records = pageData.getContent().stream().map(this::toDTO).collect(Collectors.toList());
        return new PageResult<>(records, pageData.getTotalElements(), page, size);
    }

    public ChannelStatsDTO createStats(ChannelStatsDTO dto) {
        Channel channel = channelRepository.findById(dto.getChannelId())
                .orElseThrow(() -> new RuntimeException("Channel not found"));
        ChannelStats stats = new ChannelStats();
        stats.setChannel(channel);
        stats.setStatDate(dto.getStatDate());
        stats.setImpressions(dto.getImpressions());
        stats.setClicks(dto.getClicks());
        stats.setConversions(dto.getConversions());
        stats.setCost(dto.getCost());
        stats.setRevenue(dto.getRevenue());
        ChannelStatsDTO result = toDTO(statsRepository.save(stats));
        reportCacheService.evictAll();
        return result;
    }

    public ChannelStatsDTO updateStats(Long id, ChannelStatsDTO dto) {
        ChannelStats stats = statsRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Stats not found"));
        stats.setStatDate(dto.getStatDate());
        stats.setImpressions(dto.getImpressions());
        stats.setClicks(dto.getClicks());
        stats.setConversions(dto.getConversions());
        stats.setCost(dto.getCost());
        stats.setRevenue(dto.getRevenue());
        ChannelStatsDTO result = toDTO(statsRepository.save(stats));
        reportCacheService.evictAll();
        return result;
    }

    public void deleteStats(Long id) {
        statsRepository.deleteById(id);
        reportCacheService.evictAll();
    }

    public List<ChannelStatsDTO> getStatsByDateRange(Long channelId, LocalDate start, LocalDate end) {
        checkChannelAccess(channelId);
        return statsRepository.findByChannelIdAndStatDateBetween(channelId, start, end)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    public Map<String, List<ChannelStatsDTO>> getBatchStatsByDateRange(List<Long> channelIds, LocalDate start, LocalDate end) {
        if (channelIds == null || channelIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<Long> accessibleChannelIds = filterAccessibleChannelIds(channelIds);
        if (accessibleChannelIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<ChannelStats> allStats = statsRepository.findByChannelIdsAndDateRange(accessibleChannelIds, start, end);
        Map<Long, List<ChannelStats>> grouped = allStats.stream()
                .collect(Collectors.groupingBy(s -> s.getChannel().getId(), LinkedHashMap::new, Collectors.toList()));
        Map<String, List<ChannelStatsDTO>> result = new LinkedHashMap<>();
        grouped.forEach((channelId, statsList) -> {
            String channelName = statsList.get(0).getChannel().getName();
            result.put(channelName, statsList.stream().map(this::toDTO).collect(Collectors.toList()));
        });
        return result;
    }

    private void checkChannelAccess(Long channelId) {
        if (SecurityUtils.isAdmin()) {
            return;
        }
        Channel channel = channelRepository.findById(channelId)
                .orElseThrow(() -> new RuntimeException("Channel not found"));
        if (!CHANNEL_STATUS_ACTIVE.equals(channel.getStatus())) {
            throw new StatsAccessDeniedException("权限不足：普通用户只能查看活跃渠道的统计数据");
        }
    }

    private List<Long> filterAccessibleChannelIds(List<Long> requestedIds) {
        if (SecurityUtils.isAdmin()) {
            return requestedIds;
        }
        List<Long> activeIds = channelRepository.findIdByStatus(CHANNEL_STATUS_ACTIVE);
        return requestedIds.stream()
                .filter(activeIds::contains)
                .collect(Collectors.toList());
    }

    private ChannelStatsDTO toDTO(ChannelStats stats) {
        ChannelStatsDTO dto = new ChannelStatsDTO();
        dto.setId(stats.getId());
        dto.setChannelId(stats.getChannel().getId());
        dto.setChannelName(stats.getChannel().getName());
        dto.setStatDate(stats.getStatDate());
        dto.setImpressions(stats.getImpressions());
        dto.setClicks(stats.getClicks());
        dto.setConversions(stats.getConversions());
        dto.setCost(stats.getCost());
        dto.setRevenue(stats.getRevenue());
        dto.setCreatedAt(stats.getCreatedAt());
        return dto;
    }
}
