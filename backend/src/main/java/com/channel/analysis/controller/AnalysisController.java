package com.channel.analysis.controller;

import com.channel.analysis.dto.AnalysisReportDTO;
import com.channel.analysis.dto.ApiResponse;
import com.channel.analysis.dto.ChannelTypeReportDTO;
import com.channel.analysis.service.AnalysisService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/analysis")
@RequiredArgsConstructor
public class AnalysisController {

    private final AnalysisService analysisService;

    @GetMapping("/report")
    public ResponseEntity<ApiResponse<List<AnalysisReportDTO>>> report(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return ResponseEntity.ok(ApiResponse.success(analysisService.generateReport(start, end)));
    }

    @GetMapping("/report-by-type")
    public ResponseEntity<ApiResponse<List<ChannelTypeReportDTO>>> reportByType(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return ResponseEntity.ok(ApiResponse.success(analysisService.generateReportByType(start, end)));
    }
}
