FROM maven:3.9.6-eclipse-temurin-17 AS build

WORKDIR /app

COPY . .

RUN mvn clean package

FROM eclipse-temurin:17

WORKDIR /app

COPY --from=build /app/target/*jar-with-dependencies.jar app.jar

EXPOSE 10000

CMD ["java", "-jar", "app.jar"]