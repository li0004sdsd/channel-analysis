package com.channel.analysis.service;

import com.channel.analysis.dto.ChannelStatsDTO;
import com.channel.analysis.dto.PageResult;
import com.channel.analysis.entity.Channel;
import com.channel.analysis.entity.ChannelStats;
import com.channel.analysis.repository.ChannelRepository;
import com.channel.analysis.repository.ChannelStatsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChannelStatsService {

    private final ChannelStatsRepository statsRepository;
    private final ChannelRepository channelRepository;

    public PageResult<ChannelStatsDTO> listStats(Long channelId, int page, int size) {
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
        return toDTO(statsRepository.save(stats));
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
        return toDTO(statsRepository.save(stats));
    }

    public void deleteStats(Long id) {
        statsRepository.deleteById(id);
    }

    public List<ChannelStatsDTO> getStatsByDateRange(Long channelId, LocalDate start, LocalDate end) {
        return statsRepository.findByChannelIdAndStatDateBetween(channelId, start, end)
                .stream().map(this::toDTO).collect(Collectors.toList());
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
