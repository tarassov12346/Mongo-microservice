# --- Этап 1: Сборка ---
FROM maven:3.9.4-eclipse-temurin-17-alpine AS build
WORKDIR /app

# 1. Кешируем зависимости (используем --mount для экономии места)
COPY pom.xml .
RUN --mount=type=cache,target=/root/.m2 mvn dependency:go-offline

# 2. Сборка (также с кешированием .m2)
COPY src ./src
RUN --mount=type=cache,target=/root/.m2 mvn clean package -DskipTests

# --- Этап 2: Финальный образ ---
# Используем максимально легкий образ (JRE Alpine)
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Копируем только нужный JAR
COPY --from=build /app/target/*.jar app.jar

# Создаем директории (объединяем в одну команду для уменьшения слоев)
RUN mkdir -p /src/main/resources/static/shots /src/main/resources/static/mongoprepareshots \
    && addgroup -S spring && adduser -S spring -G spring

# Безопасность: запускаем от не-root пользователя
USER spring:spring

EXPOSE 3333

ENTRYPOINT ["java", "-jar", "app.jar"]

