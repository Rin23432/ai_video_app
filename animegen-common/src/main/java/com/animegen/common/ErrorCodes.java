package com.animegen.common;

public final class ErrorCodes {
    private ErrorCodes() {
    }

    public static final int INVALID_PARAM = 4000;
    public static final int DEVICE_ID_REQUIRED = 4001;
    public static final int UNAUTHORIZED = 4010;
    public static final int WORK_NOT_FOUND = 4040;
    public static final int TASK_NOT_FOUND = 4041;
    public static final int DUPLICATE_REQUEST_IN_PROGRESS = 4090;
    public static final int ENQUEUE_FAILED = 5001;
    public static final int TASK_SUCCESS_WITHOUT_VIDEO = 5002;
    public static final int INTERNAL_ERROR = 5000;
}
