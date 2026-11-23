<div align="center">

# ChikChik AI Assistant

### Hands-free "ask AI about my screen" assistant on Android.

ChikChik is an advanced Android accessibility-powered AI assistant built with Jetpack Compose. It enables hands-free screen reading and instant AI answers by capturing screen content via an Accessibility Service and querying high-performance AI models using physical volume button shortcuts.

![Platform](https://img.shields.io/badge/platform-Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Language](https://img.shields.io/badge/language-Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)
![UI](https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)
![Storage](https://img.shields.io/badge/storage-EncryptedSharedPreferences-6DB33F?style=for-the-badge)
![License](https://img.shields.io/badge/license-MIT-blue?style=for-the-badge)

</div>

---

## Why this exists

Switching between apps to capture screenshots, copy text, and ask an AI assistant is slow and breaks your workflow. 

ChikChik turns physical volume shortcuts into an instant AI screen reader. Double-pressing Volume Up or Volume Down reads the current screen's text via the Android Accessibility Service, sends it to a high-performance AI model, and delivers a direct answer via a floating notification.

The project is designed around a fast, seamless workflow:

```text
Double-press volume key -> Capture screen nodes -> Process with AI -> Display answer notification
```

There is no account system, sign-in flow, tracking endpoint, or unnecessary background telemetry.

## What it provides

| Capability | Description |
|------------|-------------|
| Hands-free volume triggers | Double-press Volume Up (visible scan) or Volume Down (scroll scan) |
| Screen content extraction | Automatically reads UI text hierarchy using Accessibility Service |
| Foreground AI processing | Runs requests asynchronously in a LifecycleService with fallback model support |
| Secure API key storage | Stores your API key safely using Android's `EncryptedSharedPreferences` |
| Direct notifications | Delivers concise AI answers directly through high-priority notifications |
| Offline-friendly setup | Zero clutter, no login, open-source architecture |

## Features

### Volume button triggers

- **Double-press Volume Up**: Scans visible text on the active screen.
- **Double-press Volume Down**: Performs a scroll scan of page content.
- Global interceptor works across any app or screen on your device.

### Accessibility integration

- Inspects the active window's UI node tree.
- Extracts meaningful text while filtering out clutter.
- Operates securely with explicit system accessibility permissions.

### AI model fallback

- Automatically tries multiple high-performance AI models in sequence.
- Gracefully handles rate limits or downtime by falling back to alternative models.
- Provides direct, concise answers optimized for screen queries.

### Secure key management

- Enter your API key directly in the app UI.
- Securely persisted on-device using Android Jetpack `EncryptedSharedPreferences`.
- Optional support for build-time configuration via `local.properties`.

## Quick start

### Requirements

- Android Studio (Giraffe or newer recommended)
- Android SDK 36 (minSdk 26)
- A full JDK with a Java compiler
- Android Gradle Plugin 9.x
- Kotlin 2.x
- An Android device or emulator (Android 8.0 / API 26+)

### Clone the repository

```bash
git clone https://github.com/iabhnv19/chikchik.git
cd chikchik
```

### Configure API Key

Create a `local.properties` file in the project root directory and add your API key:
```properties
sdk.dir=C\:\\Users\\YourUsername\\AppData\\Local\\Android\\Sdk
AI_API_KEY=your_api_key_here
```
*(You can use `local.properties.example` as a template).*

### Open in Android Studio

1. Open Android Studio.
2. Select **Open** and choose the repository directory.
3. Wait for Gradle sync to finish.
4. Connect a device or start an emulator.
5. Select the `app` run configuration.
6. Press **Run**.

## Build and test

### Windows

Build a debug APK:

```bat
gradlew.bat assembleDebug
```

Run unit tests:

```bat
gradlew.bat testDebugUnitTest
```

### macOS and Linux

```bash
./gradlew assembleDebug
./gradlew testDebugUnitTest
```

The debug APK is generated at:

```text
app/build/outputs/apk/debug/app-debug.apk
```

## How to use

1. Open **ChikChik** on your Android device.
2. Enter your **AI API Key** in the input field.
3. Tap **Enable Accessibility Service** and turn on ChikChik in system accessibility settings.
4. Enable the AI Trigger switch in the app.
5. Double-press **Volume Up** or **Volume Down** on any screen to get instant AI answers!

## Architecture

```mermaid
flowchart TD
    A[Physical Volume Buttons] --> B[ChikChikAccessibilityService]
    B --> C[ChikChikForegroundService]
    C --> D[SecurityManager & EncryptedSharedPreferences]
    C --> E[ChatApiService (Retrofit)]
    E --> F[AI Backend API]
    F --> C
    C --> G[High-Priority Result Notification]
```

## Project structure

```text
chikchik/
├── app/
│   ├── build.gradle.kts
│   ├── proguard-rules.pro
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml
│       │   └── java/com/example/chikchik/
│       │       ├── data/                        # ChatApiService, ChatModels, SecurityManager
│       │       ├── service/                     # AccessibilityService & ForegroundService
│       │       ├── ui/theme/                    # Compose theme and colors
│       │       ├── MainActivity.kt              # Main settings and status dashboard
│       │       └── Screen.kt
│       └── res/                                 # Drawables, strings, accessibility config
├── gradle/
│   ├── libs.versions.toml
│   └── wrapper/
├── .gitignore
├── build.gradle.kts
├── gradle.properties
├── gradlew
├── gradlew.bat
├── local.properties.example
├── README.md
└── settings.gradle.kts
```

## Privacy and permissions

- **Accessibility Service**: Used exclusively to read screen text when volume shortcuts are triggered.
- **Foreground Service**: Runs AI requests asynchronously in the background.
- **Encrypted Local Storage**: API keys are encrypted on device using Android Keystore.
- **No Telemetry**: No tracking, advertising, or analytics SDKs are included.

## License

Distributed under the MIT License. See [LICENSE](LICENSE) for more information.

## Developer

Developed By
<div align="center">

<table>
  <tr>
    <td align="center">
      <a href="https://github.com/iabhnv19">
        <img src="https://github.com/iabhnv19.png" width="80" height="80" alt="Abhinav's GitHub profile picture" />
        <br />
        <strong>Abhinav</strong>
        <br />
        @iabhnv19
      </a>
    </td>
  </tr>
</table>
</div>

<div align="center">

**Built with Kotlin and Jetpack Compose for hands-free AI screen assistance.**

</div>
