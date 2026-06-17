package com.channel.analysis.service;

import com.channel.analysis.dto.AnalysisReportDTO;
import com.opencsv.CSVWriter;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExportService {

    private final AnalysisService analysisService;

    public byte[] exportAnalysisToExcel(LocalDate start, LocalDate end) throws IOException {
        List<AnalysisReportDTO> reports = analysisService.generateReport(start, end);
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Analysis Report");
            CellStyle headerStyle = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            headerStyle.setFont(font);

            Row headerRow = sheet.createRow(0);
            String[] headers = {"Channel", "Type", "Impressions", "Clicks", "Conversions",
                    "Cost", "Revenue", "CTR(%)", "CVR(%)", "ROI(%)", "CPC", "CPA"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowNum = 1;
            for (AnalysisReportDTO r : reports) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(r.getChannelName());
                row.createCell(1).setCellValue(r.getChannelType() != null ? r.getChannelType() : "");
                row.createCell(2).setCellValue(r.getTotalImpressions());
                row.createCell(3).setCellValue(r.getTotalClicks());
                row.createCell(4).setCellValue(r.getTotalConversions());
                row.createCell(5).setCellValue(r.getTotalCost().doubleValue());
                row.createCell(6).setCellValue(r.getTotalRevenue().doubleValue());
                row.createCell(7).setCellValue(r.getCtr().doubleValue());
                row.createCell(8).setCellValue(r.getCvr().doubleValue());
                row.createCell(9).setCellValue(r.getRoi().doubleValue());
                row.createCell(10).setCellValue(r.getCpc().doubleValue());
                row.createCell(11).setCellValue(r.getCpa().doubleValue());
            }
            for (int i = 0; i < headers.length; i++) sheet.autoSizeColumn(i);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        }
    }

    public String exportAnalysisToCsv(LocalDate start, LocalDate end) throws IOException {
        List<AnalysisReportDTO> reports = analysisService.generateReport(start, end);
        StringWriter sw = new StringWriter();
        try (CSVWriter writer = new CSVWriter(sw)) {
            writer.writeNext(new String[]{"Channel", "Type", "Impressions", "Clicks", "Conversions",
                    "Cost", "Revenue", "CTR(%)", "CVR(%)", "ROI(%)", "CPC", "CPA"});
            for (AnalysisReportDTO r : reports) {
                writer.writeNext(new String[]{
                        r.getChannelName(),
                        r.getChannelType() != null ? r.getChannelType() : "",
                        String.valueOf(r.getTotalImpressions()),
                        String.valueOf(r.getTotalClicks()),
                        String.valueOf(r.getTotalConversions()),
                        r.getTotalCost().toPlainString(),
                        r.getTotalRevenue().toPlainString(),
                        r.getCtr().toPlainString(),
                        r.getCvr().toPlainString(),
                        r.getRoi().toPlainString(),
                        r.getCpc().toPlainString(),
                        r.getCpa().toPlainString()
                });
            }
        }
        return sw.toString();
    }
}
