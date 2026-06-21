package com.channel.analysis.controller;

import com.channel.analysis.dto.ApiResponse;
import com.channel.analysis.dto.ChannelStatsDTO;
import com.channel.analysis.dto.PageResult;
import com.channel.analysis.service.ChannelStatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
public class ChannelStatsController {

    private final ChannelStatsService statsService;

    @GetMapping("/channel/{channelId}")
    public ResponseEntity<ApiResponse<PageResult<ChannelStatsDTO>>> list(
            @PathVariable Long channelId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success(statsService.listStats(channelId, page, size)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ChannelStatsDTO>> create(@RequestBody ChannelStatsDTO dto) {
        return ResponseEntity.ok(ApiResponse.success(statsService.createStats(dto)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ChannelStatsDTO>> update(@PathVariable Long id, @RequestBody ChannelStatsDTO dto) {
        return ResponseEntity.ok(ApiResponse.success(statsService.updateStats(id, dto)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        statsService.deleteStats(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @GetMapping("/range")
    public ResponseEntity<ApiResponse<List<ChannelStatsDTO>>> getByRange(
            @RequestParam Long channelId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return ResponseEntity.ok(ApiResponse.success(statsService.getStatsByDateRange(channelId, start, end)));
    }

    @GetMapping("/batch")
    public ResponseEntity<ApiResponse<Map<String, List<ChannelStatsDTO>>>> getBatch(
            @RequestParam List<Long> channelIds,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return ResponseEntity.ok(ApiResponse.success(statsService.getBatchStatsByDateRange(channelIds, start, end)));
    }
}
