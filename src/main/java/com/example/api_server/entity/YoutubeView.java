package com.example.api_server.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "views")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class YoutubeView {
    @Id
    private String id;
    private String videoId;
    private String teamName;
    private Long viewCount;
    private Long commentCount;
    private Instant collectedAt;
}
