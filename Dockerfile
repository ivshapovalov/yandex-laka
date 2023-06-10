FROM openjdk:17.0.1-jdk-slim

ARG JAR_FILE=/opt/app/build/libs/*SNAPSHOT.jar

WORKDIR /opt/app

# Данные для подключения к бд
ENV POSTGRES_SERVER=host.docker.internal
ENV POSTGRES_PORT=5432
ENV POSTGRES_DB=postgres
ENV POSTGRES_USER=postgres
ENV POSTGRES_PASSWORD=postgres

EXPOSE 8080

ENV TZ="Europe/London"

COPY . /opt/app

RUN chmod u+x /opt/app/gradlew && /opt/app/gradlew clean -x test build \
  && cp ${JAR_FILE} /opt/app/yandex-lavka.jar
ENTRYPOINT ["java", "-Dspring.profiles.active=production", "-jar", "yandex-lavka.jar"]

