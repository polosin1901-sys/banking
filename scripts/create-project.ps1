<#
.SYNOPSIS
    Creates the GitHub Project (v2) board and puts every requirement issue on it.

.DESCRIPTION
    The script creates a project named "offersToClients" for the repository owner, links it to the
    repository, adds all issues listed in docs/issues/created-issues.json as project items and sets
    their Status to Done.

    The script is idempotent: if a project with the same title already exists, it is reused instead
    of creating a duplicate.

    A new GitHub Project already contains the Status field with "Todo", "In Progress" and "Done"
    options, so no extra configuration is required.

    Required environment variable:
        GITHUB_TOKEN - token with the "project" scope (classic) or
                       "Projects: read and write" permission (fine-grained).

    If the token has no project scope, GitHub answers
    "Your token has not been granted the required scopes".

.EXAMPLE
    $env:GITHUB_TOKEN = "ghp_..."
    .\scripts\create-project.ps1

.EXAMPLE
    .\scripts\create-project.ps1 -DryRun
#>
param(
    [string]$Owner = 'polosin1901-sys',
    [string]$Repo = 'banking',
    [string]$Title = 'offersToClients',
    [string]$IssuesFile = 'docs/issues/created-issues.json',
    [switch]$DryRun
)

$ErrorActionPreference = 'Stop'

function Invoke-GraphQl {
    param(
        [string]$Token,
        [string]$Query,
        [hashtable]$Variables = @{}
    )

    $payload = @{ query = $Query; variables = $Variables } | ConvertTo-Json -Depth 6 -Compress
    $bytes = [System.Text.Encoding]::UTF8.GetBytes($payload)
    $headers = @{
        Authorization = "Bearer $Token"
        'User-Agent'  = 'offersToClients-docs'
    }

    $response = Invoke-RestMethod -Method Post -Uri 'https://api.github.com/graphql' `
        -Headers $headers -ContentType 'application/json; charset=utf-8' -Body $bytes

    if ($response.errors) {
        throw (($response.errors | ForEach-Object { $_.message }) -join '; ')
    }

    return $response.data
}

if (-not (Test-Path $IssuesFile)) {
    throw "Issue mapping not found: $IssuesFile. Run scripts/create-issues.ps1 first."
}

$issues = Get-Content $IssuesFile -Raw -Encoding UTF8 | ConvertFrom-Json
Write-Host "Loaded $($issues.Count) issues from $IssuesFile"

if ($DryRun) {
    Write-Host "Dry run: project '$Title' would be created for $Owner/$Repo with $($issues.Count) items"
    $issues | ForEach-Object { Write-Host ("  #{0}  {1}" -f $_.Number, $_.Title) }
    return
}

$token = $env:GITHUB_TOKEN
if ([string]::IsNullOrWhiteSpace($token)) {
    throw 'Set the GITHUB_TOKEN environment variable with the "project" scope.'
}

$linkQuery = @'
mutation($projectId: ID!, $repositoryId: ID!) {
  linkProjectV2ToRepository(input: { projectId: $projectId, repositoryId: $repositoryId }) {
    repository { name }
  }
}
'@

$existingProjectsQuery = @'
query($login: String!) {
  user(login: $login) {
    projectsV2(first: 50) { nodes { id title url } }
  }
}
'@

$createProjectQuery = @'
mutation($ownerId: ID!, $title: String!) {
  createProjectV2(input: { ownerId: $ownerId, title: $title }) {
    projectV2 { id number url }
  }
}
'@

$statusFieldQuery = @'
query($projectId: ID!) {
  node(id: $projectId) {
    ... on ProjectV2 {
      fields(first: 20) {
        nodes {
          ... on ProjectV2SingleSelectField { id name options { id name } }
        }
      }
    }
  }
}
'@

$issueIdQuery = @'
query($owner: String!, $repo: String!, $number: Int!) {
  repository(owner: $owner, name: $repo) { issue(number: $number) { id } }
}
'@

$addItemQuery = @'
mutation($projectId: ID!, $contentId: ID!) {
  addProjectV2ItemById(input: { projectId: $projectId, contentId: $contentId }) {
    item { id }
  }
}
'@

$setStatusQuery = @'
mutation($projectId: ID!, $itemId: ID!, $fieldId: ID!, $optionId: String!) {
  updateProjectV2ItemFieldValue(input: {
    projectId: $projectId, itemId: $itemId, fieldId: $fieldId,
    value: { singleSelectOptionId: $optionId }
  }) { projectV2Item { id } }
}
'@

$viewerQuery = @'
query($owner: String!, $repo: String!) {
  viewer { id login }
  repository(owner: $owner, name: $repo) { id name }
}
'@

# Resolve the owner and the repository identifiers required by Projects v2.
$lookup = Invoke-GraphQl -Token $token -Query $viewerQuery -Variables @{ owner = $Owner; repo = $Repo }

$ownerId = $lookup.viewer.id
$repoId = $lookup.repository.id
Write-Host "Owner: $($lookup.viewer.login) ($ownerId)"

# Reuse an existing board so a second run does not create a duplicate project.
$existing = Invoke-GraphQl -Token $token -Query $existingProjectsQuery -Variables @{ login = $Owner }
$project = $existing.user.projectsV2.nodes | Where-Object { $_.title -eq $Title } | Select-Object -First 1

if ($project) {
    Write-Host "Project exists: $($project.url)"
} else {
    $created = Invoke-GraphQl -Token $token -Query $createProjectQuery -Variables @{
        ownerId = $ownerId
        title   = $Title
    }
    $project = $created.createProjectV2.projectV2
    Write-Host "Project created: $($project.url)"
}

# Link the board to the repository so it appears on the repository Projects tab.
Invoke-GraphQl -Token $token -Query $linkQuery -Variables @{
    projectId    = $project.id
    repositoryId = $repoId
} | Out-Null
Write-Host "Linked to $Owner/$Repo"

# Status field and its options come with a new project board.
$fields = Invoke-GraphQl -Token $token -Query $statusFieldQuery -Variables @{ projectId = $project.id }

$statusField = $fields.node.fields.nodes | Where-Object { $_.name -eq 'Status' } | Select-Object -First 1
if (-not $statusField) {
    throw 'Status field not found in the new project.'
}

$doneOption = $statusField.options | Where-Object { $_.name -in @('Done', 'Готово') } | Select-Object -First 1
Write-Host "Status field: $($statusField.name); options: $(($statusField.options.name) -join ', ')"

foreach ($issue in $issues) {
    $issueData = Invoke-GraphQl -Token $token -Query $issueIdQuery -Variables @{
        owner  = $Owner
        repo   = $Repo
        number = [int]$issue.Number
    }

    $contentId = $issueData.repository.issue.id
    $added = Invoke-GraphQl -Token $token -Query $addItemQuery -Variables @{
        projectId = $project.id
        contentId = $contentId
    }

    $itemId = $added.addProjectV2ItemById.item.id

    if ($doneOption) {
        Invoke-GraphQl -Token $token -Query $setStatusQuery -Variables @{
            projectId = $project.id
            itemId    = $itemId
            fieldId   = $statusField.id
            optionId  = $doneOption.id
        } | Out-Null
    }

    Write-Host "  added #$($issue.Number) $($issue.Title)"
}

Write-Host ''
Write-Host "Project board: $($project.url)"
Write-Host 'Columns: Todo / In Progress / Done (default Status field of a new GitHub Project).'
