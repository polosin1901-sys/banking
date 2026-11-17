# offersToClients

Сервис персональных рекомендаций банковских продуктов: REST API, конструктор динамических правил,
статистика срабатываний и Telegram-бот.

## Стек технологий

| Слой | Технологии |
|---|---|
| Язык и платформа | Java 17, Spring Boot 4.1.2 |
| Web | Spring MVC (REST), Jackson |
| Доступ к данным | Spring Data JPA (Hibernate), Spring JDBC, HikariCP |
| Базы данных | PostgreSQL 18 (динамические правила и статистика), H2 2.4 (клиентские данные, read-only) |
| Миграции | Liquibase |
| Кэширование | Caffeine |
| Интеграции | Telegram Bot API (`java-telegram-bot-api` 8.3.0) |
| Сборка | Maven Wrapper, Spring Boot Maven Plugin (`build-info`) |
| Тесты | JUnit 5, AssertJ, Spring Boot Test |

## Что делает приложение

1. **Рекомендации.** По `user_id` сервис проверяет три статических правила из первого задания
   (Invest500, Top Saving, Простой кредит) и все динамические правила, созданные менеджером,
   после чего возвращает объединённый список без дублей.
2. **Управление правилами.** Менеджер создаёт, просматривает и удаляет динамические правила
   через REST API. Каждое правило — это набор запросов (`USER_OF`, `ACTIVE_USER_OF`,
   `TRANSACTION_SUM_COMPARE`, `TRANSACTION_SUM_COMPARE_DEPOSIT_WITHDRAW`).
3. **Статистика.** При каждом срабатывании динамического правила счётчик увеличивается атомарно;
   `GET /rule/stats` показывает все правила, включая те, что ещё ни разу не срабатывали.
4. **Telegram-бот.** `/start` показывает справку, `/recommend <username>` возвращает рекомендации
   для клиента по его логину.
5. **Сервисные эндпоинты.** `/management/info` отдаёт название и версию сборки,
   `/management/clear-caches` очищает кэши рекомендаций.

## Быстрый старт

```powershell
# 1. PostgreSQL должен быть запущен, база и пользователь созданы (см. инструкцию по развёртыванию)
$env:JAVA_HOME='C:\Users\<user>\.jdks\corretto-17.0.19'

# 2. Сборка и тесты
.\mvnw.cmd clean package

# 3. Запуск
java -jar target\offersToClients-0.0.1-SNAPSHOT.jar

# 4. Проверка
curl.exe http://localhost:8080/management/info
```

Telegram-бот включается переменной окружения `TELEGRAM_BOT_TOKEN`. Без неё приложение работает,
но бот не отвечает.

## Эндпоинты

| Метод | Путь | Назначение |
|---|---|---|
| GET | `/recommendation/{user_id}` | рекомендации клиента |
| POST | `/rule` | создать динамическое правило |
| GET | `/rule` | список правил |
| DELETE | `/rule/{id}` | удалить правило |
| GET | `/rule/stats` | статистика срабатываний |
| GET | `/management/info` | название и версия приложения |
| POST | `/management/clear-caches` | очистить кэши |

Полная спецификация: [`docs/api/openapi.yaml`](docs/api/openapi.yaml).
Swagger UI после запуска: <http://localhost:8080/swagger-ui.html>.

## Документация

| Документ | Содержимое |
|---|---|
| [Требования](docs/Requirements.md) | user stories, нефункциональные требования, диаграмма прецедентов |
| [Трассировка](docs/Traceability.md) | связь требований с кодом и задачами |
| [Архитектура](docs/Architecture.md) | диаграммы компонентов и деятельности, описание взаимодействия |
| [REST API](docs/REST-API.md) | описание эндпоинтов и схем |
| [OpenAPI / Swagger](docs/OpenAPI.md) | спецификация и запуск Swagger UI |
| [Развёртывание](docs/Deployment.md) | сервисы, сборка, запуск, переменные среды |
| [Чек-лист проверки](docs/QA-Checklist.md) | сквозные сценарии проверки трёх ТЗ |
| [Трекер задач](docs/Tracker.md) | задачи GitHub, доска проекта, публикация Wiki |

Публичные ссылки: [Wiki](https://github.com/polosin1901-sys/banking/wiki) ·
[доска проекта](https://github.com/users/polosin1901-sys/projects/2) ·
[задачи](https://github.com/polosin1901-sys/banking/issues?q=is%3Aissue) ·
[реализация](https://github.com/polosin1901-sys/banking/pull/19).

## Структура проекта

```text
src/main/java/com/bankingApp/offersToClients
├── cache/          Caffeine-кэши агрегатов
├── configuration/  два источника данных (PostgreSQL и H2)
├── controller/     REST-контроллеры
├── dto/            модели запросов и ответов
├── entity/         JPA-сущности динамических правил и статистики
├── model/          доменные перечисления и value-объекты
├── repository/     JPA-репозитории и JDBC-запросы
├── rules/          статические правила из первого ТЗ
├── service/        бизнес-логика рекомендаций, правил и статистики
│   └── dynamic/    вычислители запросов динамических правил
└── telegram/       long polling, разбор команд и отправка сообщений
```

## Переменные среды

| Переменная | Назначение | Обязательна |
|---|---|---|
| `SPRING_DATASOURCE_URL` | JDBC-URL PostgreSQL | да в production |
| `SPRING_DATASOURCE_USERNAME` | пользователь PostgreSQL | да в production |
| `SPRING_DATASOURCE_PASSWORD` | пароль PostgreSQL | да в production |
| `APPLICATION_RECOMMENDATIONS_DB_URL` | JDBC-URL H2 с клиентскими данными | да в production |
| `TELEGRAM_BOT_TOKEN` | токен бота из BotFather | нет (бот выключен без него) |
| `TELEGRAM_BOT_PROXY_HOST` / `_PORT` | HTTP-прокси для доступа к Telegram | нет |
| `SERVER_PORT` | порт приложения (по умолчанию 8080) | нет |

Подробности — в [инструкции по развёртыванию](docs/Deployment.md).
