package com.example.api_server.controller;

import com.example.api_server.dto.YoutubeStatDto;
import com.example.api_server.service.YoutubeDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/stats")
@RequiredArgsConstructor
public class YoutubeStatController {
    private final YoutubeDataService youtubeDataService;

    @GetMapping("/current")
    public List<YoutubeStatDto> getLatestStatsWithDiff() {
        return youtubeDataService.getAllTeamsLatestStats();
    }
}
