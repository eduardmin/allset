FROM eclipse-temurin:21-jre
WORKDIR /app
# Must match EXPOSE and EB / ALB container port; Spring defaults to 8080 without this.
ENV SERVER_PORT=5000
COPY app.jar /app/app.jar
EXPOSE 5000
ENTRYPOINT ["java", "-Xmx512m", "-Xms256m", "-jar", "/app/app.jar"]
