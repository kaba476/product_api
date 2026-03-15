FROM eclipse-temurin:21-jdk-jammy

WORKDIR /app

COPY target/product-api-1.0-SNAPSHOT.jar app.jar

EXPOSE 8086

ENTRYPOINT ["java", "-jar", "app.jar"]
