package com.example.api_server.service;

import com.example.api_server.dto.AggregatedLikeResult;
import com.example.api_server.dto.AggregatedViewResult;
import com.example.api_server.dto.YoutubeStatDto;
import com.example.api_server.entity.YoutubeLike;
import com.example.api_server.entity.YoutubeView;
import com.example.api_server.repository.YoutubeLikeRepository;
import com.example.api_server.repository.YoutubeViewRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.example.api_server.util.CacheUtils.*;
import static com.example.api_server.util.DataUtils.UNKNOWN_TEAM;
import static com.example.api_server.util.DataUtils.VIDEO_ID_TO_TEAM;
import static com.example.api_server.util.TimeUtils.formatToKST;

@Service
@RequiredArgsConstructor
@Slf4j
public class YoutubeDataService {
    private final YoutubeLikeRepository youtubeLikeRepository;
    private final YoutubeViewRepository youtubeViewRepository;
    private final RedisCacheService redisCacheService;

    public List<YoutubeStatDto> getAllTeamsLatestStats() {
        List<YoutubeStatDto> cached = redisCacheService.get(LATEST_STATS_CACHE_KEY, new TypeReference<>() {});
        if (cached != null) {
            return cached;
        }

        List<String> videoIds = new ArrayList<>(VIDEO_ID_TO_TEAM.keySet());

        // 1. 모든 videoId에 대해 한 번에 데이터 조회 (2번의 쿼리로 모든 데이터 획득)
        Map<String, List<YoutubeView>> viewsByVideoId = getBatchViewData(videoIds);
        Map<String, List<YoutubeLike>> likesByVideoId = getBatchLikeDataWithSeparateCache(videoIds);

        // 2. 각 videoId별로 통계 계산
        List<YoutubeStatDto> stats = videoIds.stream()
                .map(videoId -> calculateStatsForVideoId(videoId, viewsByVideoId, likesByVideoId))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        redisCacheService.set(LATEST_STATS_CACHE_KEY, stats, RECENT_TTL);
        return stats;
    }

    // MongoDB Aggregation으로 한 번에 모든 데이터 조회
    private Map<String, List<YoutubeView>> getBatchViewData(List<String> videoIds) {
         // Aggregation 활용
         return youtubeViewRepository.findTop2ByVideoIdInBatch(videoIds)
             .stream()
                 .collect(Collectors.toMap(
                         AggregatedViewResult::getVideoId,
                         AggregatedViewResult::getLatestTwo
                 ));
    }

    private Map<String, List<YoutubeLike>> getBatchLikeDataWithSeparateCache(List<String> videoIds) {
        Map<String, List<YoutubeLike>> cachedLikes = redisCacheService.get(LATEST_LIKES_CACHE_KEY, new TypeReference<>() {});
        if (cachedLikes != null && !cachedLikes.isEmpty()) {
            return cachedLikes;
        }

        // Aggregation 활용
        Map<String, List<YoutubeLike>> recentLikes = youtubeLikeRepository.findTop2ByVideoIdInBatch(videoIds)
                .stream()
                .collect(Collectors.toMap(
                        AggregatedLikeResult::getVideoId,
                        AggregatedLikeResult::getLatestTwo
                ));
        redisCacheService.set(LATEST_LIKES_CACHE_KEY, recentLikes, RECENT_LIKE_TTL);
        return recentLikes;
    }

    // 개별 videoId에 대한 통계 계산 (DB 호출 없음)
    private YoutubeStatDto calculateStatsForVideoId(String videoId,
                                                    Map<String, List<YoutubeView>> viewsByVideoId,
                                                    Map<String, List<YoutubeLike>> likesByVideoId) {
        String teamName = VIDEO_ID_TO_TEAM.getOrDefault(videoId, UNKNOWN_TEAM);

        List<YoutubeView> latestViews = viewsByVideoId.get(videoId);
        List<YoutubeLike> latestLikes = likesByVideoId.get(videoId);

        if (latestViews == null || latestViews.isEmpty() || latestLikes == null || latestLikes.isEmpty()) {
            log.info("데이터 부족: videoId={}, teamName={}. 좋아요 또는 조회수 데이터가 충분하지 않습니다.", videoId, teamName);
            return null;
        }

        // 최신 데이터 추출
        YoutubeView currentView = latestViews.get(0);
        YoutubeLike currentLike = latestLikes.get(0);
        Long currentScore = calculateScore(currentView.getViewCount(), currentLike.getRawLikes());

        // 이전 데이터 추출 및 차이 계산
        Long viewCountDifference = null;
        Long rawLikesDifference = null;
        Long scoreDifference = null;

        if (latestViews.size() > 1 && latestLikes.size() > 1) {
            YoutubeView previousView = latestViews.get(1);
            YoutubeLike previousLike = latestLikes.get(1);
            Long previousScore = calculateScore(previousView.getViewCount(), previousLike.getRawLikes());

            rawLikesDifference = currentLike.getRawLikes() - previousLike.getRawLikes();
            viewCountDifference = currentView.getViewCount() - previousView.getViewCount();
            scoreDifference = currentScore - previousScore;
        } else {
            log.info("이전 데이터 부족: videoId={}, teamName={}. 차이 계산을 건너뜁니다.", videoId, teamName);
        }

        return YoutubeStatDto.builder()
                .videoId(videoId)
                .teamName(teamName)
                .viewCount(currentView.getViewCount())
                .rawLikes(currentLike.getRawLikes())
                .score(currentScore)
                .viewCollectedAt(formatToKST(currentView.getCollectedAt()))
                .likeCollectedAt(formatToKST(currentLike.getDateCreated()))
                .rawLikesDiff(rawLikesDifference)
                .viewCountDiff(viewCountDifference)
                .scoreDiff(scoreDifference)
                .rawHalfLikes(currentLike.getRawLikes() / 2)
                .rawHalfScores(calculateScore(currentView.getViewCount(), (currentLike.getRawLikes()/2)))
                .build();
    }

    private Long calculateScore(Long viewCount, Long likeCount) {
        return viewCount + likeCount * 100;
    }
}
