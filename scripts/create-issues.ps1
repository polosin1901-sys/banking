<#
.SYNOPSIS
    Creates GitHub issues with requirement identifiers for the offersToClients project.

.DESCRIPTION
    Creates labels (if missing) and one issue per requirement group. Every issue body contains the
    requirement identifiers, acceptance criteria and links to the implementation.

    Required environment variable:
        GITHUB_TOKEN - personal access token with the "repo" scope (classic) or
                       "Issues: read and write" permission (fine-grained).

.EXAMPLE
    $env:GITHUB_TOKEN = "ghp_..."
    .\scripts\create-issues.ps1

.EXAMPLE
    .\scripts\create-issues.ps1 -DryRun
#>
param(
    [string]$Owner = 'polosin1901-sys',
    [string]$Repo = 'banking',
    [string]$Branch = 'third-task',
    [switch]$DryRun
)

$ErrorActionPreference = 'Stop'

$repoUrl = "https://github.com/$Owner/$Repo"
$codeUrl = "$repoUrl/blob/$Branch"

$labels = @(
    @{ name = 'type: requirement'; color = '1d76db'; description = 'Функциональное требование (user story)' },
    @{ name = 'type: nfr'; color = '5319e7'; description = 'Нефункциональное требование' },
    @{ name = 'type: docs'; color = '0e8a16'; description = 'Документация и оформление' },
    @{ name = 'actor: manager'; color = 'fbca04'; description = 'Менеджер' },
    @{ name = 'actor: user'; color = 'c2e0c6'; description = 'Пользователь' },
    @{ name = 'actor: external'; color = 'bfd4f2'; description = 'Внешняя система' }
)

$issues = @(
    @{
        requirements = 'US-01'
        labels       = @('type: requirement', 'actor: manager')
        title        = '[US-01] Создание динамического правила'
        body         = @'
**Как** менеджер, **я хочу** создавать динамическое правило рекомендации, **чтобы** предлагать клиентам новые продукты без изменения кода.

### Критерии приёмки
- [x] `POST /rule` принимает `product_name`, `product_id`, `product_text`, непустой `rule`
- [x] проверяются тип запроса, количество и формат аргументов
- [x] успех — `200` и сохранённое правило с `id`
- [x] некорректные данные — `400` и `{"error": "..."}`
- [x] правило сохраняется в PostgreSQL (`recommendation_rule`, `rule_query`, `rule_query_argument`)

### Реализация
- `controller/RuleController#createRule`
- `service/RuleService#createRule` (валидация)
- `entity/RuleEntity`, `entity/QueryEntity`

### Ссылки
- Ветка: `third-task`
- Код: [RuleController.java](CODE_URL/src/main/java/com/bankingApp/offersToClients/controller/RuleController.java)
- Merge request: заполняется при создании PR
'@
    },
    @{
        requirements = 'US-02'
        labels       = @('type: requirement', 'actor: manager')
        title        = '[US-02] Просмотр списка динамических правил'
        body         = @'
**Как** менеджер, **я хочу** получать список всех динамических правил, **чтобы** понимать текущие настройки.

### Критерии приёмки
- [x] `GET /rule` возвращает `{"data": [...]}`
- [x] у правила есть `id`, `product_name`, `product_id`, `product_text`, `rule`
- [x] порядок запросов внутри правила сохраняется

### Реализация
- `controller/RuleController#getAllRules`
- `service/RuleService#getAllRules`
- `repository/RuleRepository#findAllWithQueries`

### Ссылки
- Код: [RuleService.java](CODE_URL/src/main/java/com/bankingApp/offersToClients/service/RuleService.java)
'@
    },
    @{
        requirements = 'US-03'
        labels       = @('type: requirement', 'actor: manager')
        title        = '[US-03] Удаление динамического правила'
        body         = @'
**Как** менеджер, **я хочу** удалять правило, **чтобы** прекратить показ неактуального продукта.

### Критерии приёмки
- [x] `DELETE /rule/{id}` → `204` при успехе, `404` если правила нет
- [x] каскадно удаляются запросы и статистика
- [x] удалённое правило исчезает из `GET /rule` и `GET /rule/stats`

### Реализация
- `controller/RuleController#deleteRule`
- `service/RuleService#deleteRule`
- каскад в миграциях `001-create-rule-tables.yaml`, `002-create-rule-stat.yaml`

### Ссылки
- Код: [001-create-rule-tables.yaml](CODE_URL/src/main/resources/db/changelog/001-create-rule-tables.yaml)
'@
    },
    @{
        requirements = 'US-04'
        labels       = @('type: requirement', 'actor: manager')
        title        = '[US-04] Статистика срабатываний правил'
        body         = @'
**Как** менеджер, **я хочу** видеть количество срабатываний каждого правила, **чтобы** оценивать эффективность предложений.

### Критерии приёмки
- [x] `GET /rule/stats` → `{"stats":[{"rule_id":1,"count":2}]}`
- [x] присутствуют все правила, включая `count = 0`
- [x] счётчик растёт на 1 за срабатывание
- [x] инкремент атомарный (upsert)
- [x] статистика удаляется вместе с правилом

### Реализация
- `controller/RuleStatController`
- `service/RuleStatService#getStats`, `#registerHit`
- `repository/RuleStatRepository#increment`
- `entity/RuleStatEntity`

### Ссылки
- Код: [RuleStatService.java](CODE_URL/src/main/java/com/bankingApp/offersToClients/service/RuleStatService.java)
'@
    },
    @{
        requirements = 'US-05'
        labels       = @('type: requirement', 'actor: user')
        title        = '[US-05] Рекомендации через REST API'
        body         = @'
**Как** пользователь, **я хочу** получать рекомендации по `user_id`, **чтобы** видеть персональные предложения.

### Критерии приёмки
- [x] `GET /recommendation/{user_id}` → `200` и `{"user_id": "...", "recommendations": [...]}`
- [x] учитываются статические (BR-01…BR-03) и динамические правила
- [x] дубли по `id` удаляются
- [x] пустой результат — `[]`, а не `null`

### Реализация
- `controller/RecommendationController`
- `service/RecommendationService`
- `service/DynamicRuleService`
- `rules/Invest500RuleSet`, `TopSavingRuleSet`, `SimpleCreditRuleSet`

### Ссылки
- Код: [RecommendationService.java](CODE_URL/src/main/java/com/bankingApp/offersToClients/service/RecommendationService.java)
'@
    },
    @{
        requirements = 'US-06'
        labels       = @('type: requirement', 'actor: user')
        title        = '[US-06] Рекомендации в Telegram'
        body         = @'
**Как** пользователь, **я хочу** получать рекомендации командой `/recommend <username>`, **чтобы** не открывать приложение банка.

### Критерии приёмки
- [x] приветствие `Здравствуйте, <Имя Фамилия>!`
- [x] секция `Новые продукты для вас:` и маркеры `•` у каждого продукта
- [x] username ищется с `@` и без него
- [x] используются те же правила, что и в REST API
- [x] срабатывания через бота учитываются в статистике

### Реализация
- `telegram/TelegramBotListener`, `telegram/TelegramBotService`
- `repository/UsersRepository#findByUsername`
- `telegram/TelegramBotConfiguration` (условный бин при наличии токена)

### Ссылки
- Код: [TelegramBotService.java](CODE_URL/src/main/java/com/bankingApp/offersToClients/telegram/service/TelegramBotService.java)
'@
    },
    @{
        requirements = 'US-07'
        labels       = @('type: requirement', 'actor: user')
        title        = '[US-07] Справка в Telegram (/start)'
        body         = @'
**Как** пользователь, **я хочу** получать справку по `/start`, **чтобы** понять, как пользоваться ботом.

### Критерии приёмки
- [x] на `/start` и неизвестную команду бот присылает приветствие и список команд
- [x] указан синтаксис `/recommend <username>` и пример

### Реализация
- `telegram/TelegramBotService#help`
- `telegram/TelegramBotListener#processUpdate`

### Ссылки
- Код: [TelegramBotListener.java](CODE_URL/src/main/java/com/bankingApp/offersToClients/telegram/TelegramBotListener.java)
'@
    },
    @{
        requirements = 'US-08'
        labels       = @('type: requirement', 'actor: user')
        title        = '[US-08] Ответ «Пользователь не найден»'
        body         = @'
**Как** пользователь, **я хочу** получать понятный ответ при неверном логине, **чтобы** не думать, что бот сломался.

### Критерии приёмки
- [x] ноль найденных клиентов → ровно `Пользователь не найден`
- [x] больше одного клиента → тот же ответ
- [x] пробелы вокруг логина обрезаются

### Реализация
- `repository/UsersRepository#findByUsername` (`LIMIT 2`, `size() == 1`)
- `telegram/TelegramBotService#recommendation`

### Ссылки
- Код: [UsersRepository.java](CODE_URL/src/main/java/com/bankingApp/offersToClients/repository/UsersRepository.java)
'@
    },
    @{
        requirements = 'US-09'
        labels       = @('type: requirement', 'actor: external')
        title        = '[US-09] Информация о приложении'
        body         = @'
**Как** внешняя система, **я хочу** получать название и версию приложения, **чтобы** вести инвентаризацию.

### Критерии приёмки
- [x] `GET /management/info` → `{"name":"...","version":"..."}`
- [x] значения из `pom.xml` через `build-info`
- [x] без build-info — `version: "unknown"`

### Реализация
- `controller/ManagementController#getInfo`
- `pom.xml`: `spring-boot-maven-plugin` → `build-info`

### Ссылки
- Код: [ManagementController.java](CODE_URL/src/main/java/com/bankingApp/offersToClients/controller/ManagementController.java)
'@
    },
    @{
        requirements = 'US-10'
        labels       = @('type: requirement', 'actor: external')
        title        = '[US-10] Сброс кэшей'
        body         = @'
**Как** внешняя система, **я хочу** сбрасывать кэши, **чтобы** пользователи сразу видели актуальные предложения.

### Критерии приёмки
- [x] `POST /management/clear-caches` без тела → `204`
- [x] после вызова следующий запрос читает данные из H2
- [x] содержимое ответа API не меняется

### Реализация
- `controller/ManagementController#clearCaches`
- `service/ManagementService#clearCaches`
- `cache/RecommendationsCache#clear`

### Ссылки
- Код: [RecommendationsCache.java](CODE_URL/src/main/java/com/bankingApp/offersToClients/cache/RecommendationsCache.java)
'@
    },
    @{
        requirements = 'BR-01, BR-02, BR-03'
        labels       = @('type: requirement', 'actor: user')
        title        = '[BR-01..BR-03] Статические правила рекомендаций'
        body         = @'
Статические правила первого ТЗ должны поддерживаться в коде или как динамические правила.

### Критерии приёмки
- [x] Invest500: есть дебет, нет инвестиций, пополнения накоплений > 1000
- [x] Top Saving: есть дебет, пополнения ≥ 50 000, депозит > снятий
- [x] Простой кредит: нет кредита, снятия > 100 000, депозит > снятий
- [x] правила реализованы в коде и участвуют в выдаче рекомендаций

### Реализация
- `rules/Invest500RuleSet`
- `rules/TopSavingRuleSet`
- `rules/SimpleCreditRuleSet`

### Ссылки
- Код: [rules](CODE_URL/src/main/java/com/bankingApp/offersToClients/rules)
'@
    },
    @{
        requirements = 'NFR-01, NFR-03, NFR-04'
        labels       = @('type: nfr')
        title        = '[NFR-01, NFR-03, NFR-04] Производительность, отказоустойчивость, логирование'
        body         = @'
### Критерии приёмки
- [x] агрегаты H2 кэшируются Caffeine (`maximumSize = 10 000` на кэш)
- [x] повторный запрос рекомендаций не обращается к H2
- [x] приложение стартует без токена Telegram
- [x] ошибки отправки и опроса Telegram логируются и не роняют приложение
- [x] внутренние исключения не попадают в тело ответа API

### Реализация
- `cache/RecommendationsCache`
- `telegram/TelegramBotConfiguration` (`@ConditionalOnProperty`)
- `telegram/TelegramMessageSender`, `telegram/TelegramBotListener` (SLF4J)

### Ссылки
- Код: [RecommendationsCache.java](CODE_URL/src/main/java/com/bankingApp/offersToClients/cache/RecommendationsCache.java)
'@
    },
    @{
        requirements = 'NFR-02'
        labels       = @('type: nfr')
        title        = '[NFR-02] Атомарность статистики'
        body         = @'
### Критерии приёмки
- [x] инкремент выполняется одним оператором `INSERT ... ON CONFLICT ... DO UPDATE`
- [x] `rule_id` уникален
- [x] параллельные запросы не теряют срабатывания

### Реализация
- `repository/RuleStatRepository#increment`
- миграция `002-create-rule-stat.yaml` (`UNIQUE (rule_id)`)

### Ссылки
- Код: [RuleStatRepository.java](CODE_URL/src/main/java/com/bankingApp/offersToClients/repository/RuleStatRepository.java)
'@
    },
    @{
        requirements = 'NFR-05, NFR-06'
        labels       = @('type: nfr')
        title        = '[NFR-05, NFR-06] Миграции и конфигурация'
        body         = @'
### Критерии приёмки
- [x] схема PostgreSQL управляется Liquibase
- [x] Hibernate работает в режиме `ddl-auto=validate`
- [x] секреты передаются только через переменные среды
- [x] токен бота отсутствует в репозитории

### Реализация
- `src/main/resources/db/changelog/*`
- `src/main/resources/application.properties`
- `docs/Deployment.md`

### Ссылки
- Код: [db.changelog-master.yaml](CODE_URL/src/main/resources/db/changelog/db.changelog-master.yaml)
'@
    },
    @{
        requirements = 'NFR-07, NFR-08'
        labels       = @('type: nfr', 'type: docs')
        title        = '[NFR-07, NFR-08] Документация и качество кода'
        body         = @'
### Критерии приёмки
- [x] OpenAPI 3.0 покрывает все эндпоинты
- [x] публичные классы и методы имеют Javadoc
- [x] код отформатирован, за-комментированного кода нет
- [x] README содержит описание и ссылки на документацию

### Реализация
- `docs/api/openapi.yaml`
- `src/main/resources/static/swagger-ui.html`
- `README.md`, `docs/`

### Ссылки
- Код: [README.md](CODE_URL/README.md)
'@
    }
)

function Resolve-Body {
    param([string]$Text, [string]$CodeBase)
    return $Text.Replace('CODE_URL', $CodeBase)
}

if ($DryRun) {
    Write-Host "Dry run: $($issues.Count) issues would be created in $repoUrl"
    $issues | ForEach-Object { Write-Host ("  {0}  [{1}]" -f $_.title, $_.requirements) }
    return
}

$token = $env:GITHUB_TOKEN
if ([string]::IsNullOrWhiteSpace($token)) {
    throw 'Set the GITHUB_TOKEN environment variable with repository issue permissions.'
}

$headers = @{
    Authorization          = "Bearer $token"
    Accept                 = 'application/vnd.github+json'
    'X-GitHub-Api-Version' = '2022-11-28'
    'User-Agent'           = 'offersToClients-docs'
}
$apiUrl = "https://api.github.com/repos/$Owner/$Repo"

function Invoke-GitHub {
    param([string]$Method, [string]$Uri, [hashtable]$Body)

    $json = $Body | ConvertTo-Json -Depth 6
    $bytes = [System.Text.Encoding]::UTF8.GetBytes($json)
    return Invoke-RestMethod -Method $Method -Uri $Uri -Headers $headers -Body $bytes -ContentType 'application/json; charset=utf-8'
}

foreach ($label in $labels) {
    try {
        Invoke-GitHub -Method Post -Uri "$apiUrl/labels" -Body @{
            name        = $label.name
            color       = $label.color
            description = $label.description
        } | Out-Null
        Write-Host "label created: $($label.name)"
    } catch {
        Write-Host "label exists or skipped: $($label.name)"
    }
}

$created = foreach ($issue in $issues) {
    $payload = @{
        title  = $issue.title
        body   = (Resolve-Body -Text $issue.body -CodeBase $codeUrl).Trim()
        labels = $issue.labels
    }

    $result = Invoke-GitHub -Method Post -Uri "$apiUrl/issues" -Body $payload
    [pscustomobject]@{
        Requirement = $issue.requirements
        Number      = $result.number
        Title       = $result.title
        Url         = $result.html_url
    }
}

$created | Format-Table -AutoSize
$created | ConvertTo-Json -Depth 3 | Set-Content -Path 'docs/issues/created-issues.json' -Encoding UTF8
Write-Host 'Mapping saved to docs/issues/created-issues.json'
