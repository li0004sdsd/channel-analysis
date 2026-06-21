package com.channel.analysis.service;

import com.channel.analysis.dto.ChannelStatsDTO;
import com.channel.analysis.dto.PageResult;
import com.channel.analysis.entity.Channel;
import com.channel.analysis.entity.ChannelStats;
import com.channel.analysis.repository.ChannelRepository;
import com.channel.analysis.repository.ChannelStatsRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChannelStatsServiceTest {

    @Mock
    private ChannelStatsRepository statsRepository;

    @Mock
    private ChannelRepository channelRepository;

    @InjectMocks
    private ChannelStatsService statsService;

    @Test
    void getStatsByDateRange_emptyResult_returnsEmptyList() {
        Long channelId = 1L;
        LocalDate start = LocalDate.of(2025, 1, 1);
        LocalDate end = LocalDate.of(2025, 1, 31);

        when(statsRepository.findByChannelIdAndStatDateBetween(channelId, start, end))
                .thenReturn(Collections.emptyList());

        List<ChannelStatsDTO> result = statsService.getStatsByDateRange(channelId, start, end);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(statsRepository).findByChannelIdAndStatDateBetween(channelId, start, end);
        verifyNoMoreInteractions(statsRepository);
    }

    @Test
    void createStats_channelNotFound_throwsExceptionWithoutSave() {
        ChannelStatsDTO dto = new ChannelStatsDTO();
        dto.setChannelId(999L);
        dto.setStatDate(LocalDate.of(2025, 6, 1));
        dto.setImpressions(1000L);
        dto.setClicks(50L);
        dto.setConversions(5L);
        dto.setCost(new BigDecimal("100.00"));
        dto.setRevenue(new BigDecimal("200.00"));

        when(channelRepository.findById(999L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> statsService.createStats(dto));
        assertEquals("Channel not found", ex.getMessage());

        verify(statsRepository, never()).save(any(ChannelStats.class));
    }

    @Test
    void listStats_correctlyMapsTotalAndPageRecords() {
        Long channelId = 1L;
        int page = 1;
        int size = 10;

        Channel channel = new Channel();
        channel.setId(1L);
        channel.setName("Google Ads");

        ChannelStats stats1 = new ChannelStats();
        stats1.setId(1L);
        stats1.setChannel(channel);
        stats1.setStatDate(LocalDate.of(2025, 6, 1));
        stats1.setImpressions(1000L);
        stats1.setClicks(50L);
        stats1.setConversions(5L);
        stats1.setCost(new BigDecimal("100.00"));
        stats1.setRevenue(new BigDecimal("200.00"));

        ChannelStats stats2 = new ChannelStats();
        stats2.setId(2L);
        stats2.setChannel(channel);
        stats2.setStatDate(LocalDate.of(2025, 6, 2));
        stats2.setImpressions(2000L);
        stats2.setClicks(100L);
        stats2.setConversions(10L);
        stats2.setCost(new BigDecimal("200.00"));
        stats2.setRevenue(new BigDecimal("400.00"));

        List<ChannelStats> statsList = List.of(stats1, stats2);
        Page<ChannelStats> mockPage = new PageImpl<>(statsList);

        when(statsRepository.findByChannelId(eq(channelId), any())).thenReturn(mockPage);

        PageResult<ChannelStatsDTO> result = statsService.listStats(channelId, page, size);

        assertEquals(2L, result.getTotal());
        assertEquals(page, result.getPage());
        assertEquals(size, result.getSize());
        assertEquals(2, result.getRecords().size());

        ChannelStatsDTO first = result.getRecords().get(0);
        assertEquals(1L, first.getId());
        assertEquals(1L, first.getChannelId());
        assertEquals("Google Ads", first.getChannelName());
        assertEquals(LocalDate.of(2025, 6, 1), first.getStatDate());
        assertEquals(1000L, first.getImpressions());
        assertEquals(50L, first.getClicks());
        assertEquals(5L, first.getConversions());
        assertEquals(new BigDecimal("100.00"), first.getCost());
        assertEquals(new BigDecimal("200.00"), first.getRevenue());

        ChannelStatsDTO second = result.getRecords().get(1);
        assertEquals(2L, second.getId());
        assertEquals(2000L, second.getImpressions());
    }
}
