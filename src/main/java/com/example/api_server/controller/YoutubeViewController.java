package com.example.api_server.controller;

import com.example.api_server.dto.YoutubeViewResponseDto;
import com.example.api_server.service.YoutubeViewService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/history/views")
@RequiredArgsConstructor
public class YoutubeViewController {
    private final YoutubeViewService youtubeViewService;

    /**
     * 특정 팀의 날짜별 시간당 + 최근 1시간 조회수 데이터를 함께 조회
     *
     * @param videoId 팀의 비디오 ID
     * @param dates 조회할 날짜 목록
     * @return 날짜별 데이터 + 최근 1시간 데이터
     */
    @GetMapping("/with-recent/{videoId}")
    public YoutubeViewResponseDto getViewsWithRecentHour(
            @PathVariable String videoId,
            @RequestParam("dates") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) List<LocalDate> dates
    ) {
        return youtubeViewService.getViewsWithRecentHour(videoId, dates);
    }

}
