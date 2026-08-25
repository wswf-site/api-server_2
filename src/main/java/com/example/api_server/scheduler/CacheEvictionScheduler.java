package com.example.api_server.scheduler;

import com.example.api_server.service.RedisCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;


import java.util.Set;

import static com.example.api_server.util.CacheUtils.*;
import static com.example.api_server.util.DataUtils.VIDEO_ID_TO_TEAM;
import static com.example.api_server.util.TimeUtils.nowDateKST;

@Component
@RequiredArgsConstructor
@Slf4j
public class CacheEvictionScheduler {
    private final RedisCacheService redisCacheService;

    /**
     * 전체 팀 최신 통계 캐시 갱신 (5분마다 2초 지연)
     */
    @Scheduled(cron = "2 */5 * * * *")
    public void refreshLatestStats() {
        log.info("[Scheduler] youtube:latest:stats 캐시 삭제");
        redisCacheService.delete(LATEST_STATS_CACHE_KEY);
    }

    /**
     * 전체 비디오의 최신 좋아요 데이터 캐시 갱신 (30분마다 1초)
     */
    @Scheduled(cron = "1 */30 * * * *")
    public void refreshLatestLikes() {
        log.info("[Scheduler] youtube:latest:likes 캐시 삭제");
        redisCacheService.delete(LATEST_LIKES_CACHE_KEY);
    }

    /**
     * 각 videoId에 대해 최근 1시간 조회수 캐시 삭제 (5분마다 3초)
     */
    @Scheduled(cron = "3 */5 * * * *")
    public void deleteRecentHourViewCache() {
        Set<String> videoIds = VIDEO_ID_TO_TEAM.keySet();
        for (String videoId : videoIds) {
            String key = VIEW_HISTORY_CACHE_PREFIX + videoId + ":recent_hour";
            redisCacheService.delete(key);
        }
        log.info("[Scheduler] 최근 1시간 뷰 캐시 삭제");
    }

    /**
     * 각 videoId에 대해 오늘자 뷰/좋아요 캐시 삭제 (1시간 마다 4초)
     */
    @Scheduled(cron = "4 0 * * * *")
    public void deleteTodayStatsCache() {
        String today = nowDateKST().toString();  // 예: "2025-07-01"
        Set<String> videoIds = VIDEO_ID_TO_TEAM.keySet();

        for (String videoId : videoIds) {
            String viewKey = VIEW_HISTORY_CACHE_PREFIX + videoId + ":" + today;
            String likeKey = LIKE_HISTORY_CACHE_PREFIX + videoId + ":" + today;
            redisCacheService.delete(viewKey);
            redisCacheService.delete(likeKey);
        }
        log.info("[Scheduler] 오늘자 뷰, 좋아요 캐시 삭제");
    }
}
