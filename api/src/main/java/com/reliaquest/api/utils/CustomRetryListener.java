package com.reliaquest.api.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.RetryListener;
import org.springframework.stereotype.Component;

@Component("customRetryListener")
@Slf4j
public class CustomRetryListener implements RetryListener {

    @Override
    public <T, E extends Throwable> void onError(
            RetryContext context, RetryCallback<T, E> callback, Throwable throwable) {
        int attemptNumber = context.getRetryCount() + 1;
        log.warn(
                "Retry attempt #{} failed for method: {} - Error: {}",
                attemptNumber,
                getMethodName(context),
                throwable.getMessage());
    }

    @Override
    public <T, E extends Throwable> void close(
            RetryContext context, RetryCallback<T, E> callback, Throwable throwable) {
        if (throwable != null) {
            int totalAttempts = context.getRetryCount() + 1;
            log.error(
                    "All {} retry attempts exhausted for method: {} - Final error: {}",
                    totalAttempts,
                    getMethodName(context),
                    throwable.getMessage());
        }
    }

    @Override
    public <T, E extends Throwable> boolean open(RetryContext context, RetryCallback<T, E> callback) {
        log.info("Starting retryable operation for method: {}", getMethodName(context));
        return true;
    }

    private String getMethodName(RetryContext context) {
        return context.getAttribute("context.name") != null
                ? context.getAttribute("context.name").toString()
                : "unknown";
    }
}
