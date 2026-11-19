# Build stage
FROM eclipse-temurin:27-jdk-alpine AS builder
WORKDIR /workspace
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn
RUN chmod +x ./mvnw
COPY src src
RUN ./mvnw clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:27-jre-alpine
WORKDIR /app
RUN addgroup -S items && adduser -S items -G items
USER items
COPY --from=builder /workspace/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
