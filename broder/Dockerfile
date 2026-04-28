# Multi-stage build for broder backend
# Stage 1: Build
FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder

WORKDIR /app

# Copy pom.xml and download dependencies (cache layer)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build
COPY src ./src
RUN mvn package -DskipTests -B

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Create data directory for SQLite
RUN mkdir -p /app/data

# Copy built application
COPY --from=builder /app/target/quarkus-app/lib/ /app/lib/
COPY --from=builder /app/target/quarkus-app/*.jar /app/
COPY --from=builder /app/target/quarkus-app/app/ /app/app/
COPY --from=builder /app/target/quarkus-app/quarkus/ /app/quarkus/

EXPOSE 8080

ENV JAVA_OPTS="-Dquarkus.http.host=0.0.0.0"
ENV PROMETHEUS_URL=http://prometheus:9090
ENV CORS_ORIGINS=http://localhost:3000

ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -jar /app/quarkus-run.jar" ]
