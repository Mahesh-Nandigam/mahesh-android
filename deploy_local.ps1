# ==============================================================================
# ⚡ MAHESH- ONE-CLICK WIRELESS ADB DEPLOY & LIVE LOGCAT STREAMER
# ==============================================================================

Write-Host "`n🚀 [1/3] Building & Installing Mahesh Debug APK..." -ForegroundColor Cyan
./gradlew installDebug

if ($LASTEXITCODE -eq 0) {
    Write-Host "`n✅ [2/3] Launching Mahesh on Device..." -ForegroundColor Green
    adb shell am start -n com.heymahesh.agent.debug/com.heymahesh.agent.MainActivity
    
    Write-Host "`n📡 [3/3] Streaming Live Logs for Mahesh (Press Ctrl+C to Stop)..." -ForegroundColor Yellow
    adb logcat -v time -s "HeyMahesh" "VoiceAgent" "AccessibilityPilot" "AndroidActionBridge"
} else {
    Write-Host "`n❌ Build or Install failed. Check logs above." -ForegroundColor Red
}
