package com.app.service.rest.mongoServer.configuration;

import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.serverfactory.GrpcServerConfigurer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Configuration
@Slf4j // 👈 Добавляем логгер через Lombok
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
        // 🎯 ВПЕНДЮРИВАЕМ МАРКИРОВКУ СЮДА:
        log.info("🚀🚀🚀 [MONGO-SERVER v3.0] КОНТЕЙНЕР УСПЕШНО ОБНОВЛЕН! GridFS и виртуальные потоки инициализированы.");

        // Создает экзекутор, который под каждую задачу выделяет новый виртуальный поток
        return Executors.newVirtualThreadPerTaskExecutor();
    }
}
