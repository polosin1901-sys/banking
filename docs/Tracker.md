# Трекер задач и Wiki

## Задачи GitHub

Задачи созданы в трекере репозитория
[polosin1901-sys/banking](https://github.com/polosin1901-sys/banking/issues?q=is%3Aissue).
Всего 15 задач: 10 user stories, одно бизнес-правило-группа и 4 нефункциональных требования.

| Требование | Задача | Метки |
|---|---|---|
| US-01 | [#4](https://github.com/polosin1901-sys/banking/issues/4) | `type: requirement`, `actor: manager` |
| US-02 | [#5](https://github.com/polosin1901-sys/banking/issues/5) | `type: requirement`, `actor: manager` |
| US-03 | [#6](https://github.com/polosin1901-sys/banking/issues/6) | `type: requirement`, `actor: manager` |
| US-04 | [#7](https://github.com/polosin1901-sys/banking/issues/7) | `type: requirement`, `actor: manager` |
| US-05 | [#8](https://github.com/polosin1901-sys/banking/issues/8) | `type: requirement`, `actor: user` |
| US-06 | [#9](https://github.com/polosin1901-sys/banking/issues/9) | `type: requirement`, `actor: user` |
| US-07 | [#10](https://github.com/polosin1901-sys/banking/issues/10) | `type: requirement`, `actor: user` |
| US-08 | [#11](https://github.com/polosin1901-sys/banking/issues/11) | `type: requirement`, `actor: user` |
| US-09 | [#12](https://github.com/polosin1901-sys/banking/issues/12) | `type: requirement`, `actor: external` |
| US-10 | [#13](https://github.com/polosin1901-sys/banking/issues/13) | `type: requirement`, `actor: external` |
| BR-01…BR-03 | [#14](https://github.com/polosin1901-sys/banking/issues/14) | `type: requirement` |
| NFR-01, NFR-03, NFR-04 | [#15](https://github.com/polosin1901-sys/banking/issues/15) | `type: nfr` |
| NFR-02 | [#16](https://github.com/polosin1901-sys/banking/issues/16) | `type: nfr` |
| NFR-05, NFR-06 | [#17](https://github.com/polosin1901-sys/banking/issues/17) | `type: nfr` |
| NFR-07, NFR-08 | [#18](https://github.com/polosin1901-sys/banking/issues/18) | `type: nfr`, `type: docs` |

Тело каждой задачи содержит: формулировку требования, критерии приёмки, ссылки на классы
реализации, имя ветки и ссылку на код в ветке `final-task`.

Все задачи связаны с pull request [#19](https://github.com/polosin1901-sys/banking/pull/19)
(`final-task` → `third-task`): описание PR содержит `Closes #4…#18`, поэтому GitHub связал
задачи, ветку и коммиты, а после слияния закрыл все 15 задач.

## Статусы задач

Помимо меток типа и актора каждая задача получает метку статуса, чтобы прогресс был виден
в списке задач без доски:

| Метка | Значение |
|---|---|
| `status: to-do` | задача в очереди |
| `status: in-progress` | работа ведётся в ветке |
| `status: done` | требование реализовано и покрыто тестами |

Все 15 задач текущей итерации находятся в статусе `status: done`: реализация вошла
в ветку `final-task` и в `main`.

## Доска проекта

Доска создаётся скриптом [`scripts/create-project.ps1`](../scripts/create-project.ps1):

1. токен должен иметь scope `project`;
2. `$env:GITHUB_TOKEN = "<токен>"; .\scripts\create-project.ps1`;
3. скрипт создаёт проект `offersToClients`, добавляет 15 задач и выставляет статус `Done`.

Новый GitHub Project сразу содержит поле `Status` с колонками `Todo`, `In Progress`, `Done` —
история изменения статуса сохраняется в карточке задачи. Если доска ещё не создана, её можно
собрать вручную: вкладка **Projects → New project → Board**, затем **Add item** для каждой задачи.

## Публикация Wiki

Страницы проекта лежат в каталоге `docs/` и готовы к копированию в Wiki:

| Страница | Файл |
|---|---|
| Home | `docs/Home.md` |
| Requirements | `docs/Requirements.md` |
| Traceability | `docs/Traceability.md` |
| Architecture | `docs/Architecture.md` |
| REST-API | `docs/REST-API.md` |
| OpenAPI | `docs/OpenAPI.md` |
| Deployment | `docs/Deployment.md` |
| QA-Checklist | `docs/QA-Checklist.md` |

Публикация скриптом:

```powershell
# один раз создайте первую страницу через браузер: <репозиторий>/wiki
$env:GITHUB_TOKEN = "<токен>"
.\scripts\publish-wiki.ps1
```

Скрипт клонирует `banking.wiki.git`, копирует страницы, генерирует `_Sidebar.md` и отправляет
изменения. Без инициализированной Wiki git-репозиторий ещё не существует, поэтому первый шаг
выполняется через веб-интерфейс.

## Воспроизводимость

- задачи: `scripts/create-issues.ps1` — идемпотентен, повторный запуск не создаёт дубликаты;
- соответствие «требование → номер задачи»: [`docs/issues/created-issues.json`](issues/created-issues.json);
- трассировка: [Traceability](Traceability).
