[CmdletBinding()]
param(
    [string]$GatewayUrl = "http://localhost:8080",
    [switch]$IncludeAgent,
    [ValidateRange(10, 300)]
    [int]$StartupTimeoutSeconds = 120,
    [ValidateRange(5, 120)]
    [int]$ProjectionTimeoutSeconds = 30
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

function Invoke-NexaApi {
    param(
        [Parameter(Mandatory)] [ValidateSet("GET", "POST")] [string]$Method,
        [Parameter(Mandatory)] [string]$Path,
        [hashtable]$Headers = @{},
        [object]$Body
    )

    $request = @{
        Method = $Method
        Uri = "$($GatewayUrl.TrimEnd('/'))$Path"
        Headers = $Headers
        ContentType = "application/json"
    }

    if ($null -ne $Body) {
        $request.Body = $Body | ConvertTo-Json -Depth 6 -Compress
    }

    Invoke-RestMethod @request
}

function Get-JwtSubject {
    param([Parameter(Mandatory)] [string]$Token)

    $parts = $Token.Split('.')
    if ($parts.Count -ne 3) {
        throw "Registration returned an invalid JWT."
    }

    $payload = $parts[1].Replace('-', '+').Replace('_', '/')
    switch ($payload.Length % 4) {
        2 { $payload += "==" }
        3 { $payload += "=" }
    }

    $claims = [Text.Encoding]::UTF8.GetString([Convert]::FromBase64String($payload)) | ConvertFrom-Json
    if ([string]::IsNullOrWhiteSpace($claims.sub)) {
        throw "Registration JWT does not contain a subject claim."
    }

    $claims.sub
}

function Assert-Equal {
    param(
        [Parameter(Mandatory)] $Expected,
        [Parameter(Mandatory)] $Actual,
        [Parameter(Mandatory)] [string]$Message
    )

    if ($Expected -ne $Actual) {
        throw "$Message Expected '$Expected', received '$Actual'."
    }
}

Write-Host "Waiting for Nexa Bank gateway at $GatewayUrl ..."
$deadline = (Get-Date).AddSeconds($StartupTimeoutSeconds)
do {
    try {
        $health = Invoke-RestMethod -Method Get -Uri "$($GatewayUrl.TrimEnd('/'))/actuator/health" -TimeoutSec 5
        if ($health.status -eq "UP") { break }
    }
    catch {
        # The stack can take a few seconds to become reachable after Compose starts it.
    }

    if ((Get-Date) -ge $deadline) {
        throw "Gateway did not become healthy within $StartupTimeoutSeconds seconds."
    }
    Start-Sleep -Seconds 2
} while ($true)

$runId = "{0}-{1}" -f (Get-Date -Format "yyyyMMddHHmmss"), ([Guid]::NewGuid().ToString("N").Substring(0, 8))
$email = "smoke-$runId@nexabank.test"
$registration = Invoke-NexaApi -Method POST -Path "/api/v1/auth/register" -Body @{
    firstName = "Smoke"
    lastName = "Test"
    email = $email
    phone = "+919000000001"
    password = "NexaSmoke!2026"
}

$customerId = Get-JwtSubject -Token $registration.accessToken
$authHeaders = @{ Authorization = "Bearer $($registration.accessToken)" }

$savings = Invoke-NexaApi -Method POST -Path "/api/v1/accounts" -Headers $authHeaders -Body @{
    accountType = "SAVINGS"
    currency = "INR"
}
$current = Invoke-NexaApi -Method POST -Path "/api/v1/accounts" -Headers $authHeaders -Body @{
    accountType = "CURRENT"
    currency = "INR"
}

Assert-Equal -Expected $customerId -Actual $savings.customerId -Message "Savings account ownership mismatch."
Assert-Equal -Expected $customerId -Actual $current.customerId -Message "Current account ownership mismatch."

$depositHeaders = $authHeaders.Clone()
$depositHeaders["Idempotency-Key"] = "smoke-deposit-$runId"
$deposit = Invoke-NexaApi -Method POST -Path "/api/v1/accounts/$($savings.accountId)/deposits" `
    -Headers $depositHeaders -Body @{ amount = 25000.00 }
Assert-Equal -Expected ([decimal]25000) -Actual ([decimal]$deposit.balance) -Message "Deposit balance mismatch."

$transferHeaders = $authHeaders.Clone()
$transferHeaders["Idempotency-Key"] = "smoke-transfer-$runId"
$transferBody = @{
    sourceAccountId = $savings.accountId
    destinationAccountId = $current.accountId
    amount = 5000.00
}
$transfer = Invoke-NexaApi -Method POST -Path "/api/v1/transfers" -Headers $transferHeaders -Body $transferBody
$replayedTransfer = Invoke-NexaApi -Method POST -Path "/api/v1/transfers" -Headers $transferHeaders -Body $transferBody
Assert-Equal -Expected $transfer.transferId -Actual $replayedTransfer.transferId `
    -Message "Idempotent transfer replay returned a different transfer."

$savingsBalance = Invoke-NexaApi -Method GET -Path "/api/v1/accounts/$($savings.accountId)/balance" -Headers $authHeaders
$currentBalance = Invoke-NexaApi -Method GET -Path "/api/v1/accounts/$($current.accountId)/balance" -Headers $authHeaders
Assert-Equal -Expected ([decimal]20000) -Actual ([decimal]$savingsBalance.balance) -Message "Savings balance mismatch."
Assert-Equal -Expected ([decimal]5000) -Actual ([decimal]$currentBalance.balance) -Message "Current balance mismatch."

Write-Host "Waiting for the Kafka transaction projection ..."
$projectionDeadline = (Get-Date).AddSeconds($ProjectionTimeoutSeconds)
$projected = @()
do {
    $projected = @(Invoke-NexaApi -Method GET `
        -Path "/api/v1/transactions/account/$($savings.accountId)?minAmount=5000" -Headers $authHeaders)
    if ($projected.Where({ $_.transferId -eq $transfer.transferId }).Count -gt 0) { break }

    if ((Get-Date) -ge $projectionDeadline) {
        throw "Transfer did not appear in the transaction projection within $ProjectionTimeoutSeconds seconds."
    }
    Start-Sleep -Seconds 1
} while ($true)

$agentStatus = "not requested"
if ($IncludeAgent) {
    $agent = Invoke-NexaApi -Method POST -Path "/api/v1/agent/chat" -Headers $authHeaders -Body @{
        message = "What is my balance?"
    }
    if ([string]::IsNullOrWhiteSpace($agent.response)) {
        throw "Agent returned an empty response."
    }
    $agentStatus = "responded"
}

[pscustomobject]@{
    status = "PASS"
    customerId = $customerId
    savingsAccountId = $savings.accountId
    currentAccountId = $current.accountId
    transferId = $transfer.transferId
    savingsBalance = [decimal]$savingsBalance.balance
    currentBalance = [decimal]$currentBalance.balance
    projectedTransactions = $projected.Count
    agent = $agentStatus
} | Format-List
