package com.channel.analysis.service;

import com.channel.analysis.common.BudgetStatus;
import com.channel.analysis.dto.ChannelDTO;
import com.channel.analysis.dto.PageResult;
import com.channel.analysis.entity.Channel;
import com.channel.analysis.repository.ChannelRepository;
import com.channel.analysis.repository.ChannelStatsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChannelService {

    private final ChannelRepository channelRepository;
    private final ChannelStatsRepository channelStatsRepository;

    public PageResult<ChannelDTO> listChannels(int page, int size, String name, String status) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("createdAt").descending());
        Page<Channel> pageData;
        boolean hasName = name != null && !name.isBlank();
        boolean hasStatus = status != null && !status.isBlank();
        if (hasName && hasStatus) {
            pageData = channelRepository.findByNameContainingIgnoreCaseAndStatus(name, status, pageable);
        } else if (hasName) {
            pageData = channelRepository.findByNameContainingIgnoreCase(name, pageable);
        } else if (hasStatus) {
            pageData = channelRepository.findByStatus(status, pageable);
        } else {
            pageData = channelRepository.findAll(pageable);
        }
        List<Channel> channels = pageData.getContent();
        Map<Long, BigDecimal> spentMap = resolveMonthlySpentForChannels(channels);
        List<ChannelDTO> records = channels.stream()
                .map(ch -> toDTO(ch, spentMap.get(ch.getId())))
                .collect(Collectors.toList());
        return new PageResult<>(records, pageData.getTotalElements(), page, size);
    }

    public ChannelDTO getChannel(Long id) {
        Channel channel = channelRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Channel not found: " + id));
        BigDecimal spent = resolveMonthlySpent(channel);
        return toDTO(channel, spent);
    }

    @Transactional
    public ChannelDTO createChannel(ChannelDTO dto) {
        Channel channel = new Channel();
        channel.setName(dto.getName());
        channel.setType(dto.getType());
        channel.setDescription(dto.getDescription());
        channel.setBudget(dto.getBudget());
        channel.setMonthlyBudget(dto.getMonthlyBudget());
        channel.setAlertRatio(dto.getAlertRatio() != null ? dto.getAlertRatio() : new BigDecimal("0.80"));
        channel.setStatus(dto.getStatus() != null ? dto.getStatus() : "ACTIVE");
        channel.setCreatedAt(LocalDateTime.now());
        channel.setUpdatedAt(LocalDateTime.now());
        Channel saved = channelRepository.save(channel);
        BigDecimal spent = resolveMonthlySpent(saved);
        return toDTO(saved, spent);
    }

    @Transactional
    public ChannelDTO updateChannel(Long id, ChannelDTO dto) {
        Channel channel = channelRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Channel not found: " + id));
        channel.setName(dto.getName());
        channel.setType(dto.getType());
        channel.setDescription(dto.getDescription());
        channel.setBudget(dto.getBudget());
        channel.setMonthlyBudget(dto.getMonthlyBudget());
        if (dto.getAlertRatio() != null) {
            channel.setAlertRatio(dto.getAlertRatio());
        }
        channel.setStatus(dto.getStatus());
        channel.setUpdatedAt(LocalDateTime.now());
        Channel saved = channelRepository.save(channel);
        BigDecimal spent = resolveMonthlySpent(saved);
        return toDTO(saved, spent);
    }

    public void deleteChannel(Long id) {
        channelRepository.deleteById(id);
    }

    public List<ChannelDTO> getAllChannels() {
        List<Channel> channels = channelRepository.findAll();
        Map<Long, BigDecimal> spentMap = resolveMonthlySpentForChannels(channels);
        return channels.stream()
                .map(ch -> toDTO(ch, spentMap.get(ch.getId())))
                .collect(Collectors.toList());
    }

    private Map<Long, BigDecimal> resolveMonthlySpentForChannels(List<Channel> channels) {
        if (channels == null || channels.isEmpty()) {
            return new HashMap<>();
        }
        YearMonth currentYm = YearMonth.now();
        LocalDate monthStart = currentYm.atDay(1);

        Map<Long, BigDecimal> cacheHits = new HashMap<>();
        List<Long> needRecomputeIds = new java.util.ArrayList<>();

        for (Channel ch : channels) {
            if (isCacheValid(ch, currentYm)) {
                cacheHits.put(ch.getId(), ch.getMonthlySpent() != null ? ch.getMonthlySpent() : BigDecimal.ZERO);
            } else {
                needRecomputeIds.add(ch.getId());
            }
        }

        if (!needRecomputeIds.isEmpty()) {
            LocalDate end = currentYm.atEndOfMonth();
            List<Object[]> rows = channelStatsRepository.sumCostByChannelIdsAndDateRange(needRecomputeIds, monthStart, end);
            Map<Long, BigDecimal> recomputed = new HashMap<>();
            for (Object[] r : rows) {
                Long id = (Long) r[0];
                BigDecimal v = (BigDecimal) r[1];
                recomputed.put(id, v != null ? v : BigDecimal.ZERO);
            }
            for (Long id : needRecomputeIds) {
                recomputed.putIfAbsent(id, BigDecimal.ZERO);
            }
            cacheHits.putAll(recomputed);
        }

        return cacheHits;
    }

    private BigDecimal resolveMonthlySpent(Channel channel) {
        YearMonth currentYm = YearMonth.now();
        if (isCacheValid(channel, currentYm)) {
            return channel.getMonthlySpent() != null ? channel.getMonthlySpent() : BigDecimal.ZERO;
        }
        LocalDate start = currentYm.atDay(1);
        LocalDate end = currentYm.atEndOfMonth();
        BigDecimal v = channelStatsRepository.sumCostByChannelIdAndDateRange(channel.getId(), start, end);
        return v != null ? v : BigDecimal.ZERO;
    }

    private boolean isCacheValid(Channel ch, YearMonth currentYm) {
        if (ch.getSpentMonth() == null) {
            return false;
        }
        YearMonth cacheYm = YearMonth.from(ch.getSpentMonth());
        return cacheYm.equals(currentYm) && ch.getMonthlySpent() != null;
    }

    private BudgetStatus computeBudgetStatus(BigDecimal monthlyBudget, BigDecimal alertRatio, BigDecimal monthlySpent) {
        if (monthlyBudget == null || monthlyBudget.compareTo(BigDecimal.ZERO) <= 0) {
            return BudgetStatus.NOT_CONFIGURED;
        }
        if (monthlySpent == null) {
            monthlySpent = BigDecimal.ZERO;
        }
        if (monthlySpent.compareTo(monthlyBudget) > 0) {
            return BudgetStatus.OVERSPENT;
        }
        BigDecimal threshold = monthlyBudget.multiply(alertRatio != null ? alertRatio : new BigDecimal("0.80"));
        if (monthlySpent.compareTo(threshold) >= 0) {
            return BudgetStatus.ALERT;
        }
        return BudgetStatus.NORMAL;
    }

    private BigDecimal computeUtilizationRate(BigDecimal monthlyBudget, BigDecimal monthlySpent) {
        if (monthlyBudget == null || monthlyBudget.compareTo(BigDecimal.ZERO) <= 0) {
            return null;
        }
        if (monthlySpent == null) {
            monthlySpent = BigDecimal.ZERO;
        }
        return monthlySpent.divide(monthlyBudget, 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"))
                .setScale(2, RoundingMode.HALF_UP);
    }

    private ChannelDTO toDTO(Channel channel, BigDecimal monthlySpent) {
        ChannelDTO dto = new ChannelDTO();
        dto.setId(channel.getId());
        dto.setName(channel.getName());
        dto.setType(channel.getType());
        dto.setDescription(channel.getDescription());
        dto.setBudget(channel.getBudget());
        dto.setMonthlyBudget(channel.getMonthlyBudget());
        dto.setAlertRatio(channel.getAlertRatio());
        dto.setMonthlySpent(monthlySpent);
        dto.setBudgetUtilizationRate(computeUtilizationRate(channel.getMonthlyBudget(), monthlySpent));
        dto.setBudgetStatus(computeBudgetStatus(channel.getMonthlyBudget(), channel.getAlertRatio(), monthlySpent).name());
        dto.setStatus(channel.getStatus());
        dto.setCreatedAt(channel.getCreatedAt());
        dto.setUpdatedAt(channel.getUpdatedAt());
        return dto;
    }
}
