# Развёртывание

Инструкция описывает запуск приложения в рабочей среде с нуля: какие сервисы нужны,
как собрать JAR и как связать приложение с базами данных через переменные среды.

## 1. Необходимые сервисы и условия

| Компонент | Версия / требование | Обязателен | Назначение |
|---|---|---|---|
| JRE/JDK | 17 (проверено на Corretto 17.0.19) | да | запуск приложения |
| PostgreSQL | 15+ (проверено на 18.6) | да | динамические правила и статистика |
| Файл H2 с клиентскими данными | H2 2.4.x, файл `transaction.mv.db` | да | клиенты, продукты, транзакции |
| Maven Wrapper | входит в репозиторий (`mvnw`, `mvnw.cmd`) | только для сборки | сборка JAR |
| Telegram Bot API | исходящий HTTPS до `api.telegram.org` | нет | работа бота |
| Свободный TCP-порт | по умолчанию 8080 | да | HTTP API |

H2 не требует отдельного сервиса: база открывается приложением как файл. PostgreSQL должен быть
запущен до старта приложения.

## 2. Подготовка PostgreSQL

```sql
CREATE USER offers_user WITH PASSWORD 'offers_password';
CREATE DATABASE offers_db OWNER offers_user;
```

Схему создаёт Liquibase при первом запуске: таблицы `recommendation_rule`, `rule_query`,
`rule_query_argument`, `rule_stat` и служебные таблицы миграций. Отдельно выполнять DDL не нужно,
Hibernate работает в режиме `validate` и только проверяет соответствие схемы сущностям.

## 3. Подготовка данных H2

Приложению нужен файл H2 с таблицами `users`, `products`, `transactions`.
По умолчанию используется относительный путь `./transaction`, то есть файл `transaction.mv.db`
в рабочем каталоге процесса.

Варианты:

- скопировать подготовленный файл `transaction.mv.db` в каталог запуска;
- указать абсолютный путь через переменную `APPLICATION_RECOMMENDATIONS_DB_URL`
  (рекомендуется для production), например:

```bash
APPLICATION_RECOMMENDATIONS_DB_URL=jdbc:h2:file:/var/lib/offers/transaction
```

Каталог должен быть доступен процессу на чтение. Запись не требуется: соединение открывается
в режиме `readOnly`.

## 4. Сборка JAR

Из корня проекта:

```bash
./mvnw clean package
```

Windows PowerShell:

```powershell
.\mvnw.cmd clean package
```

Результат: `target/offersToClients-0.0.1-SNAPSHOT.jar`.
Сборка выполняет тесты; чтобы пропустить их, добавьте `-DskipTests`.
Плагин `spring-boot-maven-plugin` формирует исполняемый JAR, а цель `build-info` создаёт
`META-INF/build-info.properties` — из него `/management/info` берёт название и версию.

## 5. Настройка конфигурации

Значения из `application.properties` переопределяются переменными среды по правилу
«точки → подчёркивания, верхний регистр».

| Свойство Spring Boot | Переменная среды | Пример | Обязательна |
|---|---|---|---|
| `spring.datasource.url` | `SPRING_DATASOURCE_URL` | `jdbc:postgresql://db:5432/offers_db` | да |
| `spring.datasource.username` | `SPRING_DATASOURCE_USERNAME` | `offers_user` | да |
| `spring.datasource.password` | `SPRING_DATASOURCE_PASSWORD` | `********` | да |
| `spring.datasource.driver-class-name` | `SPRING_DATASOURCE_DRIVER_CLASS_NAME` | `org.postgresql.Driver` | нет |
| `application.recommendations-db.url` | `APPLICATION_RECOMMENDATIONS_DB_URL` | `jdbc:h2:file:/var/lib/offers/transaction` | да |
| `telegram.bot.token` | `TELEGRAM_BOT_TOKEN` | `123456789:AA...` | нет |
| `telegram.bot.proxy.host` | `TELEGRAM_BOT_PROXY_HOST` | `127.0.0.1` | нет |
| `telegram.bot.proxy.port` | `TELEGRAM_BOT_PROXY_PORT` | `1301` | нет |
| `server.port` | `SERVER_PORT` | `8080` | нет |
| `spring.liquibase.change-log` | `SPRING_LIQUIBASE_CHANGE_LOG` | `classpath:db/changelog/db.changelog-master.yaml` | нет |

Токен бота никогда не хранится в репозитории: без переменной `TELEGRAM_BOT_TOKEN` приложение
запускается, но бот выключен.

### Пример запуска

Linux/macOS:

```bash
export SPRING_DATASOURCE_URL="jdbc:postgresql://localhost:5432/offers_db"
export SPRING_DATASOURCE_USERNAME="offers_user"
export SPRING_DATASOURCE_PASSWORD="offers_password"
export APPLICATION_RECOMMENDATIONS_DB_URL="jdbc:h2:file:/var/lib/offers/transaction"
export TELEGRAM_BOT_TOKEN="123456789:AA..."

java -jar target/offersToClients-0.0.1-SNAPSHOT.jar
```

Windows PowerShell:

```powershell
$env:SPRING_DATASOURCE_URL="jdbc:postgresql://localhost:5432/offers_db"
$env:SPRING_DATASOURCE_USERNAME="offers_user"
$env:SPRING_DATASOURCE_PASSWORD="offers_password"
$env:APPLICATION_RECOMMENDATIONS_DB_URL="jdbc:h2:file:C:\offers\data\transaction"
$env:TELEGRAM_BOT_TOKEN="123456789:AA..."

java -jar target\offersToClients-0.0.1-SNAPSHOT.jar
```

Фоновый запуск в Linux:

```bash
nohup java -jar target/offersToClients-0.0.1-SNAPSHOT.jar > offers.log 2>&1 &
```

## 6. Проверка после запуска

```bash
curl -s http://localhost:8080/management/info
# {"name":"offersToClients","version":"0.0.1-SNAPSHOT"}

curl -s http://localhost:8080/rule/stats
# {"stats":[]}

curl -s http://localhost:8080/recommendation/cd515076-5d8a-44be-930e-8d4fcb79f42d
# {"user_id":"cd515076-...","recommendations":[{"name":"Invest500",...}]}

curl -i -X POST http://localhost:8080/management/clear-caches
# HTTP/1.1 204
```

Swagger UI: <http://localhost:8080/swagger-ui.html>, спецификация: <http://localhost:8080/openapi.yaml>.

## 7. Типовые проблемы

| Симптом | Причина | Решение |
|---|---|---|
| `Connection refused` к PostgreSQL при старте | сервер не запущен или неверный `SPRING_DATASOURCE_URL` | запустить PostgreSQL, проверить хост/порт |
| `Schema-validation: missing table` | миграции не применились | проверить `spring.liquibase.enabled`, права пользователя БД |
| `Database may be already in use` (H2) | файл H2 открыт другим процессом, например запущенным приложением или тестами | остановить лишний процесс; не запускать тесты при работающем приложении |
| Бот не отвечает, в логах `ConnectException` | у JVM нет прямого доступа к `api.telegram.org` | задать `TELEGRAM_BOT_PROXY_HOST`/`_PORT` или системные `-Dhttps.proxyHost` |
| `409 Conflict` в логах Telegram | запущено два экземпляра с одним токеном | оставить один экземпляр |
| `401 Unauthorized` от Telegram | токен отозван или неверен | выпустить новый токен в BotFather |
| `/management/info` возвращает `unknown` | запуск без build-info (например, из IDE) | собирать через `mvnw package` или запускать собранный JAR |

## 8. Требования к окружению production

- процессу нужен доступ на запись только к каталогу логов; H2 монтируется read-only;
- PostgreSQL — отдельный сервис с резервным копированием; статистика и правила восстанавливаются из дампа;
- секреты хранятся в переменных среды или в secret manager, не в файлах репозитория;
- порт 8080 публикуется через reverse proxy с TLS;
- для наблюдения достаточно стандартных логов Spring Boot; `/management/info` используется для
  проверки версии развёрнутого артефакта.
