# Downloads Sauce Labs' My Demo App for Android and iOS. A file whose SHA-256 is not the published one is deleted.
$ErrorActionPreference = 'Stop'
$version = '2.3.0'
$apps = Join-Path $PSScriptRoot '..\apps'
New-Item -ItemType Directory -Force $apps | Out-Null
[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12

function Save-Verified([string]$url, [string]$target, [string]$sha256) {
    if (-not (Test-Path $target)) {
        Invoke-WebRequest -Uri $url -OutFile $target -UseBasicParsing
    }
    if ((Get-FileHash $target -Algorithm SHA256).Hash -ne $sha256) {
        Remove-Item $target
        throw "SHA-256 of $(Split-Path $target -Leaf) does not match the published value"
    }
}

# Android: the APK is used as it is
$apk = Join-Path $apps 'mda-2.3.0-27.apk'
Save-Verified "https://github.com/saucelabs/my-demo-app-android/releases/download/$version/mda-2.3.0-27.apk" `
    $apk 'BD3A3D6F2D50E076B2701ED2CC6A0CA82157B40A7033CF5BE9F5D084B6591EC8'

# iOS: the simulator zip holds Payload/My Demo App.app, which is moved to the name the tests use
$iosApp = Join-Path $apps 'SauceLabs-Demo-App.app'
if (-not (Test-Path $iosApp)) {
    $zip = Join-Path $apps 'SauceLabs-Demo-App.Simulator.zip'
    Save-Verified "https://github.com/saucelabs/my-demo-app-ios/releases/download/$version/SauceLabs-Demo-App.Simulator.zip" `
        $zip '576285B16A750115BD8A294E4C50C86A88BCF28BA3D167C09E4223E7C91E9F18'
    $extract = Join-Path $apps 'ios-extract'
    Expand-Archive -Path $zip -DestinationPath $extract -Force
    Move-Item (Join-Path $extract 'Payload\My Demo App.app') $iosApp
    Remove-Item $extract -Recurse -Force
}

"OK: $apk"
"OK: $iosApp"
