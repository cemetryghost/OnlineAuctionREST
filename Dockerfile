FROM openjdk:22-jdk

WORKDIR /app

COPY target/*.jar app.jar

COPY src/main/resources/application.properties /app/resources/

ENTRYPOINT ["java", "-jar", "app.jar"]
