FROM eclipse-temurin:17-jre AS build
WORKDIR /build

COPY mvnw .
COPY mvnw.cmd .
COPY pom.xml .

COPY .mvn/ .mvn/

RUN chmod +x mvnw

RUN ./mvnw dependency:go-offline -B

COPY src src

RUN ./mvnw package -DskipTests -B

FROM eclipse-temurin:17-jre-alpine

LABEL maintainer="hotel-room-booking-system"
LABEL version="1.0.0"
LABEL description="Backend Engineer Coding Challenge"

RUN apk add --no-cache curl

RUN addgroup -S appgroup && adduser -S appuser -G appgroup

WORKDIR /app

COPY --from=build /build/target/*.jar app.jar

RUN chown -R appuser:appgroup /app

USER appuser

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=5s --start-period=10s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -DJava.security.egd=file:/dev/./urandom"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]