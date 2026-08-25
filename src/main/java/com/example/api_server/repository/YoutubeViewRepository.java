package com.example.api_server.repository;

import com.example.api_server.dto.AggregatedViewResult;
import com.example.api_server.entity.YoutubeView;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Repository
public interface YoutubeViewRepository extends MongoRepository<YoutubeView, String> {
    // 모든 videoId에 대해 가장 최신 데이터 2개 반환하기
    @Aggregation(pipeline = {
            "{ '$match': { 'videoId': { '$in': ?0 } } }",
            "{ '$sort': { 'videoId': 1, 'collectedAt': -1 } }",
            "{ '$group': { '_id': '$videoId', 'docs': { '$push': '$$ROOT' } } }",
            "{ '$project': { 'videoId': '$_id', 'latestTwo': { '$slice': ['$docs', 2] } } }"
    })
    List<AggregatedViewResult> findTop2ByVideoIdInBatch(List<String> videoIds);

    // 특정 videoId의 날짜 범위별 시간당 최신 데이터 조회 (1시간마다 1개씩)
    @Aggregation(pipeline = {
            "{ '$match': { 'videoId': ?0, 'collectedAt': { '$gte': ?1, '$lt': ?2 } } }",
            "{ '$addFields': { 'hourKey': { '$dateToString': { 'format': '%Y-%m-%d-%H', 'date': '$collectedAt', 'timezone': 'Asia/Seoul' } } } }",
            "{ '$sort': { 'collectedAt': 1 } }",
            "{ '$group': { '_id': '$hourKey', 'latest': { '$first': '$$ROOT' } } }",
            "{ '$replaceRoot': { 'newRoot': '$latest' } }",
            "{ '$sort': { 'collectedAt': 1 } }"
    })
    List<YoutubeView> findHourlyLatestViewsByVideoIdAndDateRange(String videoId, Instant start, Instant end);

    // 최근 1시간치 데이터 조회 (최근 12개 - 5분마다 수집되므로)
    // 최근 12개 데이터 조회 (5분마다 수집되므로 약 1시간치)
    @Aggregation(pipeline = {
            "{ '$match': { 'videoId': ?0 } }",
            "{ '$sort': { 'collectedAt': -1 } }",
            "{ '$limit': 12 }",
            "{ '$sort': { 'collectedAt': 1 } }"
    })
    List<YoutubeView> findRecent12ViewsByVideoId(String videoId);
}