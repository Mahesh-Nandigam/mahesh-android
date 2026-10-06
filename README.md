# ⚡ Mahesh- (The Sovereign Voice AI Assistant for Android)

> **"Hey Mahesh"** — A multi-turn reasoning Android Voice Assistant and Autonomous Mobile Screen Pilot built with modern Kotlin 2.0 and Jetpack Compose.

---

## 🌟 Core Superpowers
* 🎤 **Offline Wake Word:** Low-power wake word listener (`"Hey Mahesh"`) running in background.
* 🧠 **GOAT-Level Reasoning:** Entity disambiguation (*"Found 3 Ramalingams: College, Uncle, Jio. Which one?"*) and missing parameter resolution.
* ⚡ **Direct Native System Bridge:** Direct calling via `TelecomManager`, contacts fuzzy resolution via `ContactsContract`, and `CameraX` auto-snap.
* 🦾 **Accessibility UI Screen Pilot:** Hands-free UI typing and sending for third-party apps (WhatsApp, Instagram).
* ✨ **Holographic System Overlay:** Glowing hardware-accelerated animated HUD rendering across any screen.

---

## 🏗️ Tech Stack
* **Language:** Kotlin 2.0+ (Coroutines, Flow, StateFlow)
* **UI:** Jetpack Compose + Material 3 Design System
* **Architecture:** Clean Architecture + MVVM + Unidirectional Data Flow (UDF)
* **Target SDK:** Android 15+ (API 35) | **Min SDK:** Android 8.0 (API 26)

---

## 🚀 Development & Local Testing
```bash
# Build debug APK
./gradlew assembleDebug

# Deploy to connected physical device over Wireless ADB
./gradlew installDebug
adb shell am start -n com.heymahesh.agent/.MainActivity
```
