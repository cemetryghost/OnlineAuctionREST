# Используем официальный образ OpenJDK версии 22
FROM openjdk:22-jdk

# Устанавливаем рабочую директорию внутри контейнера
WORKDIR /app

# Копируем JAR-файл вашего приложения в рабочую директорию
COPY target/*.jar app.jar

# Копируем файлы конфигурации в рабочую директорию
COPY src/main/resources/application.properties /app/resources/

# Задаем точку входа для запуска вашего приложения при старте контейнера
ENTRYPOINT ["java", "-jar", "app.jar"]
