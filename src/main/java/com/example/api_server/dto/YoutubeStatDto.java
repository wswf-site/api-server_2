package com.example.api_server.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class YoutubeStatDto {
    private String teamName;
    private String videoId;

    private long viewCount;
    private Long rawLikes;
    private Long score;
    private String viewCollectedAt; // KST로 포맷된 문자열
    private String likeCollectedAt; // KST로 포맷된 문자열

    private Long rawLikesDiff;
    private Long viewCountDiff;
    private Long scoreDiff;

    private Long rawHalfLikes;
    private Long rawHalfScores;
}
