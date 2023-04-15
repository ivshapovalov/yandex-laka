FROM openjdk:17.0.1-jdk-slim
ARG JAR_FILE=/opt/app/build/libs/*SNAPSHOT.jar
WORKDIR /opt/app
ENV TZ="Europe/London"
COPY . /opt/app
RUN chmod u+x /opt/app/gradlew && /opt/app/gradlew clean test build
RUN cp ${JAR_FILE} /opt/app/yandex-lavka.jar
ENTRYPOINT ["java", "-Dspring.profiles.active=production", "-jar", "yandex-lavka.jar"]

