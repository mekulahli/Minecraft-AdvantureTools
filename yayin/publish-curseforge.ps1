# ==============================================================================
# CurseForge Otomatik Yayınlama Betiği
# ==============================================================================

$root = Split-Path -Parent $PSScriptRoot
$envFile = Join-Path $root ".env"

if (-not (Test-Path $envFile)) {
    Write-Error ".env dosyası bulunamadı!"
    exit 1
}

# .env değişkenlerini yükle
Get-Content $envFile | ForEach-Object {
    if ($_ -match "^\s*([^#=]+)\s*=\s*(.*)$") {
        [System.Environment]::SetEnvironmentVariable($matches[1].Trim(), $matches[2].Trim())
    }
}

$token = [System.Environment]::GetEnvironmentVariable("CURSEFORGE_TOKEN")
$projectId = [System.Environment]::GetEnvironmentVariable("CURSEFORGE_PROJECT_ID")

if (-not $token -or -not $projectId) {
    Write-Error "CURSEFORGE_TOKEN veya CURSEFORGE_PROJECT_ID .env içinde bulunamadı!"
    exit 1
}

$jarFiles = Get-ChildItem (Join-Path $root "build\libs\*.jar") | Where-Object { $_.Name -notmatch "sources|javadoc" }
if (-not $jarFiles) {
    Write-Host "Jar dosyası bulunamadı. Derleniyor: ./gradlew build" -ForegroundColor Yellow
    Push-Location $root
    & .\gradlew.bat build -x test
    Pop-Location
    $jarFiles = Get-ChildItem (Join-Path $root "build\libs\*.jar") | Where-Object { $_.Name -notmatch "sources|javadoc" }
}

if (-not $jarFiles) {
    Write-Error "Derlenmiş jar dosyası bulunamadı!"
    exit 1
}

$jarPath = $jarFiles[0].FullName
$jarName = $jarFiles[0].Name
Write-Host "Yüklenecek dosya: $jarName" -ForegroundColor Cyan

# Changelog okuma
$changelogPath = Join-Path $root "CHANGELOG.md"
$changelogText = "## Macera Aletleri`n`nYeni surum yayinlandi."
if (Test-Path $changelogPath) {
    $changelogText = (Get-Content $changelogPath -Raw -Encoding utf8)
}

Add-Type -AssemblyName System.Net.Http
$client = New-Object System.Net.Http.HttpClient
$client.DefaultRequestHeaders.Add("X-Api-Token", $token)

$form = New-Object System.Net.Http.MultipartFormDataContent

$metaObj = @{
    changelog = $changelogText
    changelogType = "markdown"
    displayName = $jarFiles[0].BaseName
    gameVersions = @(16498, 10150, 9638, 9639, 14454) # Minecraft 26.2, NeoForge, Client, Server, Java 25
    releaseType = "beta"
}

$metadataJson = $metaObj | ConvertTo-Json
$stringContent = New-Object System.Net.Http.StringContent($metadataJson, [System.Text.Encoding]::UTF8, "application/json")
$form.Add($stringContent, "metadata")

$fileStream = [System.IO.File]::OpenRead($jarPath)
$fileContent = New-Object System.Net.Http.StreamContent($fileStream)
$fileContent.Headers.ContentType = [System.Net.Http.Headers.MediaTypeHeaderValue]::Parse("application/java-archive")
$form.Add($fileContent, "file", $jarName)

Write-Host "CurseForge'a yükleniyor (Proje ID: $projectId)..." -ForegroundColor Yellow
$response = $client.PostAsync("https://minecraft.curseforge.com/api/projects/$projectId/upload-file", $form).Result
$content = $response.Content.ReadAsStringAsync().Result

$fileStream.Close()
$client.Dispose()

if ($response.IsSuccessStatusCode) {
    Write-Host "Basariyla yuklendi! Yanit: $content" -ForegroundColor Green
} else {
    Write-Error "Yukleme hatasi: $($response.StatusCode) - $content"
}
