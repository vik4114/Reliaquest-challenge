package com.reliaquest.api.utils;

import com.reliaquest.api.exception.ApiException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class Utils {

    public WebClient.ResponseSpec addExceptionHandling(WebClient.ResponseSpec retrieve) {
        return retrieve.onStatus((HttpStatusCode code) -> code.equals(HttpStatus.TOO_MANY_REQUESTS), resp -> {
                    log.warn("Received 429 Too Many Requests - creating ApiException for retry");
                    return resp.bodyToMono(String.class)
                            .defaultIfEmpty("")
                            .flatMap(body -> Mono.error(new ApiException(
                                    "Rate Limit Reached, try after some time", HttpStatus.TOO_MANY_REQUESTS)));
                })
                .onStatus((HttpStatusCode code) -> code.equals(HttpStatus.NOT_FOUND), resp -> {
                    log.warn("Received 404 client error - creating ApiException");
                    return resp.bodyToMono(String.class)
                            .map(body -> new ApiException("Employee Not Found", HttpStatus.NOT_FOUND));
                })
                .onStatus(HttpStatusCode::is4xxClientError, resp -> {
                    log.warn("Received 4xx client error - creating ApiException");
                    return resp.bodyToMono(String.class)
                            .map(body -> new ApiException(
                                    "Bad Request encountered from Server" + body, HttpStatus.BAD_REQUEST));
                })
                .onStatus((HttpStatusCode code) -> code.equals(HttpStatus.SERVICE_UNAVAILABLE), resp -> {
                    log.warn("Received 503 Service Unavailable - creating ApiException for retry");
                    return resp.bodyToMono(String.class)
                            .defaultIfEmpty("")
                            .flatMap(body -> Mono.error(new ApiException(
                                    "Service Unavailable, try after some time", HttpStatus.SERVICE_UNAVAILABLE)));
                })
                .onStatus(HttpStatusCode::is5xxServerError, resp -> {
                    log.warn("Received 5xx server error - creating ApiException");
                    return resp.bodyToMono(String.class)
                            .map(body -> new ApiException(
                                    "Internal Server Error: " + body, HttpStatus.INTERNAL_SERVER_ERROR));
                });
    }
}
