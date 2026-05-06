package com.example.demo.global.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Configuration
public class WebClientConfig {

    @Value("${ai.server.url}")
    private String aiServerUrl;

    @Bean
    public WebClient aiWebClient(WebClient.Builder builder) {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 300000)
                .responseTimeout(Duration.ofMinutes(5))
                .doOnConnected(conn -> conn
                        .addHandlerLast(new ReadTimeoutHandler(300, TimeUnit.SECONDS))
                        .addHandlerLast(new WriteTimeoutHandler(300, TimeUnit.SECONDS)));
        return builder
                .baseUrl(aiServerUrl)
                .build();
    }
}
