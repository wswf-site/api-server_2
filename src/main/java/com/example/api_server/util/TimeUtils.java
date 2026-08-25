package com.example.api_server.util;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class TimeUtils {
    public static final ZoneId KST_ZONE_ID = ZoneId.of("Asia/Seoul");

    public static String formatToKST(Instant utc) {
        if (utc == null) return null;
        return utc.atZone(KST_ZONE_ID)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    public static boolean isToday(LocalDate date) {
        return nowDateKST().equals(date);
    }

    public static List<LocalDate> excludeFutureDates(List<LocalDate> dates) {
        return dates.stream()
                .filter(date -> !date.isAfter(nowDateKST()))
                .collect(Collectors.toList());
    }

    public static LocalDate nowDateKST() {
        return LocalDate.now(KST_ZONE_ID);
    }
}
