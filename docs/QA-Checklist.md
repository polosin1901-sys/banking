# Чек-лист проверки проекта

Сквозная проверка соответствия трём техническим заданиям и финальным требованиям.
Выполняется на локальной машине после `mvnw clean package`.

## 0. Подготовка окружения

```powershell
# PostgreSQL запущен, база offers_db и пользователь offers_user созданы
$env:JAVA_HOME='C:\Users\<user>\.jdks\corretto-17.0.19'
$env:TELEGRAM_BOT_TOKEN="<токен>"          # необязательно
$env:TELEGRAM_BOT_PROXY_HOST="127.0.0.1"   # только если нужен прокси
$env:TELEGRAM_BOT_PROXY_PORT="1301"
```

```powershell
.\mvnw.cmd clean package
java -jar target\offersToClients-0.0.1-SNAPSHOT.jar
```

Ожидаемо: `BUILD SUCCESS`, в логах `Started OffersToClientsApplication`, при заданном токене —
`Telegram bot long polling started`.

## 1. Служебные эндпоинты

| Проверка | Команда | Ожидаемый результат |
|---|---|---|
| Информация о сборке | `curl.exe -s http://localhost:8080/management/info` | `{"name":"offersToClients","version":"0.0.1-SNAPSHOT"}` |
| Сброс кэшей | `curl.exe -i -X POST http://localhost:8080/management/clear-caches` | `HTTP/1.1 204`, тело пустое |
| Swagger UI | открыть <http://localhost:8080/swagger-ui.html> | страница со списком эндпоинтов |
| OpenAPI | `curl.exe -s http://localhost:8080/openapi.yaml` | YAML-спецификация |

## 2. Динамические правила

Создание:

```powershell
curl.exe -s -X POST http://localhost:8080/rule `
  -H "Content-Type: application/json" `
  -d '{\"product_name\":\"Stat test\",\"product_id\":\"33333333-3333-3333-3333-333333333333\",\"product_text\":\"Stats work\",\"rule\":[{\"query\":\"USER_OF\",\"arguments\":[\"DEBIT\"],\"negate\":false}]}'
```

| Проверка | Команда | Ожидаемый результат |
|---|---|---|
| Валидация: мало аргументов | POST с `TRANSACTION_SUM_COMPARE` и 3 аргументами | `400`, `{"error":"Query TRANSACTION_SUM_COMPARE expects 4 argument(s), but got 3"}` |
| Валидация: неизвестный тип | POST с `query: "UNKNOWN"` | `400`, `{"error":"Unknown query type: UNKNOWN"}` |
| Список правил | `curl.exe -s http://localhost:8080/rule` | `{"data":[...]}`, запросы в исходном порядке |
| Удаление существующего | `curl.exe -i -X DELETE http://localhost:8080/rule/2` | `204` |
| Удаление несуществующего | `curl.exe -i -X DELETE http://localhost:8080/rule/999` | `404` |

## 3. Статистика

| Проверка | Команда | Ожидаемый результат |
|---|---|---|
| Пустая статистика | `curl.exe -s http://localhost:8080/rule/stats` при отсутствии правил | `{"stats":[]}` |
| Правило без срабатываний | создать правило с `USER_OF INVEST`, не вызывать рекомендации | `{"stats":[{"rule_id":N,"count":0}]}` |
| Рост счётчика | дважды `curl.exe -s http://localhost:8080/recommendation/cd515076-5d8a-44be-930e-8d4fcb79f42d` | `count = 2` |
| Каскад | удалить правило и запросить статистику | правило отсутствует в ответе |

## 4. Рекомендации (статические правила)

| Клиент | `user_id` | Ожидаемый продукт |
|---|---|---|
| Rolf Bogisich (`quintin.deckow`) | `cd515076-5d8a-44be-930e-8d4fcb79f42d` | Invest500 |
| Vincent Hackett (`celeste.schaefer`) | `d4a4d619-9a0c-4fc5-b0cb-76c49409546b` | Top Saving |
| Minna Sawayn (`ramon.cassin`) | `1f9b149c-6577-448a-bc94-16bea229b71a` | Простой кредит |

```powershell
curl.exe -s http://localhost:8080/recommendation/cd515076-5d8a-44be-930e-8d4fcb79f42d
```

Ожидаемо: `200` и список, содержащий продукт из таблицы. Динамические продукты добавляются в конец,
дубли по `id` отсутствуют.

## 5. Кэширование

1. Вызвать рекомендации дважды для одного клиента — второй вызов не должен выполнять SQL к H2
   (проверяется включением `logging.level.org.springframework.jdbc.core.JdbcTemplate=DEBUG`).
2. Выполнить `POST /management/clear-caches`.
3. Снова вызвать рекомендации — SQL к H2 появляется снова, содержимое ответа не меняется.

## 6. Telegram-бот

| Сообщение | Ожидаемый ответ |
|---|---|
| `/start` | приветствие, список команд, пример `/recommend quintin.deckow` |
| `/recommend quintin.deckow` | `Здравствуйте, Rolf Bogisich!` + `Новые продукты для вас:` + `• Invest500` |
| `/recommend celeste.schaefer` | `Здравствуйте, Vincent Hackett!` + `• Top Saving` |
| `/recommend ramon.cassin` | `Здравствуйте, Minna Sawayn!` + `• Простой кредит` |
| `/recommend @quintin.deckow` | то же, что без `@` |
| `/recommend нет.такого` | ровно `Пользователь не найден` |
| `/recommend` без аргумента | `Пользователь не найден` |
| любая неизвестная команда | приветствие и справка |

Дополнительно:

- срабатывания через бота увеличивают `hit_count` в `/rule/stats`;
- при отсутствии `TELEGRAM_BOT_TOKEN` приложение стартует, REST работает, бот не опрашивает Telegram
  (в логах `Telegram bot is not configured, long polling is disabled`).

## 7. Сценарий «несколько пользователей»

В H2 нет дублей `username`, поэтому сценарий проверяется логикой `LIMIT 2` + `size() == 1`.
При необходимости можно временно добавить дубль в копию файла H2 и убедиться, что бот отвечает
`Пользователь не найден`.

## 8. Регрессия после очистки кода

- [ ] `.\mvnw.cmd test` — все тесты зелёные
- [ ] `.\mvnw.cmd javadoc:javadoc` — без ошибок, документация в `target/reports/apidocs`
- [ ] в `src/main/java` нет wildcard-импортов и за-комментированного кода
- [ ] `git grep "telegram.bot.token="` не находит реальный токен
- [ ] README и страницы документации содержат рабочие ссылки
