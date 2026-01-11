# Этап 1: Сборка приложения
FROM maven:3.9.4-eclipse-temurin-17 AS build
WORKDIR /app

# Копируем pom.xml и скачиваем зависимости (кеширование)
COPY pom.xml .
RUN mvn dependency:go-offline

# Копируем исходный код и собираем jar
COPY src ./src
RUN mvn clean package -DskipTests

# Этап 2: Создание финального образа
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Копируем собранный jar из первого этапа
COPY --from=build /app/target/mongo-microservice-1.0-snapshot.jar app.jar

# Создаем директории для статики, указанные в ваших проперти
RUN mkdir -p /src/main/resources/static/shots /src/main/resources/static/mongoprepareshots

# Открываем порт сервиса
EXPOSE 3333

# Запуск приложения
ENTRYPOINT ["java", "-jar", "app.jar"]
