package com.animegen.api.handler;

import com.animegen.common.ApiResponse;
import com.animegen.common.BizException;
import com.animegen.common.ErrorCodes;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BizException.class)
    public ApiResponse<Void> handleBiz(BizException ex) {
        log.warn("biz error code={}, msg={}, traceId={}", ex.getCode(), ex.getMessage(), MDC.get("traceId"));
        return ApiResponse.fail(ex.getCode(), ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiResponse<Void> handleValidation(MethodArgumentNotValidException ex) {
        FieldError fieldError = ex.getBindingResult().getFieldError();
        String msg = fieldError == null ? "invalid request" : fieldError.getField() + " " + fieldError.getDefaultMessage();
        return ApiResponse.fail(ErrorCodes.INVALID_PARAM, msg);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ApiResponse<Void> handleConstraint(ConstraintViolationException ex) {
        return ApiResponse.fail(ErrorCodes.INVALID_PARAM, ex.getMessage());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ApiResponse<Void> handleBadJson(HttpMessageNotReadableException ex) {
        return ApiResponse.fail(ErrorCodes.INVALID_PARAM, "invalid request body");
    }

    @ExceptionHandler(Exception.class)
    public ApiResponse<Void> handleUnknown(Exception ex) {
        log.error("unknown error traceId={}", MDC.get("traceId"), ex);
        return ApiResponse.fail(ErrorCodes.INTERNAL_ERROR, ex.getMessage());
    }
}
