package com.animegen.service.ranking;

public final class RankingConstants {
    private RankingConstants() {
    }

    public static final String WINDOW_DAILY = "daily";
    public static final String WINDOW_WEEKLY = "weekly";
    public static final String WINDOW_MONTHLY = "monthly";

    public static final String RANK_CONTENT = "content";
    public static final String RANK_AUTHOR = "author";
    public static final String RANK_TAG = "tag";

    public static final String EVENT_CONTENT_PUBLISHED = "CONTENT_PUBLISHED";
    public static final String EVENT_LIKE_CREATED = "LIKE_CREATED";
    public static final String EVENT_LIKE_CANCELED = "LIKE_CANCELED";
    public static final String EVENT_FAVORITE_CREATED = "FAVORITE_CREATED";
    public static final String EVENT_FAVORITE_CANCELED = "FAVORITE_CANCELED";
    public static final String EVENT_COMMENT_CREATED = "COMMENT_CREATED";
    public static final String EVENT_COMMENT_DELETED = "COMMENT_DELETED";

    public static String normalizeWindow(String window) {
        if (window == null) {
            return WINDOW_WEEKLY;
        }
        String lower = window.trim().toLowerCase();
        if (WINDOW_DAILY.equals(lower) || WINDOW_WEEKLY.equals(lower) || WINDOW_MONTHLY.equals(lower)) {
            return lower;
        }
        return WINDOW_WEEKLY;
    }

    public static String zsetContent(String window) {
        return "zset:rank:content:" + normalizeWindow(window);
    }

    public static String zsetAuthor(String window) {
        return "zset:rank:author:" + normalizeWindow(window);
    }

    public static String zsetTag(String window) {
        return "zset:rank:tag:" + normalizeWindow(window);
    }
}
