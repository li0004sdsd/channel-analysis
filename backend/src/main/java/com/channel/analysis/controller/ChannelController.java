package com.channel.analysis.controller;

import com.channel.analysis.dto.ApiResponse;
import com.channel.analysis.dto.ChannelDTO;
import com.channel.analysis.dto.PageResult;
import com.channel.analysis.service.ChannelService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/channels")
@RequiredArgsConstructor
public class ChannelController {

    private final ChannelService channelService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResult<ChannelDTO>>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String status) {
        return ResponseEntity.ok(ApiResponse.success(channelService.listChannels(page, size, name, status)));
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<ChannelDTO>>> all() {
        return ResponseEntity.ok(ApiResponse.success(channelService.getAllChannels()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ChannelDTO>> get(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(channelService.getChannel(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ChannelDTO>> create(@Valid @RequestBody ChannelDTO dto) {
        return ResponseEntity.ok(ApiResponse.success(channelService.createChannel(dto)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ChannelDTO>> update(@PathVariable Long id, @Valid @RequestBody ChannelDTO dto) {
        return ResponseEntity.ok(ApiResponse.success(channelService.updateChannel(id, dto)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        channelService.deleteChannel(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
