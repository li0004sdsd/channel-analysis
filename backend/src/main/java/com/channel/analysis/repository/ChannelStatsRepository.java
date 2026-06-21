package com.channel.analysis.repository;

import com.channel.analysis.entity.ChannelStats;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;

public interface ChannelStatsRepository extends JpaRepository<ChannelStats, Long> {
    Page<ChannelStats> findByChannelId(Long channelId, Pageable pageable);

    List<ChannelStats> findByChannelIdAndStatDateBetween(Long channelId, LocalDate start, LocalDate end);

    @Query("SELECT s FROM ChannelStats s JOIN FETCH s.channel WHERE s.statDate BETWEEN :start AND :end")
    List<ChannelStats> findByDateRange(@Param("start") LocalDate start, @Param("end") LocalDate end);

    @Query("SELECT s FROM ChannelStats s WHERE s.channel.id = :channelId")
    List<ChannelStats> findAllByChannelId(@Param("channelId") Long channelId);

    @Query("SELECT s FROM ChannelStats s JOIN FETCH s.channel WHERE s.channel.id IN :channelIds AND s.statDate BETWEEN :start AND :end ORDER BY s.channel.id, s.statDate ASC")
    List<ChannelStats> findByChannelIdsAndDateRange(@Param("channelIds") List<Long> channelIds, @Param("start") LocalDate start, @Param("end") LocalDate end);
}
