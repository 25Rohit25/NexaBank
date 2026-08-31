$ErrorActionPreference = 'Continue'

$requirements = @(
    @{ Name = 'Java'; Command = 'java'; Args = @('-version') },
    @{ Name = 'Maven'; Command = 'mvn'; Args = @('-version') },
    @{ Name = 'Docker'; Command = 'docker'; Args = @('--version') },
    @{ Name = 'Docker Compose'; Command = 'docker'; Args = @('compose', 'version') },
    @{ Name = 'Git'; Command = 'git'; Args = @('--version') }
)

$failed = $false
foreach ($requirement in $requirements) {
    Write-Host "`n[$($requirement.Name)]"
    & $requirement.Command @($requirement.Args)
    if ($LASTEXITCODE -ne 0) {
        $failed = $true
        Write-Host "Missing or misconfigured: $($requirement.Name)" -ForegroundColor Red
    }
}

$javaVersion = (& java -version 2>&1 | Select-Object -First 1) -join ''
if ($javaVersion -notmatch 'version "21') {
    $failed = $true
    Write-Host "`nNexa Bank requires JDK 21. Current runtime: $javaVersion" -ForegroundColor Red
}

if ($failed) {
    exit 1
}

Write-Host "`nAll Nexa Bank prerequisites are ready." -ForegroundColor Green

