package com.reliaquest.api.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

@Configuration
@EnableRetry
@EnableAspectJAutoProxy(exposeProxy = true)
@Slf4j
public class AppConfig {

    @Value("${web-client.config.connection-timeout}")
    private int connectionTimeoutMillis;

    @Value("${web-client.config.read-timeout}")
    private int readTimeoutMillis;

    @Value("${web-client.config.write-timeout}")
    private int writeTimeoutMillis;

    @Value("${web-client.config.response-timeout}")
    private int responseTimeoutMillis;

    @Bean
    public WebClient webClient() throws Exception {
        try {
            log.info(
                    "Initializing WebClient with connectionTimeout={}ms, readTimeout={}ms, writeTimeout={}ms, responseTimeout={}ms",
                    connectionTimeoutMillis,
                    readTimeoutMillis,
                    writeTimeoutMillis,
                    responseTimeoutMillis);

            // SSL context allowing all certificates (for local/mock API)
            SslContext sslContext = SslContextBuilder.forClient()
                    .trustManager(InsecureTrustManagerFactory.INSTANCE)
                    .build();

            // Configure WebClient exchange strategies (max in-memory size)
            ExchangeStrategies strategies = ExchangeStrategies.builder()
                    .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(-1))
                    .build();

            // Configure Reactor Netty HttpClient with timeouts
            HttpClient httpClient = HttpClient.create()
                    .secure(spec -> spec.sslContext(sslContext))
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectionTimeoutMillis)
                    .responseTimeout(Duration.ofMillis(responseTimeoutMillis))
                    .doOnConnected(conn -> conn.addHandlerLast(
                                    new ReadTimeoutHandler(readTimeoutMillis, TimeUnit.MILLISECONDS))
                            .addHandlerLast(new WriteTimeoutHandler(writeTimeoutMillis, TimeUnit.MILLISECONDS)));

            // Build WebClient
            return WebClient.builder()
                    .exchangeStrategies(strategies)
                    .clientConnector(new ReactorClientHttpConnector(httpClient))
                    .build();

        } catch (Exception e) {
            log.error("Failed to create WebClient instance", e);
            throw e;
        }
    }
}
