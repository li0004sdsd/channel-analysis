package com.channel.analysis.service;

import com.channel.analysis.dto.ImportResult;
import com.channel.analysis.dto.ImportRowResult;
import com.channel.analysis.entity.Channel;
import com.channel.analysis.entity.ChannelStats;
import com.channel.analysis.exception.BatchImportException;
import com.channel.analysis.repository.ChannelRepository;
import com.channel.analysis.repository.ChannelStatsRepository;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChannelStatsImportService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final double MAX_FAILURE_RATIO = 0.20;

    private final ChannelStatsRepository statsRepository;
    private final ChannelRepository channelRepository;
    private final ReportCacheOperations reportCacheService;

    @Transactional
    public ImportResult importCsv(MultipartFile file) throws IOException {
        List<String[]> rawRows = parseCsv(file);
        List<ParsedRow> parsedRows = parseRows(rawRows);

        Map<Long, Channel> channelMap = loadChannels(parsedRows);
        Set<String> existingKeys = loadExistingKeys(parsedRows);

        List<ImportRowResult> rowResults = new ArrayList<>();
        List<ChannelStats> validEntities = new ArrayList<>();

        for (ParsedRow row : parsedRows) {
            ImportRowResult result = validateAndBuildEntity(row, channelMap, existingKeys);
            rowResults.add(result);
            if ("SUCCESS".equals(result.getStatus())) {
                validEntities.add(row.entity);
                existingKeys.add(row.channelId + "_" + row.statDate);
            }
        }

        long failedCount = rowResults.stream().filter(r -> "FAILED".equals(r.getStatus())).count();
        double failureRatio = (double) failedCount / rowResults.size();

        if (failureRatio > MAX_FAILURE_RATIO) {
            log.warn("批量导入校验失败率 {}% 超过阈值 20%，整批回滚。失败数: {}/{}",
                    String.format("%.1f", failureRatio * 100), failedCount, rowResults.size());
            throw new BatchImportException(ImportResult.of(rowResults, true));
        }

        if (!validEntities.isEmpty()) {
            statsRepository.saveAll(validEntities);
            reportCacheService.evictAll();
            log.info("批量导入完成，成功写入 {} 条记录", validEntities.size());
        }

        return ImportResult.of(rowResults, false);
    }

    private List<String[]> parseCsv(MultipartFile file) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
             CSVReader csvReader = new CSVReader(reader)) {
            csvReader.readNext();
            return csvReader.readAll();
        } catch (CsvException e) {
            throw new IOException("CSV解析失败: " + e.getMessage(), e);
        }
    }

    private List<ParsedRow> parseRows(List<String[]> rawRows) {
        List<ParsedRow> result = new ArrayList<>();
        for (int i = 0; i < rawRows.size(); i++) {
            String[] cols = rawRows.get(i);
            int rowNum = i + 2;
            ParsedRow pr = new ParsedRow();
            pr.rowNumber = rowNum;
            pr.rawCols = cols;
            result.add(pr);
        }
        return result;
    }

    private Map<Long, Channel> loadChannels(List<ParsedRow> rows) {
        Set<Long> channelIds = new HashSet<>();
        for (ParsedRow row : rows) {
            try {
                if (row.rawCols.length > 0 && !row.rawCols[0].isBlank()) {
                    channelIds.add(Long.parseLong(row.rawCols[0].trim()));
                }
            } catch (NumberFormatException ignored) {
            }
        }
        if (channelIds.isEmpty()) {
            return Map.of();
        }
        return channelRepository.findAllById(channelIds).stream()
                .collect(Collectors.toMap(Channel::getId, Function.identity()));
    }

    private Set<String> loadExistingKeys(List<ParsedRow> rows) {
        Set<Long> channelIds = new HashSet<>();
        Set<LocalDate> statDates = new HashSet<>();
        for (ParsedRow row : rows) {
            try {
                if (row.rawCols.length > 0 && !row.rawCols[0].isBlank()
                        && row.rawCols.length > 1 && !row.rawCols[1].isBlank()) {
                    channelIds.add(Long.parseLong(row.rawCols[0].trim()));
                    statDates.add(LocalDate.parse(row.rawCols[1].trim(), DATE_FORMATTER));
                }
            } catch (NumberFormatException | DateTimeParseException ignored) {
            }
        }
        if (channelIds.isEmpty() || statDates.isEmpty()) {
            return Set.of();
        }
        List<Object[]> existing = statsRepository.findExistingChannelDatePairs(
                new ArrayList<>(channelIds), new ArrayList<>(statDates));
        Set<String> keys = new HashSet<>();
        for (Object[] pair : existing) {
            keys.add(pair[0] + "_" + pair[1]);
        }
        return keys;
    }

    private ImportRowResult validateAndBuildEntity(ParsedRow row, Map<Long, Channel> channelMap, Set<String> existingKeys) {
        String[] cols = row.rawCols;

        if (cols.length < 7) {
            return ImportRowResult.failed(row.rowNumber, null, null,
                    "列数不足，期望7列(渠道ID,统计日期,曝光量,点击量,转化量,花费,收入)，实际" + cols.length + "列");
        }

        Long channelId;
        try {
            channelId = Long.parseLong(cols[0].trim());
        } catch (NumberFormatException e) {
            return ImportRowResult.failed(row.rowNumber, null, null,
                    "渠道ID格式错误: " + cols[0].trim());
        }
        row.channelId = channelId;

        String dateStr = cols[1].trim();
        LocalDate statDate;
        try {
            statDate = LocalDate.parse(dateStr, DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            return ImportRowResult.failed(row.rowNumber, channelId, null,
                    "统计日期格式错误: " + dateStr + "，期望格式: yyyy-MM-dd");
        }
        row.statDate = statDate;

        Channel channel = channelMap.get(channelId);
        if (channel == null) {
            return ImportRowResult.failed(row.rowNumber, channelId, dateStr,
                    "渠道不存在: ID=" + channelId);
        }

        String duplicateKey = channelId + "_" + statDate;
        if (existingKeys.contains(duplicateKey)) {
            return ImportRowResult.skipped(row.rowNumber, channelId, dateStr);
        }

        Long impressions;
        try {
            impressions = Long.parseLong(cols[2].trim());
            if (impressions < 0) {
                return ImportRowResult.failed(row.rowNumber, channelId, dateStr,
                        "曝光量不能为负数: " + impressions);
            }
        } catch (NumberFormatException e) {
            return ImportRowResult.failed(row.rowNumber, channelId, dateStr,
                    "曝光量格式错误: " + cols[2].trim());
        }

        Long clicks;
        try {
            clicks = Long.parseLong(cols[3].trim());
            if (clicks < 0) {
                return ImportRowResult.failed(row.rowNumber, channelId, dateStr,
                        "点击量不能为负数: " + clicks);
            }
        } catch (NumberFormatException e) {
            return ImportRowResult.failed(row.rowNumber, channelId, dateStr,
                    "点击量格式错误: " + cols[3].trim());
        }

        Long conversions;
        try {
            conversions = Long.parseLong(cols[4].trim());
            if (conversions < 0) {
                return ImportRowResult.failed(row.rowNumber, channelId, dateStr,
                        "转化量不能为负数: " + conversions);
            }
        } catch (NumberFormatException e) {
            return ImportRowResult.failed(row.rowNumber, channelId, dateStr,
                    "转化量格式错误: " + cols[4].trim());
        }

        BigDecimal cost;
        try {
            cost = new BigDecimal(cols[5].trim());
            if (cost.compareTo(BigDecimal.ZERO) < 0) {
                return ImportRowResult.failed(row.rowNumber, channelId, dateStr,
                        "花费不能为负数: " + cost);
            }
        } catch (NumberFormatException e) {
            return ImportRowResult.failed(row.rowNumber, channelId, dateStr,
                    "花费格式错误: " + cols[5].trim());
        }

        BigDecimal revenue;
        try {
            revenue = new BigDecimal(cols[6].trim());
            if (revenue.compareTo(BigDecimal.ZERO) < 0) {
                return ImportRowResult.failed(row.rowNumber, channelId, dateStr,
                        "收入不能为负数: " + revenue);
            }
        } catch (NumberFormatException e) {
            return ImportRowResult.failed(row.rowNumber, channelId, dateStr,
                    "收入格式错误: " + cols[6].trim());
        }

        ChannelStats entity = new ChannelStats();
        entity.setChannel(channel);
        entity.setStatDate(statDate);
        entity.setImpressions(impressions);
        entity.setClicks(clicks);
        entity.setConversions(conversions);
        entity.setCost(cost);
        entity.setRevenue(revenue);
        row.entity = entity;

        return ImportRowResult.success(row.rowNumber, channelId, dateStr);
    }

    private static class ParsedRow {
        int rowNumber;
        String[] rawCols;
        Long channelId;
        LocalDate statDate;
        ChannelStats entity;
    }
}
