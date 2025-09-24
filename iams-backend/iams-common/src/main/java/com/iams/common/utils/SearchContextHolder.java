package com.iams.common.utils;

public class SearchContextHolder {
    private static final ThreadLocal<Integer> RESTRICTED_COUNT = new ThreadLocal<>();

    public static void setRestrictedCount(int count) {
        RESTRICTED_COUNT.set(count);
    }

    public static int getAndClearRestrictedCount() {
        Integer count = RESTRICTED_COUNT.get();
        RESTRICTED_COUNT.remove(); // 获取后立即清除，防止内存泄漏
        return count != null ? count : 0;
    }
}
