# Build stage
FROM gradle:8.7-jdk21 AS build
WORKDIR /src
COPY . .
RUN gradle build -x test

# Run stage
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /src/build/libs/*-SNAPSHOT.jar /app/app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
