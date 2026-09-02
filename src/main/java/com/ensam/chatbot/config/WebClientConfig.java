package com.ensam.chatbot.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

@Configuration
@EnableConfigurationProperties(OllamaProperties.class)
public class WebClientConfig {
    @Bean
    public WebClient ollamaWebClient(OllamaProperties properties) {
        HttpClient httpClient = HttpClient.create()
                .responseTimeout(properties.responseTimeout())
                .option(io.netty.channel.ChannelOption.CONNECT_TIMEOUT_MILLIS,
                        Math.toIntExact(properties.connectTimeout().toMillis()));

        return WebClient.builder()
                .baseUrl(properties.baseUrl())
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }
}
