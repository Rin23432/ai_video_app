package com.animegen.common;

public final class AuthContext {
    private static final ThreadLocal<Long> USER_HOLDER = new ThreadLocal<>();

    private AuthContext() {
    }

    public static void setUserId(Long userId) {
        USER_HOLDER.set(userId);
    }

    public static Long getUserId() {
        return USER_HOLDER.get();
    }

    public static void clear() {
        USER_HOLDER.remove();
    }
}
