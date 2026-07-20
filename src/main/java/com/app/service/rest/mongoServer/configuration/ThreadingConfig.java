package com.app.service.rest.mongoServer.configuration;

import net.devh.boot.grpc.server.serverfactory.GrpcServerConfigurer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Configuration
public class ThreadingConfig {
    // ЭЛЕГАНТНЫЙ ПЕРЕКЛЮЧАТЕЛЬ gRPC НА ВИРТУАЛЬНЫЕ ПОТОКИ (ИЗ ВТОРОГО ПРОЕКТА)
    @Bean
    @ConditionalOnProperty(name = "spring.threads.virtual.enabled", havingValue = "true")
    public GrpcServerConfigurer grpcVirtualThreadsConfigurer() {
        return serverBuilder -> serverBuilder.executor(Executors.newVirtualThreadPerTaskExecutor());
    }

    // Для ваших CompletableFuture в контроллерах
    @Bean(name = "virtualThreadExecutor")
    public Executor virtualThreadExecutor() {
        // Создает экзекутор, который под каждую задачу выделяет новый виртуальный поток
        return Executors.newVirtualThreadPerTaskExecutor();
    }
}
