# REST API

Полная машиночитаемая спецификация: [`openapi.yaml`](api/openapi.yaml) (OpenAPI 3.0.3).
После запуска приложения доступны:

- Swagger UI — <http://localhost:8080/swagger-ui.html>
- спецификация — <http://localhost:8080/openapi.yaml>

## Сводная таблица эндпоинтов

| Метод | Путь | Актор | Успех | Ошибки |
|---|---|---|---|---|
| GET | `/recommendation/{user_id}` | Пользователь | 200 | 400 |
| POST | `/rule` | Менеджер | 200 | 400 |
| GET | `/rule` | Менеджер | 200 | — |
| DELETE | `/rule/{id}` | Менеджер | 204 | 404 |
| GET | `/rule/stats` | Менеджер | 200 | — |
| GET | `/management/info` | Внешняя система | 200 | — |
| POST | `/management/clear-caches` | Внешняя система | 204 | — |

## GET /recommendation/{user_id}

**Ответ 200**

```json
{
  "user_id": "cd515076-5d8a-44be-930e-8d4fcb79f42d",
  "recommendations": [
    {
      "name": "Invest500",
      "id": "147f6a0f-3b91-413b-ab99-87f081d60d5a",
      "text": "Откройте Invest500 и начните инвестировать с 1000 рублей."
    }
  ]
}
```

Если предложений нет, `recommendations` — пустой массив.

```bash
curl -s http://localhost:8080/recommendation/cd515076-5d8a-44be-930e-8d4fcb79f42d
```

## POST /rule

**Запрос**

```json
{
  "product_name": "Invest 500",
  "product_id": "147f6a0f-3b91-413b-ab99-87f081d60d5a",
  "product_text": "Откройте брокерский счёт и получите бонус.",
  "rule": [
    { "query": "USER_OF", "arguments": ["DEBIT"], "negate": false },
    { "query": "TRANSACTION_SUM_COMPARE", "arguments": ["SAVING", "DEPOSIT", ">", "1000"], "negate": false }
  ]
}
```

**Ответ 200**

```json
{
  "id": 3,
  "product_name": "Invest 500",
  "product_id": "147f6a0f-3b91-413b-ab99-87f081d60d5a",
  "product_text": "Откройте брокерский счёт и получите бонус.",
  "rule": [
    { "query": "USER_OF", "arguments": ["DEBIT"], "negate": false },
    { "query": "TRANSACTION_SUM_COMPARE", "arguments": ["SAVING", "DEPOSIT", ">", "1000"], "negate": false }
  ]
}
```

**Ответ 400**

```json
{ "error": "Query TRANSACTION_SUM_COMPARE expects 4 argument(s), but got 3" }
```

```bash
curl -s -X POST http://localhost:8080/rule \
  -H "Content-Type: application/json" \
  -d '{"product_name":"Invest 500","product_id":"147f6a0f-3b91-413b-ab99-87f081d60d5a","product_text":"Откройте брокерский счёт.","rule":[{"query":"USER_OF","arguments":["DEBIT"],"negate":false}]}'
```

## GET /rule

**Ответ 200**

```json
{
  "data": [
    {
      "id": 1,
      "product_name": "Invest 500",
      "product_id": "147f6a0f-3b91-413b-ab99-87f081d60d5a",
      "product_text": "Откройте брокерский счёт.",
      "rule": [{ "query": "USER_OF", "arguments": ["DEBIT"], "negate": false }]
    }
  ]
}
```

## DELETE /rule/{id}

- `204 No Content` — правило удалено вместе с запросами и статистикой;
- `404 Not Found` — правила с таким `id` нет.

```bash
curl -i -X DELETE http://localhost:8080/rule/3
```

## GET /rule/stats

**Ответ 200**

```json
{
  "stats": [
    { "rule_id": 1, "count": 5 },
    { "rule_id": 2, "count": 0 }
  ]
}
```

Правила без срабатываний обязательно присутствуют с `count = 0`.

## GET /management/info

**Ответ 200**

```json
{ "name": "offersToClients", "version": "0.0.1-SNAPSHOT" }
```

`name` — `artifactId`, `version` — версия из `pom.xml`. Если build-info недоступен (запуск из IDE),
возвращается `version: "unknown"`.

## POST /management/clear-caches

Запрос без тела, ответ `204 No Content`.

```bash
curl -i -X POST http://localhost:8080/management/clear-caches
```

## Аргументы запросов динамических правил

| `query` | Аргументы | Пример | Смысл |
|---|---|---|---|
| `USER_OF` | `[тип продукта]` | `["DEBIT"]` | у клиента есть хотя бы одна транзакция по продукту |
| `ACTIVE_USER_OF` | `[тип продукта]` | `["DEBIT"]` | у клиента не менее 5 транзакций по продукту |
| `TRANSACTION_SUM_COMPARE` | `[тип продукта, тип транзакции, оператор, константа]` | `["DEBIT", "WITHDRAW", ">", "100000"]` | сумма операций сравнивается с константой |
| `TRANSACTION_SUM_COMPARE_DEPOSIT_WITHDRAW` | `[тип продукта, оператор]` | `["DEBIT", ">"]` | пополнения сравниваются со снятиями |

Допустимые значения: тип продукта — `DEBIT`, `CREDIT`, `SAVING`, `INVEST`; тип транзакции —
`DEPOSIT`, `WITHDRAW`; оператор — `>`, `<`, `=`, `>=`, `<=`. Флаг `negate: true` инвертирует
результат запроса.

## Коды ошибок

| Код | Когда возникает | Тело |
|---|---|---|
| 400 | некорректный JSON, неизвестный `query`, неверное число аргументов, `user_id` не UUID | `{"error": "..."}` |
| 404 | удаление несуществующего правила | пустое |
| 500 | внутренняя ошибка (например, недоступна PostgreSQL) | стандартный ответ Spring Boot |
