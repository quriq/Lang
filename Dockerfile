# Этап 1: Сборка через Maven (Java 21)
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Копируем pom.xml и загружаем зависимости (кэшируется)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Копируем исходники и собираем
COPY src ./src
RUN mvn clean package -DskipTests -B

# Этап 2: Минимальный образ для запуска (Java 21 JRE)
FROM eclipse-temurin:21-jre
WORKDIR /app

# Копируем собранный JAR
COPY --from=build /app/target/*.jar app.jar

# Render прокидывает PORT через переменную окружения
EXPOSE 10000

# Запуск с учётом PORT от Render
ENTRYPOINT ["sh", "-c", "java -Dserver.port=${PORT:-10000} -jar app.jar"]