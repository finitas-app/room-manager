FROM amazoncorretto:17-alpine-jdk

VOLUME /tmp
WORKDIR /app
COPY . .
RUN ./gradlew clean build -x test
ENTRYPOINT ["java", "-jar", "./build/libs/room-manageer-0.0.1.jar"]
