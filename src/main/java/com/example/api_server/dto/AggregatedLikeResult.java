package com.example.api_server.dto;

import com.example.api_server.entity.YoutubeLike;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AggregatedLikeResult {
    private String videoId;
    private List<YoutubeLike> latestTwo;
}
