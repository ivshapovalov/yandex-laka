FROM openjdk:17.0.1-jdk-slim
ARG JAR_FILE=/opt/app/build/libs/*SNAPSHOT.jar
WORKDIR /opt/app
EXPOSE 8080
ENV TZ="Europe/London"
COPY . /opt/app
RUN chmod u+x /opt/app/gradlew && /opt/app/gradlew clean -x test build \
  && cp ${JAR_FILE} /opt/app/yandex-lavka.jar
ENTRYPOINT ["java", "-Dspring.profiles.active=production", "-jar", "yandex-lavka.jar"]

