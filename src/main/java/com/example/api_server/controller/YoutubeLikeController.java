package com.example.api_server.controller;

import com.example.api_server.dto.YoutubeLikeDto;
import com.example.api_server.service.YoutubeLikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/history/likes")
@RequiredArgsConstructor
public class YoutubeLikeController {
    private final YoutubeLikeService youtubeLikeService;

    @GetMapping("/{videoId}")
    public Map<String, List<YoutubeLikeDto>> getHourlyLikes(
            @PathVariable String videoId,
            @RequestParam("dates") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) List<LocalDate> dates
    ) {
        return youtubeLikeService.getHourlyLikesByDates(videoId, dates);
    }
}
