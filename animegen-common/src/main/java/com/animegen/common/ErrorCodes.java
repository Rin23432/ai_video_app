package com.animegen.common;

public final class ErrorCodes {
    private ErrorCodes() {
    }

    public static final int INVALID_PARAM = 4000;
    public static final int DEVICE_ID_REQUIRED = 4001;
    public static final int UNAUTHORIZED = 4010;
    public static final int LOGIN_REQUIRED = 40100;
    public static final int WORK_NOT_FOUND = 4040;
    public static final int TASK_NOT_FOUND = 4041;
    public static final int CONTENT_NOT_FOUND = 4042;
    public static final int COMMENT_NOT_FOUND = 4043;
    public static final int TAG_NOT_FOUND = 4044;
    public static final int DUPLICATE_REQUEST_IN_PROGRESS = 4090;
    public static final int CONTENT_ALREADY_PUBLISHED = 4091;
    public static final int PERMISSION_DENIED = 4030;
    public static final int CONTENT_NOT_READY = 4092;
    public static final int MALL_SKU_NOT_FOUND = 4404;
    public static final int MALL_ORDER_NOT_FOUND = 4405;
    public static final int MALL_STOCK_NOT_ENOUGH = 4409;
    public static final int MALL_ORDER_STATUS_INVALID = 4410;
    public static final int SENSITIVE_WORD_HIT = 4510;
    public static final int ENQUEUE_FAILED = 5001;
    public static final int TASK_SUCCESS_WITHOUT_VIDEO = 5002;
    public static final int INTERNAL_ERROR = 5000;
}
