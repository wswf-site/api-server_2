package com.example.api_server.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class YoutubeViewResponseDto {
    List<YoutubeViewDto> recentHourData;
    Map<String, List<YoutubeViewDto>> dailyData;
}
