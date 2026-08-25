package com.example.api_server.service;

import com.example.api_server.dto.YoutubeLikeDto;
import com.example.api_server.entity.YoutubeLike;
import com.example.api_server.repository.YoutubeLikeRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.*;

import static com.example.api_server.util.CacheUtils.*;
import static com.example.api_server.util.TimeUtils.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class YoutubeLikeService {
    private final YoutubeLikeRepository youtubeLikeRepository;
    private final RedisCacheService redisCacheService;

    public Map<String, List<YoutubeLikeDto>> getHourlyLikesByDates(String videoId, List<LocalDate> dates) {
        Map<String, List<YoutubeLikeDto>> result = new LinkedHashMap<>();
        for (LocalDate date : excludeFutureDates(dates)) {
            result.put(date.toString(), getAndCacheHourlyLikesForDate(videoId, date));
        }
        return result;
    }

    private List<YoutubeLikeDto> getAndCacheHourlyLikesForDate(String videoId, LocalDate date) {
        String cacheKey = LIKE_HISTORY_CACHE_PREFIX+ videoId + ":" + date;
        List<YoutubeLikeDto> cached = redisCacheService.get(cacheKey, new TypeReference<>() {});
        if (cached != null) {
            return cached;
        }

        Instant start = date.atStartOfDay(KST_ZONE_ID).toInstant();
        Instant end = date.plusDays(1).atStartOfDay(KST_ZONE_ID).toInstant();

        List<YoutubeLike> hourlyLikes = youtubeLikeRepository.findHourlyLatestLikesByVideoIdAndDateRange(videoId, start, end);

        if (hourlyLikes.isEmpty()) {
            log.info("데이터 없음: videoId={}, date={}", videoId, date);
        }

        List<YoutubeLikeDto> convertedLikes = hourlyLikes.stream()
                .map(YoutubeLikeDto::from)
                .toList();

        Duration ttl = isToday(date) ? TODAY_CACHE_TTL : PAST_CACHE_TTL;
        redisCacheService.set(cacheKey, convertedLikes, ttl);
        return convertedLikes;
    }

}
