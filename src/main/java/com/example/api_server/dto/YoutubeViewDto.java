package com.example.api_server.dto;

import com.example.api_server.entity.YoutubeView;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

import java.time.Instant;

import static com.example.api_server.util.TimeUtils.formatToKST;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class YoutubeViewDto {
    private String videoId;
    private String teamName;
    private Long viewCount;
    private Long commentCount;
    private String collectedAt; // KST로 포맷된 문자열

    public static YoutubeViewDto from(YoutubeView entity) {
        return YoutubeViewDto.builder()
                .videoId(entity.getVideoId())
                .teamName(entity.getTeamName())
                .viewCount(entity.getViewCount())
                .commentCount(entity.getCommentCount())
                .collectedAt(formatToKST(entity.getCollectedAt()))
                .build();
    }
}
