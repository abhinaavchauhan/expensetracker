Add-Type -AssemblyName System.Drawing
$files = @(
    "d:\Projects\Expence Tracker\app\src\main\res\drawable\ic_logo_premium.png",
    "d:\Projects\Expence Tracker\app\src\main\res\drawable\img_avatar.png",
    "d:\Projects\Expence Tracker\app\src\main\res\drawable\user_avatar_premium.png",
    "d:\Projects\Expence Tracker\app\src\main\res\drawable\user_profile_avatar.png"
)

foreach ($file in $files) {
    if (Test-Path $file) {
        Write-Host "Converting $file ..."
        $tmpFile = $file + ".tmp"
        $img = [System.Drawing.Image]::FromFile($file)
        $img.Save($tmpFile, [System.Drawing.Imaging.ImageFormat]::Png)
        $img.Dispose()
        Remove-Item $file
        Rename-Item $tmpFile $file
        Write-Host "Done."
    } else {
        Write-Host "File NOT found: $file"
    }
}
