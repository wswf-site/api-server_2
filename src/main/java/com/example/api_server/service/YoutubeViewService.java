package com.example.api_server.service;

import com.example.api_server.dto.YoutubeViewDto;
import com.example.api_server.dto.YoutubeViewResponseDto;
import com.example.api_server.entity.YoutubeView;
import com.example.api_server.repository.YoutubeViewRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.example.api_server.util.CacheUtils.*;
import static com.example.api_server.util.TimeUtils.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class YoutubeViewService {
    private final YoutubeViewRepository youtubeViewRepository;
    private final RedisCacheService redisCacheService;

    /**
     * 날짜별 시간당 + 최근 1시간 데이터를 함께 조회
     */
    public YoutubeViewResponseDto getViewsWithRecentHour(String videoId, List<LocalDate> dates) {
        // 날짜별 시간당 데이터
        Map<String, List<YoutubeViewDto>> dailyData = getHourlyViewsByDates(videoId, dates);

        // 최근 1시간 데이터
        List<YoutubeViewDto> recentHourData = getRecentHourViews(videoId);

        return YoutubeViewResponseDto.builder()
                .recentHourData(recentHourData)
                .dailyData(dailyData)
                .build();
    }

    /**
     * 날짜별 시간당 조회수 데이터 조회
     */
    public Map<String, List<YoutubeViewDto>> getHourlyViewsByDates(String videoId, List<LocalDate> dates) {
        Map<String, List<YoutubeViewDto>> result = new LinkedHashMap<>();
        for (LocalDate date : excludeFutureDates(dates)) {
            result.put(date.toString(), getAndCacheHourlyViewsForDate(videoId, date));
        }
        return result;
    }

    private List<YoutubeViewDto> getAndCacheHourlyViewsForDate(String videoId, LocalDate date) {
        String cacheKey = VIEW_HISTORY_CACHE_PREFIX + videoId + ":" + date;
        List<YoutubeViewDto> cached = redisCacheService.get(cacheKey, new TypeReference<>() {});
        if (cached != null) {
            return cached;
        }

        Instant start = date.atStartOfDay(KST_ZONE_ID).toInstant();
        Instant end = date.plusDays(1).atStartOfDay(KST_ZONE_ID).toInstant();

        List<YoutubeView> hourlyViews = youtubeViewRepository.findHourlyLatestViewsByVideoIdAndDateRange(videoId, start, end);

        if (hourlyViews.isEmpty()) {
            log.info("조회수 데이터 없음: videoId={}, date={}", videoId, date);
        }

        List<YoutubeViewDto> convertedViews = hourlyViews.stream()
                .map(YoutubeViewDto::from)
                .toList();

        Duration ttl = isToday(date) ? TODAY_CACHE_TTL : PAST_CACHE_TTL;
        redisCacheService.set(cacheKey, convertedViews, ttl);
        return convertedViews;
    }

    /**
     * 최근 1시간치 조회수 데이터 조회 (5분마다 수집된 최근 12개)
     */
    public List<YoutubeViewDto> getRecentHourViews(String videoId) {
        String cacheKey = VIEW_HISTORY_CACHE_PREFIX + videoId + ":recent_hour";

        List<YoutubeViewDto> cached = redisCacheService.get(cacheKey, new TypeReference<>() {});
        if (cached != null) {
            return cached;
        }

        List<YoutubeView> recentViews = youtubeViewRepository.findRecent12ViewsByVideoId(videoId);

        if (recentViews.isEmpty()) {
            log.info("최근 1시간 조회수 데이터 없음: videoId={}", videoId);
        }

        List<YoutubeViewDto> convertedRecentViews = recentViews.stream()
                .map(YoutubeViewDto::from)
                .toList();

        redisCacheService.set(cacheKey, convertedRecentViews, RECENT_TTL);
        return convertedRecentViews;
    }

}
