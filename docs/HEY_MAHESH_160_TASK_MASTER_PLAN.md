# 📋 "MAHESH-" — THE 170-MICRO-TASK MASTER EXECUTION PLAN
### Production Roadmap: From Local Wireless ADB Testing to Google Play Console Internal Track

---

## 🏗️ MODULE 01: Architecture, Toolchain & GitHub CI/CD (Tasks 1–18)
- [x] **Task 001:** Initialize GitHub repository (`mahesh-android`) with clean `.gitignore` and branch protection rules.
- [x] **Task 002:** Configure Gradle Version Catalog (`gradle/libs.versions.toml`) for centralized dependency management.
- [x] **Task 003:** Configure Kotlin 2.0+ compiler, JVM 17/21 bytecode target, and Coroutines flags.
- [x] **Task 004:** Configure Android Jetpack Compose BOM and Material 3 design system.
- [x] **Task 005:** Set `minSdk = 26` (Android 8.0) and `targetSdk = 35` (Android 15+).
- [x] **Task 006:** Setup Clean Architecture folder hierarchy (`core`, `state`, `reasoning`, `audio`, `actions`, `accessibility`, `overlay`, `ui`).
- [x] **Task 007:** Configure Coroutine Dispatchers Provider (`Dispatchers.IO`, `Dispatchers.Default`, `Dispatchers.Main`).
- [x] **Task 008:** Configure `proguard-rules.pro` with keep rules for serialization models and JNI bindings.
- [x] **Task 009:** Set up custom `MaheshApplication.kt` with structured lifecycle logging.
- [x] **Task 010:** Set up DataStore / EncryptedSharedPreferences for secure local token and settings storage.
- [x] **Task 011:** Implement Network State Connectivity Manager (`NetworkCallback`).
- [x] **Task 012:** Set up GitHub Actions CI workflow (`.github/workflows/build_and_release.yml`) for automated APK builds.
- [x] **Task 013:** Configure Wireless ADB one-click deploy script (`deploy_local.ps1`) for instant testing on your physical phone.
- [x] **Task 014:** Configure live log streaming filter (`adb logcat -s "HeyMahesh"`) for real-time terminal debugging.
- [x] **Task 015:** Build base `MainActivity.kt` with Compose `Surface` and `MaheshTheme`.
- [x] **Task 016:** Add unit test framework dependencies (`kotlinx-coroutines-test`, `mockk`).
- [x] **Task 017:** Verify empty baseline debug APK compilation.
- [x] **Task 018:** Validate 1-click install on local physical device over Wi-Fi.

---

## 🧠 MODULE 02: Core Data Models & Tool Schemas (Tasks 19–34)
- [x] **Task 019:** Define sealed interface `AssistantState` (`Idle`, `Listening`, `Thinking`, `Disambiguating`, `Executing`, `Speaking`, `Error`).
- [x] **Task 020:** Define `ContactEntity` data class (`id`, `name`, `phoneNumber`, `tag`, `avatarUri`).
- [x] **Task 021:** Define `ActionType` enum (`PHONE_CALL`, `WHATSAPP_MESSAGE`, `INSTAGRAM_DM`, `CAMERA_SNAP`, `FLASHLIGHT`, `APP_LAUNCH`, `ALARM`, `GENERAL_CHAT`).
- [x] **Task 022:** Define `ToolDefinition` and JSON Schema mapping classes for LLM tool calling.
- [x] **Task 023:** Define `PendingDisambiguationContext` model to store multiple matching contact candidates.
- [x] **Task 024:** Define `PendingSlotContext` model to track missing parameters (e.g. missing message text).
- [x] **Task 025:** Define `ResolvedAction` sealed class with payload schemas for every tool.
- [x] **Task 026:** Define `VoiceFeedbackConfig` data class (pitch, speed, tone, voice persona).
- [x] **Task 027:** Define `AppPermissionState` data class (Audio, Overlay, Accessibility, Contacts, Phone).
- [x] **Task 028:** Define `CommandHistoryItem` model for logging past user interactions.
- [x] **Task 029:** Implement JSON serialization / deserialization helpers using Kotlinx Serialization.
- [x] **Task 030:** Create in-memory caching repository for active session state.
- [x] **Task 031:** Write unit tests for all state models and serialization parity.
- [x] **Task 032:** Create mock fixtures for simulated user voice commands and tool outputs.
- [x] **Task 033:** Validate thread-safety of state models under concurrent asynchronous access.
- [x] **Task 034:** Document all data models in codebase architecture docs.

---

## ⚡ MODULE 03: GOAT-Level Multi-Turn Reasoning Engine (Tasks 35–54)
- [x] **Task 035:** Create `ReasoningEngine` core interface and state-machine contract.
- [x] **Task 036:** Implement fast local Intent Classifier to separate phone actions from general questions.
- [x] **Task 037:** Build Contact Disambiguation Handler (detects $\ge 2$ matching names in contacts).
- [x] **Task 038:** Implement phonetic & fuzzy matching algorithm for complex Indian names.
- [x] **Task 039:** Implement Disambiguation Question Synthesizer (*"Found 3 Ramalingams: College, Uncle, Jio. Which one?"*).
- [x] **Task 040:** Build User Choice Resolver (*"College one"*, *"Second one"*, *"Uncle"*).
- [x] **Task 041:** Build Missing Argument Slot-Filler for WhatsApp (*"What should I say to Rahul?"*).
- [x] **Task 042:** Build Missing Argument Slot-Filler for Phone Calls (*"Who should I call?"*).
- [x] **Task 043:** Implement Multi-Turn Session Memory with configurable 15-second conversational window.
- [x] **Task 044:** Implement LLM Tool-Calling Prompt Template with strict JSON output formatting.
- [x] **Task 045:** Integrate Gemini 2.0 Flash / Groq LLaMA 3.3 for sub-300ms cloud reasoning.
- [x] **Task 046:** Build Offline Rule-Based Fallback Parser for zero-latency execution without internet.
- [x] **Task 047:** Add Self-Correction Handler (recovers when user changes mind mid-sentence).
- [x] **Task 048:** Add Filler-Word Stripper (*"bro"*, *"please"*, *"fast"*, *"on speaker"*, *"can you"*).
- [x] **Task 049:** Add Natural Time & Alarm Parsing logic (*"in 20 minutes"*, *"tomorrow 7 AM"*).
- [x] **Task 050:** Add App-Name Normalizer (*"Insta" $\rightarrow$ "Instagram"*, *"YT" $\rightarrow$ "YouTube"*).
- [x] **Task 051:** Implement fallback personality responses for general chit-chat.
- [x] **Task 052:** Write 30+ comprehensive unit tests for multi-turn reasoning dialogs.
- [x] **Task 053:** Benchmark reasoning latency and ensure $< 150\text{ms}$ parsing overhead.
- [x] **Task 054:** Test reasoning engine with simulated voice transcripts in test suite.

---

## 🎙️ MODULE 04: Speech-to-Text (STT) & Audio Ingestion (Tasks 55–70)
- [x] **Task 055:** Configure `AudioRecord` PCM 16-bit 16kHz mono audio capture stream.
- [x] **Task 056:** Build low-memory circular audio buffer to prevent GC garbage collection spikes.
- [x] **Task 057:** Implement Voice Activity Detection (VAD) to auto-detect silence / end-of-speech.
- [x] **Task 058:** Integrate Android native `SpeechRecognizer` with `RecognitionListener`.
- [x] **Task 059:** Implement partial transcript streaming flow for real-time UI text feedback.
- [x] **Task 060:** Integrate Cloud Fast Whisper STT (Groq API) for ultra-accurate Indian accent capture.
- [x] **Task 061:** Build hybrid STT switcher (runs native offline STT, switches to cloud if connected).
- [x] **Task 062:** Add noise suppression and acoustic echo cancellation (`NoiseSuppressor`, `AcousticEchoCanceler`).
- [x] **Task 063:** Implement audio stream gain booster for distant mic capture in noisy environments.
- [x] **Task 064:** Handle Bluetooth headset / TWS earphone audio routing (`ACTION_SCO_AUDIO_STATE_UPDATED`).
- [x] **Task 065:** Handle audio focus transitions (`AudioManager.OnAudioFocusChangeListener`).
- [x] **Task 066:** Implement STT error recovery and automatic restart logic.
- [x] **Task 067:** Add simulated audio feeder for automated testing without speaking aloud.
- [x] **Task 068:** Measure audio buffer memory consumption and verify zero leaks.
- [x] **Task 069:** Test microphone permission handling on Android 13/14/15.
- [x] **Task 070:** Complete end-to-end Voice-to-Text integration test.

---

## 🔊 MODULE 05: Offline Wake-Word Detection Engine ("Hey Mahesh") (Tasks 71–84)
- [x] **Task 071:** Integrate Porcupine / openWakeWord C/C++ native JNI bindings.
- [x] **Task 072:** Create custom trained keyword model for `"Hey Mahesh"`.
- [x] **Task 073:** Create secondary fallback keyword models (`"Mahesh"`, `"Hey Assistant"`).
- [x] **Task 074:** Build `WakeWordListener` running in dedicated background worker thread.
- [x] **Task 075:** Implement sensitivity threshold tuning (0.0 to 1.0 slider) to eliminate false triggers.
- [x] **Task 076:** Build wake-word audio chime generator (subtle audio ding upon trigger).
- [x] **Task 077:** Implement screen-off audio wake-lock management.
- [x] **Task 078:** Implement battery-saving sleep cycles when phone is stationery in pocket.
- [x] **Task 079:** Add proximity sensor check to prevent waking up inside tight pockets.
- [x] **Task 080:** Add toggle in UI to enable/disable wake-word detection on demand.
- [x] **Task 081:** Build diagnostic wake-word trigger counter and accuracy logger.
- [x] **Task 082:** Test wake-word reliability across 5 different room acoustics / background noises.
- [x] **Task 083:** Verify overall wake-word battery consumption stays under 1.5% per 24 hours.
- [x] **Task 084:** Test wake-word activation while screen is locked.

---

## 🗣️ MODULE 06: Text-to-Speech (TTS) & Voice Synthesis (Tasks 85–97)
- [x] **Task 085:** Initialize Android native `TextToSpeech` engine with locale auto-detection (`en-IN`, `en-US`).
- [x] **Task 086:** Implement `UtteranceProgressListener` to synchronize speech finish with UI state.
- [x] **Task 087:** Configure optimal speech rate (1.15x) and pitch for crisp, modern assistant feel.
- [x] **Task 088:** Implement Cloud TTS client (Cartesia / ElevenLabs Turbo) for human-like high-fidelity voice.
- [x] **Task 089:** Build audio cache to instantly replay frequent phrases (*"Calling now"*, *"What message?"*).
- [x] **Task 090:** Implement speech ducking (lowers background music volume while assistant speaks).
- [x] **Task 091:** Implement instant speech interrupt (stops speaking immediately if user speaks again).
- [x] **Task 092:** Add earphone/headset dedicated speech channel selector.
- [x] **Task 093:** Add voice personality customizer (Friendly, Sci-Fi Jarvis, Professional).
- [x] **Task 094:** Handle TTS engine initialization failures with graceful silent visual fallback.
- [x] **Task 095:** Write unit tests for TTS queue management and concurrency.
- [x] **Task 096:** Test speakerphone vs earpiece output routing.
- [x] **Task 097:** Complete end-to-end reasoning $\rightarrow$ TTS voice output pipeline test.

---

## 📱 MODULE 07: Native Android Action Bridge (Tasks 98–118)
- [x] **Task 098:** Build `ContactsProviderHelper` using Android `ContactsContract` queries.
- [x] **Task 099:** Implement phonetic normalization for contacts search (handles spelling variations).
- [x] **Task 100:** Build `TelecomManager` direct phone dialer using `Intent.ACTION_CALL`.
- [x] **Task 101:** Implement speakerphone auto-activation via `AudioManager.isSpeakerphoneOn`.
- [x] **Task 102:** Build `CameraX` controller supporting front and back camera lifecycle.
- [x] **Task 103:** Implement automatic photo capture with file storage in standard DCIM/Mahesh gallery.
- [x] **Task 104:** Implement audible countdown timer (3-2-1) before snapping the photo.
- [x] **Task 105:** Build Torch / Flashlight controller via `CameraManager.setTorchMode`.
- [x] **Task 106:** Build Alarm and Timer dispatcher using `AlarmClock.ACTION_SET_ALARM`.
- [x] **Task 107:** Build Dynamic App Launcher (scans `PackageManager` to launch any installed app by name).
- [x] **Task 108:** Build Volume & Media playback controller (play/pause/next track).
- [x] **Task 109:** Build Bluetooth & WiFi system settings intent launcher.
- [x] **Task 110:** Build Battery Level and Charging Status query reader.
- [x] **Task 111:** Build DND (Do Not Disturb) mode controller (`NotificationManager`).
- [x] **Task 112:** Build WhatsApp direct chat opener using URI `https://api.whatsapp.com/send?phone=...`.
- [x] **Task 113:** Build Instagram profile/inbox opener using custom intent `instagram://...`.
- [x] **Task 114:** Add security permission guard before executing sensitive system actions.
- [x] **Task 115:** Implement execution result feedback model (reports success/failure to reasoning engine).
- [x] **Task 116:** Write comprehensive unit & integration tests for all system bridges.
- [x] **Task 117:** Benchmark execution speed: ensure all direct actions trigger in $< 200\text{ms}$.
- [x] **Task 118:** Test direct calling on physical device with multiple SIM cards.

---

## 🦾 MODULE 08: Deep Screen Pilot & Accessibility Automation (Tasks 119–137)
- [x] **Task 119:** Create `ScreenPilotAccessibilityService` extending Android `AccessibilityService`.
- [x] **Task 120:** Configure `accessibility_service_config.xml` with `canRetrieveWindowContent` flags.
- [x] **Task 121:** Implement recursive UI Node hierarchy crawler (`AccessibilityNodeInfo`).
- [x] **Task 122:** Build search algorithm to locate `EditText` input nodes by ID, hint text, or class name.
- [x] **Task 123:** Build `performSetText` helper using `ACTION_SET_TEXT` with `Bundle` arguments.
- [x] **Task 124:** Build search algorithm to locate clickable "Send" / "Submit" button nodes.
- [x] **Task 125:** Build `performClick` helper with fallback to bounding box coordinate gestures.
- [x] **Task 126:** Implement WhatsApp Auto-Pilot: search contact $\rightarrow$ enter chat $\rightarrow$ type message $\rightarrow$ click Send.
- [x] **Task 127:** Implement Instagram DM Auto-Pilot: open inbox $\rightarrow$ search user $\rightarrow$ type message $\rightarrow$ send.
- [x] **Task 128:** Implement Home screen return gesture (`GLOBAL_ACTION_HOME`) after action completion.
- [x] **Task 129:** Implement back button navigation gesture (`GLOBAL_ACTION_BACK`).
- [x] **Task 130:** Add safety interceptor: automatically freeze automation if password/payment PIN screen is detected.
- [x] **Task 131:** Implement retry mechanism with exponential backoff if UI node is slow to load.
- [x] **Task 132:** Build Accessibility Service status checker & direct settings shortcut launcher.
- [x] **Task 133:** Add visual highlight overlay over target nodes during automation execution.
- [x] **Task 134:** Handle WhatsApp multi-version UI variations across different Android builds.
- [x] **Task 135:** Write automated UI simulation tests for accessibility navigation.
- [x] **Task 136:** Test zero-memory-leak node recycling (`AccessibilityNodeInfo.recycle()`).
- [x] **Task 137:** Test hands-free WhatsApp messaging flow end-to-end on physical phone.

---

## ✨ MODULE 09: System Window HUD Overlay & Futuristic UI (Tasks 138–154)
- [x] **Task 138:** Build `FloatingHUDController` using `WindowManager` and `TYPE_APPLICATION_OVERLAY`.
- [x] **Task 139:** Implement ComposeView host inside WindowManager for seamless Jetpack Compose rendering.
- [x] **Task 140:** Build Siri/Jarvis glowing edge-of-screen animated shader waveform.
- [x] **Task 141:** Build Central Holographic Voice Orb with dynamic audio amplitude scaling.
- [x] **Task 142:** Implement real-time live transcription subtitle card at screen bottom.
- [x] **Task 143:** Build Interactive Disambiguation Chip Carousel (clickable contact choice pills).
- [x] **Task 144:** Build Action Status Toast (e.g. *"Calling Ramalingam College..."* with cancel button).
- [x] **Task 145:** Implement touch-outside to dismiss or minimize overlay HUD.
- [x] **Task 146:** Add smooth entering and exiting transitions using `AnimatedVisibility` and Spring physics.
- [x] **Task 147:** Build Floating Mini-Pill widget when minimized to screen corner.
- [x] **Task 148:** Implement Drag-to-move floating pill gesture with edge-snapping physics.
- [x] **Task 149:** Build Main Activity Settings Dashboard with Material 3 Dark/Light themes.
- [x] **Task 150:** Build Permissions Onboarding Carousel with interactive grant buttons and status indicators.
- [x] **Task 151:** Build Live Voice Playground screen to test all voice features with visual logs.
- [x] **Task 152:** Build Command History & Analytics screen.
- [x] **Task 153:** Ensure 60/120 FPS buttery smooth animation rendering without frame drops.
- [x] **Task 154:** Test overlay rendering over third-party apps (YouTube, WhatsApp, Instagram).

---

## 🚀 MODULE 10: Performance, Play Store Compliance & Launch (Tasks 155–170)
- [x] **Task 155:** Create Google Play Prominent Disclosure Modal for Accessibility Services.
- [x] **Task 156:** Create Google Play Microphone Background Policy compliance declaration.
- [x] **Task 157:** Set up Foreground Service Notification with custom action buttons (Mute, Stop, Settings).
- [x] **Task 158:** Configure Android 14+ `ServiceType.MICROPHONE` in `AndroidManifest.xml`.
- [x] **Task 159:** Implement dynamic runtime permissions manager for Android 13+ (`POST_NOTIFICATIONS`, `RECORD_AUDIO`).
- [x] **Task 160:** Run LeakCanary audit and eliminate all Activity / Service memory leaks.
- [x] **Task 161:** Run Android Lint & StrictMode checks to eliminate main-thread disk/network IO.
- [x] **Task 162:** Configure release ProGuard / R8 code shrinking and resource optimization.
- [x] **Task 163:** Set up Keystore signing configuration for release builds.
- [x] **Task 164:** Build Release Android App Bundle (`.aab`) with split APK architecture.
- [x] **Task 165:** Create Privacy Policy document covering offline audio processing and zero data resale.
- [x] **Task 166:** Generate high-resolution App Icon, Splash Screen, and Feature Banners for Play Store.
- [x] **Task 167:** Create 30-second App Demonstration Video for Google Play review verification.
- [x] **Task 168:** Prepare Google Play Store listing metadata, keywords, and description.
- [x] **Task 169:** Perform complete 100% production verification test on physical device.
- [x] **Task 170:** 🎉 **Upload AAB to Google Play Console Internal Testing track for college friends to test!**

---
*Updated with 170 micro-tasks aligned with local Wireless ADB testing and Google Play Internal Testing release.*
