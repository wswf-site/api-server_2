package com.example.api_server.dto;

import com.example.api_server.entity.YoutubeView;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AggregatedViewResult {
    private String videoId;
    private List<YoutubeView> latestTwo;
}
