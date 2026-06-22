package com.channel.analysis.repository;

import com.channel.analysis.entity.Channel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface ChannelRepository extends JpaRepository<Channel, Long> {
    Page<Channel> findByNameContainingIgnoreCase(String name, Pageable pageable);
    Page<Channel> findByStatus(String status, Pageable pageable);
    Page<Channel> findByNameContainingIgnoreCaseAndStatus(String name, String status, Pageable pageable);
    List<Long> findIdByStatus(String status);

    @Modifying
    @Query("UPDATE Channel c SET c.monthlySpent = c.monthlySpent + :delta, " +
           "c.spentMonth = :targetMonth, c.updatedAt = CURRENT_TIMESTAMP " +
           "WHERE c.id = :channelId AND (c.spentMonth = :targetMonth OR c.spentMonth IS NULL)")
    int incrementMonthlySpent(@Param("channelId") Long channelId,
                              @Param("delta") BigDecimal delta,
                              @Param("targetMonth") LocalDate targetMonth);

    @Modifying
    @Query("UPDATE Channel c SET c.monthlySpent = :delta, " +
           "c.spentMonth = :targetMonth, c.updatedAt = CURRENT_TIMESTAMP " +
           "WHERE c.id = :channelId AND (c.spentMonth != :targetMonth OR c.spentMonth IS NULL)")
    int resetAndSetMonthlySpent(@Param("channelId") Long channelId,
                                @Param("delta") BigDecimal delta,
                                @Param("targetMonth") LocalDate targetMonth);
}
