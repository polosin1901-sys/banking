<#
.SYNOPSIS
    Publishes the pages from docs/ to the GitHub Wiki of the repository.

.DESCRIPTION
    Clones <owner>/<repo>.wiki.git into a temporary directory, copies the wiki-ready pages,
    generates _Sidebar.md and pushes the result.

    The wiki must be initialised once through the web interface: open the "Wiki" tab of the
    repository and create the first page. GitHub only creates the wiki git repository after that.

    Authentication uses the same token as the issue script. Pass it through GITHUB_TOKEN or put the
    token into the HTTPS remote explicitly; git credential manager works as well.

.EXAMPLE
    $env:GITHUB_TOKEN = "ghp_..."
    .\scripts\publish-wiki.ps1

.EXAMPLE
    .\scripts\publish-wiki.ps1 -DryRun
#>
param(
    [string]$Owner = 'polosin1901-sys',
    [string]$Repo = 'banking',
    [string]$Branch = 'final-task',
    [switch]$DryRun
)

$ErrorActionPreference = 'Stop'

$pages = [ordered]@{
    'Home.md'          = 'Home'
    'Requirements.md'  = 'Requirements'
    'Traceability.md'  = 'Traceability'
    'Architecture.md'  = 'Architecture'
    'REST-API.md'      = 'REST-API'
    'OpenAPI.md'       = 'OpenAPI'
    'Deployment.md'    = 'Deployment'
    'QA-Checklist.md'  = 'QA-Checklist'
    'Tracker.md'       = 'Tracker'
}

$repoUrl = "https://github.com/$Owner/$Repo"
$wikiUrl = "$repoUrl.wiki.git"
$codeUrl = "$repoUrl/blob/$Branch"

if ($DryRun) {
    Write-Host "Dry run: the following pages would be published to $wikiUrl"
    $pages.GetEnumerator() | ForEach-Object {
        Write-Host ("  docs/{0} -> {1}" -f $_.Key, $_.Value)
    }
    return
}

$workDir = Join-Path ([System.IO.Path]::GetTempPath()) ("offers-wiki-" + [guid]::NewGuid().ToString('N'))

try {
    git clone --quiet $wikiUrl $workDir
} catch {
    throw "Cannot clone $wikiUrl. Open $repoUrl/wiki in a browser and create the first page, then run the script again."
}

try {
    foreach ($page in $pages.GetEnumerator()) {
        $source = Join-Path 'docs' $page.Key
        if (-not (Test-Path $source)) {
            Write-Host "skipped (not found): $source"
            continue
        }

        $content = Get-Content $source -Raw -Encoding UTF8

        # Wiki pages cannot link to files of the repository with relative paths.
        $content = $content.Replace('](api/openapi.yaml)', "]($codeUrl/docs/api/openapi.yaml)")
        $content = $content.Replace('](../scripts/create-issues.ps1)', "]($codeUrl/scripts/create-issues.ps1)")

        $target = Join-Path $workDir $page.Value
        [System.IO.File]::WriteAllText($target, $content, (New-Object System.Text.UTF8Encoding($false)))
        Write-Host "published: $($page.Value)"
    }

    $sidebar = ($pages.Values | ForEach-Object { "* [$_]($_)" }) -join "`n"
    [System.IO.File]::WriteAllText(
        (Join-Path $workDir '_Sidebar.md'),
        "_Sidebar_`n`n$sidebar`n",
        (New-Object System.Text.UTF8Encoding($false))
    )

    git -C $workDir add --all
    git -C $workDir -c user.name='offersToClients' -c user.email='offers@example.com' commit --quiet -m 'docs: publish project wiki'
    git -C $workDir push --quiet origin HEAD
    Write-Host "Wiki updated: $repoUrl/wiki"
} finally {
    Remove-Item -Recurse -Force $workDir -ErrorAction SilentlyContinue
}
