# OpenAPI / Swagger

Спецификация API ведётся в одном файле — [`docs/api/openapi.yaml`](api/openapi.yaml)
(OpenAPI 3.0.3). Это канонический источник описания эндпоинтов: он же публикуется приложением
по адресу `/openapi.yaml` и используется Swagger UI.

## Состав спецификации

| Группа | Эндпоинты |
|---|---|
| Recommendations | `GET /recommendation/{user_id}` |
| Rules | `GET /rule`, `POST /rule`, `DELETE /rule/{id}` |
| Statistics | `GET /rule/stats` |
| Management | `GET /management/info`, `POST /management/clear-caches` |

Для каждого метода указаны: назначение, параметры, схема запроса, схема ответа, коды `200/204`
и ошибки `400/404`, а также примеры. Переиспользуемые схемы вынесены в `components/schemas`:
`Recommendation`, `RecommendationResponse`, `Query`, `RuleRequest`, `RuleResponse`,
`RuleListResponse`, `RuleStat`, `RuleStatListResponse`, `ManagementInfo`, `ErrorResponse`.

## Просмотр после запуска

```powershell
java -jar target\offersToClients-0.0.1-SNAPSHOT.jar
```

| Адрес | Что отдаёт |
|---|---|
| <http://localhost:8080/swagger-ui.html> | интерактивная документация Swagger UI |
| <http://localhost:8080/openapi.yaml> | сама спецификация в YAML |

Swagger UI — статическая страница `src/main/resources/static/swagger-ui.html`, которая загружает
библиотеку с CDN (`unpkg.com/swagger-ui-dist`) и читает `/openapi.yaml`. Отдельная зависимость
`springdoc` не используется: спецификация остаётся рукописной и не расходится с кодом при
обновлении Spring Boot.

Файл спецификации попадает в JAR без дублирования: `maven-resources-plugin` копирует
`docs/api/openapi.yaml` в `target/classes/static/` на фазе `process-resources`.

## Просмотр в репозитории

GitHub показывает YAML как обычный текст. Для рендера можно открыть файл в
[Swagger Editor](https://editor.swagger.io/) и вставить его содержимое, либо использовать
локальный Swagger UI после запуска приложения.

## Правило поддержки

При изменении контроллера или DTO спецификация обновляется в том же коммите. Проверка:

```powershell
.\mvnw.cmd clean package
# после сборки файл должен существовать:
Test-Path target\classes\static\openapi.yaml
```
