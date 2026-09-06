# ==============================================================================
# Этап 1: Сборка gRPC-классов и JAR-артефакта (Среда Ubuntu для работы с protoc)
# ==============================================================================
# Берем стандартный образ Maven (не Alpine), чтобы os-maven-plugin корректно
# скачал бинарники protoc под glibc Linux
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# 1. Копируем pom.xml и кэшируем зависимости с использованием монтирования кэша
COPY pom.xml .
RUN --mount=type=cache,target=/root/.m2 mvn dependency:go-offline -B

# 2. Копируем исходный код приложения и собираем проект (также с кэшем .m2)
COPY src ./src
RUN --mount=type=cache,target=/root/.m2 mvn clean package -DskipTests

# ==============================================================================
# Этап 2: Финальный высокопроизводительный рантайм (Ultra-Low Latency GridFS)
# ==============================================================================
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Копируем собранный JAR
COPY --from=build /app/target/Mongo-microservice-1.0-SNAPSHOT.jar app.jar

# Создаем пользователя spring
RUN addgroup -S spring && adduser -S spring -G spring

# 🛠 СОЗДАЕМ ЛОКАЛЬНЫЕ ПАПКИ И КОПИРУЕМ СТАТИКУ ИЗ ИСХОДНИКОВ
# Создаем папки shots и mongoPrepareShots прямо в рабочей директории /app
RUN mkdir -p shots mongoPrepareShots

# Копируем оригинальные картинки-заглушки в созданную папку
COPY --from=build /app/src/main/resources/static/mongoPrepareShots/ mongoPrepareShots/

# Выдаем права пользователю spring
RUN chown -R spring:spring shots mongoPrepareShots

# Переключаемся на безопасного пользователя
USER spring:spring

EXPOSE 3333 6565

ENTRYPOINT ["java", \
            "-XX:+UseContainerSupport", \
            "-XX:+UseZGC", \
            "-Djdk.tracePinnedThreads=short", \
            "-jar", "app.jar", \
            "--spring.config.name=mongo-server"]


