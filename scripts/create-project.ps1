<#
.SYNOPSIS
    Creates the GitHub Project (v2) board and puts every requirement issue on it.

.DESCRIPTION
    The script creates a project named "offersToClients" for the repository owner, adds all issues
    listed in docs/issues/created-issues.json as project items and sets their Status to Done.

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

# Resolve the owner and the repository identifiers required by Projects v2.
$lookup = Invoke-GraphQl -Token $token -Query @'
query($owner: String!, $repo: String!) {
  viewer { id login }
  repository(owner: $owner, name: $repo) { id name }
}
'@ -Variables @{ owner = $Owner; repo = $Repo }

$ownerId = $lookup.viewer.id
Write-Host "Owner: $($lookup.viewer.login) ($ownerId)"

$created = Invoke-GraphQl -Token $token -Query @'
mutation($ownerId: ID!, $title: String!) {
  createProjectV2(input: { ownerId: $ownerId, title: $title }) {
    projectV2 { id number url }
  }
}
'@ -Variables @{ ownerId = $ownerId; title = $Title }

$project = $created.createProjectV2.projectV2
Write-Host "Project created: $($project.url)"

# Status field and its options come with a new project board.
$fields = Invoke-GraphQl -Token $token -Query @'
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
'@ -Variables @{ projectId = $project.id }

$statusField = $fields.node.fields.nodes | Where-Object { $_.name -eq 'Status' } | Select-Object -First 1
if (-not $statusField) {
    throw 'Status field not found in the new project.'
}

$doneOption = $statusField.options | Where-Object { $_.name -in @('Done', 'Готово') } | Select-Object -First 1
Write-Host "Status field: $($statusField.name); options: $(($statusField.options.name) -join ', ')"

foreach ($issue in $issues) {
    $issueData = Invoke-GraphQl -Token $token -Query @'
query($owner: String!, $repo: String!, $number: Int!) {
  repository(owner: $owner, name: $repo) { issue(number: $number) { id } }
}
'@ -Variables @{ owner = $Owner; repo = $Repo; number = [int]$issue.Number }

    $contentId = $issueData.repository.issue.id
    $added = Invoke-GraphQl -Token $token -Query @'
mutation($projectId: ID!, $contentId: ID!) {
  addProjectV2ItemById(input: { projectId: $projectId, contentId: $contentId }) {
    item { id }
  }
}
'@ -Variables @{ projectId = $project.id; contentId = $contentId }

    $itemId = $added.addProjectV2ItemById.item.id

    if ($doneOption) {
        Invoke-GraphQl -Token $token -Query @'
mutation($projectId: ID!, $itemId: ID!, $fieldId: ID!, $optionId: String!) {
  updateProjectV2ItemFieldValue(input: {
    projectId: $projectId, itemId: $itemId, fieldId: $fieldId,
    value: { singleSelectOptionId: $optionId }
  }) { projectV2Item { id } }
}
'@ -Variables @{
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
