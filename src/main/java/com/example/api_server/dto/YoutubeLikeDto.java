package com.example.api_server.dto;

import com.example.api_server.entity.YoutubeLike;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

import static com.example.api_server.util.TimeUtils.formatToKST;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class YoutubeLikeDto {
    private String videoId;
    private String teamName;
    private Long rawLikes;
    private Long rawDislikes;
    private String dateCreated;  // KST로 포맷된 문자열
    private String collectedAt;  // KST로 포맷된 문자열

    public static YoutubeLikeDto from(YoutubeLike entity) {
        return YoutubeLikeDto.builder()
                .videoId(entity.getVideoId())
                .teamName(entity.getTeamName())
                .rawLikes(entity.getRawLikes())
                .rawDislikes(entity.getRawDislikes())
                .dateCreated(formatToKST(entity.getDateCreated()))
                .collectedAt(formatToKST(entity.getCollectedAt()))
                .build();
    }
}
