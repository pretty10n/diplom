# Core Form Service

## Запуск

Требования:
- Java 21
- Maven 3.9+
- Postgres на `localhost:5432` (по умолчанию `core_form_service/postgres/postgres`)

В проекте зафиксирован путь JDK для Maven/Spring Boot: `C:\Program Files\Java\jdk-21.0.11`.

### Рекомендуемый dev-запуск

```bash
mvn package -DskipTests
java -jar target/core-form-service-0.0.1-SNAPSHOT.jar
```

### Примечание по `spring-boot:run`

В некоторых локальных окружениях с нестандартной JDK команда `mvn spring-boot:run`
может завершаться ошибкой `ClassNotFoundException: jd.ru.App`.
Рабочий путь для разработки и проверки API — запуск через собранный `jar`.
