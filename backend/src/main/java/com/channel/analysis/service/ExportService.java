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
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExportService {

    private static final String[] REPORT_HEADERS = {
            "Channel", "Type", "Impressions", "Clicks", "Conversions",
            "Cost", "Revenue", "CTR(%)", "CVR(%)", "ROI(%)", "CPC", "CPA"
    };

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
            for (int i = 0; i < REPORT_HEADERS.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(REPORT_HEADERS[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowNum = 1;
            for (AnalysisReportDTO r : reports) {
                Row row = sheet.createRow(rowNum++);
                String[] values = buildReportRow(r);
                for (int i = 0; i < values.length; i++) {
                    Cell cell = row.createCell(i);
                    switch (i) {
                        case 0, 1:
                            cell.setCellValue(values[i]);
                            break;
                        case 2, 3, 4:
                            cell.setCellValue(Long.parseLong(values[i]));
                            break;
                        default:
                            cell.setCellValue(Double.parseDouble(values[i]));
                            break;
                    }
                }
            }
            for (int i = 0; i < REPORT_HEADERS.length; i++) sheet.autoSizeColumn(i);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        }
    }

    public String exportAnalysisToCsv(LocalDate start, LocalDate end) throws IOException {
        List<AnalysisReportDTO> reports = analysisService.generateReport(start, end);
        StringWriter sw = new StringWriter();
        try (CSVWriter writer = new CSVWriter(sw)) {
            writer.writeNext(REPORT_HEADERS);
            for (AnalysisReportDTO r : reports) {
                writer.writeNext(buildReportRow(r));
            }
        }
        return sw.toString();
    }

    private String[] buildReportRow(AnalysisReportDTO r) {
        return new String[]{
                r.getChannelName(),
                r.getChannelType() != null ? r.getChannelType() : "",
                String.valueOf(r.getTotalImpressions()),
                String.valueOf(r.getTotalClicks()),
                String.valueOf(r.getTotalConversions()),
                valueOf(r.getTotalCost()),
                valueOf(r.getTotalRevenue()),
                valueOf(r.getCtr()),
                valueOf(r.getCvr()),
                valueOf(r.getRoi()),
                valueOf(r.getCpc()),
                valueOf(r.getCpa())
        };
    }

    private String valueOf(BigDecimal v) {
        return v.toPlainString();
    }
}
