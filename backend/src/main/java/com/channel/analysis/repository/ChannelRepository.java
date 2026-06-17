package com.channel.analysis.repository;

import com.channel.analysis.entity.Channel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChannelRepository extends JpaRepository<Channel, Long> {
    Page<Channel> findByNameContainingIgnoreCase(String name, Pageable pageable);
    Page<Channel> findByStatus(String status, Pageable pageable);
    Page<Channel> findByNameContainingIgnoreCaseAndStatus(String name, String status, Pageable pageable);
}
