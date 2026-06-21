package com.channel.analysis.service;

import com.channel.analysis.dto.AnalysisReportDTO;
import com.channel.analysis.dto.ChannelTypeReportDTO;
import com.channel.analysis.entity.ChannelStats;
import com.channel.analysis.repository.ChannelRepository;
import com.channel.analysis.repository.ChannelStatsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnalysisService {

    private final ChannelRepository channelRepository;
    private final ChannelStatsRepository statsRepository;

    public List<AnalysisReportDTO> generateReport(LocalDate start, LocalDate end) {
        List<ChannelStats> allStats = statsRepository.findByDateRange(start, end);
        Map<Long, List<ChannelStats>> byChannel = allStats.stream()
                .collect(Collectors.groupingBy(s -> s.getChannel().getId()));

        List<AnalysisReportDTO> reports = new ArrayList<>();
        byChannel.forEach((channelId, statsList) -> {
            AnalysisReportDTO dto = new AnalysisReportDTO();
            dto.setChannelId(channelId);
            dto.setChannelName(statsList.get(0).getChannel().getName());
            dto.setChannelType(statsList.get(0).getChannel().getType());

            long impressions = statsList.stream().mapToLong(ChannelStats::getImpressions).sum();
            long clicks = statsList.stream().mapToLong(ChannelStats::getClicks).sum();
            long conversions = statsList.stream().mapToLong(ChannelStats::getConversions).sum();
            BigDecimal cost = statsList.stream().map(ChannelStats::getCost).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal revenue = statsList.stream().map(ChannelStats::getRevenue).reduce(BigDecimal.ZERO, BigDecimal::add);

            dto.setTotalImpressions(impressions);
            dto.setTotalClicks(clicks);
            dto.setTotalConversions(conversions);
            dto.setTotalCost(cost);
            dto.setTotalRevenue(revenue);

            dto.setCtr(impressions > 0
                    ? BigDecimal.valueOf(clicks).divide(BigDecimal.valueOf(impressions), 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
                    : BigDecimal.ZERO);
            dto.setCvr(clicks > 0
                    ? BigDecimal.valueOf(conversions).divide(BigDecimal.valueOf(clicks), 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
                    : BigDecimal.ZERO);
            dto.setRoi(cost.compareTo(BigDecimal.ZERO) > 0
                    ? revenue.subtract(cost).divide(cost, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
                    : BigDecimal.ZERO);
            dto.setCpc(clicks > 0
                    ? cost.divide(BigDecimal.valueOf(clicks), 4, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO);
            dto.setCpa(conversions > 0
                    ? cost.divide(BigDecimal.valueOf(conversions), 4, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO);

            reports.add(dto);
        });
        return reports;
    }

    public List<ChannelTypeReportDTO> generateReportByType(LocalDate start, LocalDate end) {
        List<ChannelStats> allStats = statsRepository.findByDateRange(start, end);
        Map<String, List<ChannelStats>> byType = allStats.stream()
                .collect(Collectors.groupingBy(s -> {
                    String type = s.getChannel().getType();
                    return type != null && !type.isEmpty() ? type : "UNKNOWN";
                }));

        List<ChannelTypeReportDTO> reports = new ArrayList<>();
        byType.forEach((type, statsList) -> {
            ChannelTypeReportDTO dto = new ChannelTypeReportDTO();
            dto.setChannelType(type);

            long impressions = statsList.stream().mapToLong(ChannelStats::getImpressions).sum();
            long clicks = statsList.stream().mapToLong(ChannelStats::getClicks).sum();
            long conversions = statsList.stream().mapToLong(ChannelStats::getConversions).sum();
            BigDecimal cost = statsList.stream().map(ChannelStats::getCost).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal revenue = statsList.stream().map(ChannelStats::getRevenue).reduce(BigDecimal.ZERO, BigDecimal::add);

            dto.setTotalImpressions(impressions);
            dto.setTotalClicks(clicks);
            dto.setTotalConversions(conversions);
            dto.setTotalCost(cost);
            dto.setTotalRevenue(revenue);

            dto.setCtr(impressions > 0
                    ? BigDecimal.valueOf(clicks).divide(BigDecimal.valueOf(impressions), 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
                    : BigDecimal.ZERO);
            dto.setCvr(clicks > 0
                    ? BigDecimal.valueOf(conversions).divide(BigDecimal.valueOf(clicks), 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
                    : BigDecimal.ZERO);
            dto.setRoi(cost.compareTo(BigDecimal.ZERO) > 0
                    ? revenue.subtract(cost).divide(cost, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
                    : BigDecimal.ZERO);
            dto.setCpc(clicks > 0
                    ? cost.divide(BigDecimal.valueOf(clicks), 4, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO);
            dto.setCpa(conversions > 0
                    ? cost.divide(BigDecimal.valueOf(conversions), 4, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO);

            reports.add(dto);
        });

        reports.sort(Comparator.comparing(ChannelTypeReportDTO::getRoi).reversed());
        return reports;
    }
}
