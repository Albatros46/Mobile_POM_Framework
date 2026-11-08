# Creates the emulator once. The system image must be installed:
#   system-images;android-35;default;x86_64
# -AvdHome keeps the (large) virtual disk away from a full C: drive.
param(
    [string]$Name = 'qa_api35',
    [string]$Image = 'system-images;android-35;default;x86_64',
    [string]$Device = 'pixel_6',
    [string]$AvdHome = ''
)
$ErrorActionPreference = 'Stop'
$sdk = if ($env:ANDROID_HOME) { $env:ANDROID_HOME } else { Join-Path $env:LOCALAPPDATA 'Android\Sdk' }
if ($AvdHome) { New-Item -ItemType Directory -Force $AvdHome | Out-Null; $env:ANDROID_AVD_HOME = $AvdHome }
'no' | & (Join-Path $sdk 'cmdline-tools\latest\bin\avdmanager.bat') create avd -n $Name -k $Image -d $Device --force
& (Join-Path $sdk 'emulator\emulator.exe') -list-avds
