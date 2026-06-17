package com.channel.analysis.service;

import com.channel.analysis.dto.ChannelDTO;
import com.channel.analysis.dto.PageResult;
import com.channel.analysis.entity.Channel;
import com.channel.analysis.repository.ChannelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChannelService {

    private final ChannelRepository channelRepository;

    public PageResult<ChannelDTO> listChannels(int page, int size, String name, String status) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("createdAt").descending());
        Page<Channel> pageData;
        boolean hasName = name != null && !name.isBlank();
        boolean hasStatus = status != null && !status.isBlank();
        if (hasName && hasStatus) {
            pageData = channelRepository.findByNameContainingIgnoreCaseAndStatus(name, status, pageable);
        } else if (hasName) {
            pageData = channelRepository.findByNameContainingIgnoreCase(name, pageable);
        } else if (hasStatus) {
            pageData = channelRepository.findByStatus(status, pageable);
        } else {
            pageData = channelRepository.findAll(pageable);
        }
        List<ChannelDTO> records = pageData.getContent().stream().map(this::toDTO).collect(Collectors.toList());
        return new PageResult<>(records, pageData.getTotalElements(), page, size);
    }

    public ChannelDTO getChannel(Long id) {
        Channel channel = channelRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Channel not found: " + id));
        return toDTO(channel);
    }

    public ChannelDTO createChannel(ChannelDTO dto) {
        Channel channel = new Channel();
        channel.setName(dto.getName());
        channel.setType(dto.getType());
        channel.setDescription(dto.getDescription());
        channel.setBudget(dto.getBudget());
        channel.setStatus(dto.getStatus() != null ? dto.getStatus() : "ACTIVE");
        channel.setCreatedAt(LocalDateTime.now());
        channel.setUpdatedAt(LocalDateTime.now());
        return toDTO(channelRepository.save(channel));
    }

    public ChannelDTO updateChannel(Long id, ChannelDTO dto) {
        Channel channel = channelRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Channel not found: " + id));
        channel.setName(dto.getName());
        channel.setType(dto.getType());
        channel.setDescription(dto.getDescription());
        channel.setBudget(dto.getBudget());
        channel.setStatus(dto.getStatus());
        channel.setUpdatedAt(LocalDateTime.now());
        return toDTO(channelRepository.save(channel));
    }

    public void deleteChannel(Long id) {
        channelRepository.deleteById(id);
    }

    public List<ChannelDTO> getAllChannels() {
        return channelRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    private ChannelDTO toDTO(Channel channel) {
        ChannelDTO dto = new ChannelDTO();
        dto.setId(channel.getId());
        dto.setName(channel.getName());
        dto.setType(channel.getType());
        dto.setDescription(channel.getDescription());
        dto.setBudget(channel.getBudget());
        dto.setStatus(channel.getStatus());
        dto.setCreatedAt(channel.getCreatedAt());
        dto.setUpdatedAt(channel.getUpdatedAt());
        return dto;
    }
}
