# Трассировка требований

Таблица связывает каждое требование с реализацией в коде и задачей в трекере. Полный список
задач с описаниями лежит в [`scripts/create-issues.ps1`](../scripts/create-issues.ps1);
после создания задач скрипт сохраняет соответствие «требование → номер задачи» в
`docs/issues/created-issues.json`.

## Как поддерживается связь

1. Требование имеет уникальный идентификатор (`US-*`, `NFR-*`, `BR-*`) — см. [Требования](Requirements).
2. На каждую группу требований заводится задача GitHub с идентификатором в заголовке.
3. Работа ведётся в отдельной ветке; PR в описание включает `Closes #<номер задачи>`, поэтому
   GitHub автоматически связывает PR, ветку и коммиты с задачей.
4. Карточка задачи перемещается по колонкам проекта `To Do → In Progress → Done`, история
   перемещений сохраняется в GitHub Projects.

## Функциональные требования

| ID | Задача | Реализация | Ветка | Статус |
|---|---|---|---|---|
| US-01 | `[US-01] Создание динамического правила` | `controller/RuleController#createRule`, `service/RuleService#createRule`, `entity/RuleEntity` | `third-task` | Done |
| US-02 | `[US-02] Просмотр списка динамических правил` | `controller/RuleController#getAllRules`, `repository/RuleRepository#findAllWithQueries` | `third-task` | Done |
| US-03 | `[US-03] Удаление динамического правила` | `controller/RuleController#deleteRule`, каскады в миграциях `001`, `002` | `third-task` | Done |
| US-04 | `[US-04] Статистика срабатываний правил` | `controller/RuleStatController`, `service/RuleStatService`, `repository/RuleStatRepository#increment` | `third-task` | Done |
| US-05 | `[US-05] Рекомендации через REST API` | `controller/RecommendationController`, `service/RecommendationService`, `service/DynamicRuleService` | `third-task` | Done |
| US-06 | `[US-06] Рекомендации в Telegram` | `telegram/TelegramBotListener`, `telegram/TelegramBotService`, `repository/UsersRepository` | `third-task` | Done |
| US-07 | `[US-07] Справка в Telegram (/start)` | `telegram/TelegramBotService#help` | `third-task` | Done |
| US-08 | `[US-08] Ответ «Пользователь не найден»` | `repository/UsersRepository#findByUsername`, `telegram/TelegramBotService#recommendation` | `third-task` | Done |
| US-09 | `[US-09] Информация о приложении` | `controller/ManagementController#getInfo`, `pom.xml` (`build-info`) | `third-task` | Done |
| US-10 | `[US-10] Сброс кэшей` | `controller/ManagementController#clearCaches`, `service/ManagementService`, `cache/RecommendationsCache#clear` | `third-task` | Done |

## Бизнес-правила

| ID | Задача | Реализация | Ветка | Статус |
|---|---|---|---|---|
| BR-01 | `[BR-01..BR-03] Статические правила рекомендаций` | `rules/Invest500RuleSet` | `third-task` | Done |
| BR-02 | `[BR-01..BR-03] Статические правила рекомендаций` | `rules/TopSavingRuleSet` | `third-task` | Done |
| BR-03 | `[BR-01..BR-03] Статические правила рекомендаций` | `rules/SimpleCreditRuleSet` | `third-task` | Done |

## Нефункциональные требования

| ID | Задача | Реализация | Ветка | Статус |
|---|---|---|---|---|
| NFR-01 | `[NFR-01, NFR-03, NFR-04] Производительность, отказоустойчивость, логирование` | `cache/RecommendationsCache`, `repository/RecommendationsRepository` | `third-task` | Done |
| NFR-02 | `[NFR-02] Атомарность статистики` | `repository/RuleStatRepository#increment`, `002-create-rule-stat.yaml` | `third-task` | Done |
| NFR-03 | `[NFR-01, NFR-03, NFR-04] Производительность, отказоустойчивость, логирование` | `telegram/TelegramBotConfiguration`, `telegram/TelegramMessageSender` | `third-task` | Done |
| NFR-04 | `[NFR-01, NFR-03, NFR-04] Производительность, отказоустойчивость, логирование` | `telegram/TelegramBotListener`, `controller/RuleController#handleIllegalArgument` | `third-task` | Done |
| NFR-05 | `[NFR-05, NFR-06] Миграции и конфигурация` | `src/main/resources/db/changelog/*`, `spring.jpa.hibernate.ddl-auto=validate` | `third-task` | Done |
| NFR-06 | `[NFR-05, NFR-06] Миграции и конфигурация` | `application.properties` (только комментарии), `docs/Deployment.md` | `third-task` | Done |
| NFR-07 | `[NFR-07, NFR-08] Документация и качество кода` | `docs/api/openapi.yaml`, `static/swagger-ui.html` | `third-task` | Done |
| NFR-08 | `[NFR-07, NFR-08] Документация и качество кода` | Javadoc в `service/`, `controller/`, `repository/`, `entity/`, `telegram/` | `third-task` | Done |

## Покрытие акторов

| Актор | Требования |
|---|---|
| Менеджер | US-01, US-02, US-03, US-04 |
| Пользователь | US-05, US-06, US-07, US-08, BR-01…BR-03 |
| Внешняя система | US-09, US-10 |

## Задачи по оформлению проекта

| Задача | Содержание | Артефакт |
|---|---|---|
| `[DOCS] Wiki: главная страница` | название, стек, описание, ссылки | [Home](Home) |
| `[DOCS] Wiki: требования и прецеденты` | user stories, NFR, диаграмма вариантов использования | [Requirements](Requirements) |
| `[DOCS] Wiki: архитектура` | диаграммы компонентов и деятельности | [Architecture](Architecture) |
| `[DOCS] Wiki: REST API` | описание эндпоинтов и схем | [REST-API](REST-API) |
| `[DOCS] Wiki: OpenAPI / Swagger` | спецификация и инструкция запуска Swagger UI | [OpenAPI](OpenAPI) |
| `[DOCS] Wiki: развёртывание` | сервисы, сборка, запуск, переменные среды | [Deployment](Deployment) |
| `[CHORE] Форматирование и Javadoc` | реформат кода, Javadoc, удаление wildcard-импортов | `src/main/java/**` |
| `[CHORE] Секреты вне репозитория` | токен бота только в переменной среды | `application.properties`, `README.md` |
