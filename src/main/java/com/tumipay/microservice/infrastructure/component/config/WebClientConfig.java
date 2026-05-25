package com.tumipay.microservice.infrastructure.component.config;

import com.tumipay.microservice.shared.properties.WebClientProperties;
import io.netty.channel.ChannelOption;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

/**
 * WebClientConfig
 * <p>
 * WebClientConfig class.
 * <p>
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 3/03/2026
 */
@Configuration
@EnableConfigurationProperties(WebClientProperties.class)
public class WebClientConfig {

    @Bean
    public WebClient webClient(WebClientProperties properties) {

        final ConnectionProvider provider = ConnectionProvider.builder("custom-pool")
            .maxConnections(properties.getPool().getMaxConnections())
            .pendingAcquireTimeout(properties.getPool().getPendingAcquireTimeout())
            .maxIdleTime(properties.getPool().getMaxIdleTime())
            .maxLifeTime(properties.getPool().getMaxLifeTime())
            .evictInBackground(properties.getPool().getEvictInBackground())
            .build();

        final HttpClient httpClient = HttpClient.create(provider)
            .responseTimeout(properties.getTimeout().getResponse())
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS,
                (int) properties.getTimeout().getConnect().toMillis())
            .keepAlive(properties.getTcp().isKeepAlive());

        return WebClient.builder()
            .clientConnector(new ReactorClientHttpConnector(httpClient))
            .build();
    }
}
