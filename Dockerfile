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
# Переходим на легковесный JRE 21 Alpine
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Копируем собранный JAR из этапа сборки
COPY --from=build /app/target/Mongo-microservice-1.0-SNAPSHOT.jar app.jar

# СОХРАНЯЕМ И КОРРЕКТИРУЕМ КОСТЫЛЬ С ПАПКАМИ И БЕЗОПАСНОСТЬЮ:
# Создаем директории для скриншотов (как в твоем старом файле) и создаем не-root пользователя.
# Дополнительно передаем права (chown) на эти папки пользователю spring, чтобы он мог туда писать.
RUN mkdir -p /src/main/resources/static/shots /src/main/resources/static/mongoprepareshots \
    && addgroup -S spring && adduser -S spring -G spring \
    && chown -R spring:spring /src/main/resources/static/shots /src/main/resources/static/mongoprepareshots

# Переключаемся на безопасного пользователя
USER spring:spring

# ОТКРЫВАЕМ ТОЧНЫЕ ПОРТЫ ИЗ PROPERTIES:
# 3333 - HTTP REST порт административных команд
# 6565 - Высокоскоростной gRPC сервер для стриминга тяжелой графики движку
EXPOSE 3333 6565

# Точка входа с поддержкой лимитов Docker, ZGC для Loom и флагом трассировки пиннинга
ENTRYPOINT ["java", \
            "-XX:+UseContainerSupport", \
            "-XX:+UseZGC", \
            "-Djdk.tracePinnedThreads=short", \
            "-jar", "app.jar", \
            "--spring.config.name=mongo-server"]

