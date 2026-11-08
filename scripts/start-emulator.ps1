# Starts the emulator without a window and waits until Android has finished booting.
# The very first boot took 14 minutes on my laptop, later ones are much faster.
param(
    [string]$Name = 'qa_api35',
    [string]$AvdHome = '',
    [int]$TimeoutMinutes = 20
)
$ErrorActionPreference = 'Stop'
$sdk = if ($env:ANDROID_HOME) { $env:ANDROID_HOME } else { Join-Path $env:LOCALAPPDATA 'Android\Sdk' }
if ($AvdHome) { $env:ANDROID_AVD_HOME = $AvdHome }
$adb = Join-Path $sdk 'platform-tools\adb.exe'
Start-Process (Join-Path $sdk 'emulator\emulator.exe') -WindowStyle Hidden -ArgumentList @(
    '-avd', $Name, '-no-window', '-no-audio', '-no-boot-anim', '-no-snapshot-save', '-gpu', 'swiftshader_indirect')
# adb reports 'no emulators found' on stderr until the device is up: that is expected here, not an error
$ErrorActionPreference = 'Continue'
$deadline = (Get-Date).AddMinutes($TimeoutMinutes)
while ((Get-Date) -lt $deadline) {
    $booted = & $adb -e shell getprop sys.boot_completed 2>$null
    if ("$booted".Trim() -eq '1') { "Booted: $(& $adb -e shell getprop ro.product.model)"; exit 0 }
    Start-Sleep -Seconds 5
}
throw "The emulator did not boot within $TimeoutMinutes minutes"
