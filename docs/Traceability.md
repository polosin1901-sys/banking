# Трассировка требований

Таблица связывает каждое требование с реализацией в коде и задачей в трекере. Полный список
задач с описаниями лежит в [`scripts/create-issues.ps1`](../scripts/create-issues.ps1);
после создания задач скрипт сохраняет соответствие «требование → номер задачи» в
`docs/issues/created-issues.json`.

Все задачи объединены pull request [#19](https://github.com/polosin1901-sys/banking/pull/19)\n(ветка inal-task → 	hird-task), в описании которого перечислены Closes #4…#18.\n\n## Как поддерживается связь

1. Требование имеет уникальный идентификатор (`US-*`, `NFR-*`, `BR-*`) — см. [Требования](Requirements).
2. На каждую группу требований заводится задача GitHub с идентификатором в заголовке.
3. Работа ведётся в отдельной ветке; PR в описание включает `Closes #<номер задачи>`, поэтому
   GitHub автоматически связывает PR, ветку и коммиты с задачей.
4. Карточка задачи перемещается по колонкам проекта `To Do → In Progress → Done`, история
   перемещений сохраняется в GitHub Projects.

## Функциональные требования

| ID | Задача | Реализация | Ветка | Статус |
|---|---|---|---|---|
| US-01 | [#4](https://github.com/polosin1901-sys/banking/issues/4) | `controller/RuleController#createRule`, `service/RuleService#createRule`, `entity/RuleEntity` | `final-task` | Done |
| US-02 | [#5](https://github.com/polosin1901-sys/banking/issues/5) | `controller/RuleController#getAllRules`, `repository/RuleRepository#findAllWithQueries` | `final-task` | Done |
| US-03 | [#6](https://github.com/polosin1901-sys/banking/issues/6) | `controller/RuleController#deleteRule`, каскады в миграциях `001`, `002` | `final-task` | Done |
| US-04 | [#7](https://github.com/polosin1901-sys/banking/issues/7) | `controller/RuleStatController`, `service/RuleStatService`, `repository/RuleStatRepository#increment` | `final-task` | Done |
| US-05 | [#8](https://github.com/polosin1901-sys/banking/issues/8) | `controller/RecommendationController`, `service/RecommendationService`, `service/DynamicRuleService` | `final-task` | Done |
| US-06 | [#9](https://github.com/polosin1901-sys/banking/issues/9) | `telegram/TelegramBotListener`, `telegram/TelegramBotService`, `repository/UsersRepository` | `final-task` | Done |
| US-07 | [#10](https://github.com/polosin1901-sys/banking/issues/10) | `telegram/TelegramBotService#help` | `final-task` | Done |
| US-08 | [#11](https://github.com/polosin1901-sys/banking/issues/11) | `repository/UsersRepository#findByUsername`, `telegram/TelegramBotService#recommendation` | `final-task` | Done |
| US-09 | [#12](https://github.com/polosin1901-sys/banking/issues/12) | `controller/ManagementController#getInfo`, `pom.xml` (`build-info`) | `final-task` | Done |
| US-10 | [#13](https://github.com/polosin1901-sys/banking/issues/13) | `controller/ManagementController#clearCaches`, `service/ManagementService`, `cache/RecommendationsCache#clear` | `final-task` | Done |

## Бизнес-правила

| ID | Задача | Реализация | Ветка | Статус |
|---|---|---|---|---|
| BR-01 | [#14](https://github.com/polosin1901-sys/banking/issues/14) | `rules/Invest500RuleSet` | `final-task` | Done |
| BR-02 | [#14](https://github.com/polosin1901-sys/banking/issues/14) | `rules/TopSavingRuleSet` | `final-task` | Done |
| BR-03 | [#14](https://github.com/polosin1901-sys/banking/issues/14) | `rules/SimpleCreditRuleSet` | `final-task` | Done |

## Нефункциональные требования

| ID | Задача | Реализация | Ветка | Статус |
|---|---|---|---|---|
| NFR-01 | [#15](https://github.com/polosin1901-sys/banking/issues/15) | `cache/RecommendationsCache`, `repository/RecommendationsRepository` | `final-task` | Done |
| NFR-02 | [#16](https://github.com/polosin1901-sys/banking/issues/16) | `repository/RuleStatRepository#increment`, `002-create-rule-stat.yaml` | `final-task` | Done |
| NFR-03 | [#15](https://github.com/polosin1901-sys/banking/issues/15) | `telegram/TelegramBotConfiguration`, `telegram/TelegramMessageSender` | `final-task` | Done |
| NFR-04 | [#15](https://github.com/polosin1901-sys/banking/issues/15) | `telegram/TelegramBotListener`, `controller/RuleController#handleIllegalArgument` | `final-task` | Done |
| NFR-05 | [#17](https://github.com/polosin1901-sys/banking/issues/17) | `src/main/resources/db/changelog/*`, `spring.jpa.hibernate.ddl-auto=validate` | `final-task` | Done |
| NFR-06 | [#17](https://github.com/polosin1901-sys/banking/issues/17) | `application.properties` (только комментарии), `docs/Deployment.md` | `final-task` | Done |
| NFR-07 | [#18](https://github.com/polosin1901-sys/banking/issues/18) | `docs/api/openapi.yaml`, `static/swagger-ui.html` | `final-task` | Done |
| NFR-08 | [#18](https://github.com/polosin1901-sys/banking/issues/18) | Javadoc в `service/`, `controller/`, `repository/`, `entity/`, `telegram/` | `final-task` | Done |

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
| `[TRACKER] Задачи и доска` | 15 задач с ID требований, доска To Do / In Progress / Done | [Tracker](Tracker) |
