FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /build

COPY pom.xml .
COPY src ./src

RUN ./mvnw package -DskipTests -B

FROM eclipse-temurin:21-jre-jammy

LABEL maintainer="hotel-room-booking-system"
LABEL version="1.0.0"
LABEL description="Backend Engineer Coding Challenge"

RUN useradd -m appuser
WORKDIR /app

COPY --from=build /build/target/*.jar /app/app.jar

USER appuser
EXPOSE 8080

ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -DJava.security.egd=file:/dev/./urandom"
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]