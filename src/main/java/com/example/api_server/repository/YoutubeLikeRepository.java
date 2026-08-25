package com.example.api_server.repository;

import com.example.api_server.dto.AggregatedLikeResult;
import com.example.api_server.entity.YoutubeLike;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Repository
public interface YoutubeLikeRepository extends MongoRepository<YoutubeLike, String> {
    @Aggregation(pipeline = {
            "{ '$match': { 'videoId': { '$in': ?0 } } }",
            "{ '$sort': { 'videoId': 1, 'dateCreated': -1 } }",
            "{ '$group': { '_id': '$videoId', 'docs': { '$push': '$$ROOT' } } }",
            "{ '$project': { 'videoId': '$_id', 'latestTwo': { '$slice': ['$docs', 2] } } }"
    })
    List<AggregatedLikeResult> findTop2ByVideoIdInBatch(List<String> videoIds);

    @Aggregation(pipeline = {
            "{ '$match': { 'videoId': ?0, 'dateCreated': { '$gte': ?1, '$lt': ?2 } } }",
            "{ '$addFields': { 'hourKey': { '$dateToString': { 'format': '%Y-%m-%d-%H', 'date': '$dateCreated', 'timezone': 'Asia/Seoul' } } } }",
            "{ '$sort': { 'dateCreated': 1 } }",
            "{ '$group': { '_id': '$hourKey', 'latest': { '$first': '$$ROOT' } } }",
            "{ '$replaceRoot': { 'newRoot': '$latest' } }",
            "{ '$sort': { 'dateCreated': 1 } }"
    })
    List<YoutubeLike> findHourlyLatestLikesByVideoIdAndDateRange(String videoId, Instant start, Instant end);

}

