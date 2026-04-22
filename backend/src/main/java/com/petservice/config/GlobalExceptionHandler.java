package com.petservice.config;

import com.petservice.common.BusinessException;
import com.petservice.common.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BusinessException.class)
    public Result<?> handleBusinessException(BusinessException e) {
        log.warn("业务异常: {}", e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<?> handleValidationException(MethodArgumentNotValidException e) {
        var fieldError = e.getBindingResult().getFieldError();
        String message = (fieldError != null)
            ? fieldError.getDefaultMessage()
            : "参数校验失败";
        return Result.error(400, message);
    }

    @ExceptionHandler(BindException.class)
    public Result<?> handleBindException(BindException e) {
        var fieldError = e.getFieldError();
        String message = (fieldError != null)
            ? fieldError.getDefaultMessage()
            : "参数绑定失败";
        return Result.error(400, message);
    }

    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception e) {
        log.error("系统异常", e);
        String detail = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
        // 优先显示根因
        Throwable cause = e.getCause();
        while (cause != null) {
            if (cause.getMessage() != null) detail = cause.getMessage();
            cause = cause.getCause();
        }
        return Result.error(500, "服务器内部错误: " + detail);
    }
}
