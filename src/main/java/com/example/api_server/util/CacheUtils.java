package com.example.api_server.util;

import java.time.Duration;

public class CacheUtils {

    public static final String LATEST_STATS_CACHE_KEY = "youtube:latest:stats";
    public static final String LATEST_LIKES_CACHE_KEY = "youtube:latest:likes";
    public static final String LIKE_HISTORY_CACHE_PREFIX = "youtube:history:likes:";
    public static final String VIEW_HISTORY_CACHE_PREFIX = "youtube:history:views:";


    public static final Duration RECENT_TTL = Duration.ofMinutes(5);
    public static final Duration RECENT_LIKE_TTL = Duration.ofMinutes(30);
    public static final Duration TODAY_CACHE_TTL = Duration.ofHours(1);
    public static final Duration PAST_CACHE_TTL = Duration.ofDays(7);

}
