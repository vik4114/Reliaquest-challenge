package com.reliaquest.api.utils;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Retryable(
        retryFor = {com.reliaquest.api.exception.ApiException.class},
        exceptionExpression = "#root.status.value() == 429 || #root.status.value() == 503",
        maxAttemptsExpression = "#{${retry.max-attempts}}",
        listeners = {"customRetryListener"},
        backoff =
        @Backoff(
                delayExpression = "#{${retry.delay-ms}}",
                multiplierExpression = "#{${retry.multiplier}}",
                maxDelayExpression = "#{${retry.max-delay-ms}}",
                randomExpression = "#{${retry.jitter}}"))
public @interface Retry {}

