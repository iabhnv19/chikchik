# ChikChik AI Assistant

ChikChik is an advanced Android accessibility-powered AI assistant built with Jetpack Compose. It enables hands-free screen reading and instant AI answers by capturing screen content via Accessibility Service and querying high-performance AI models.

## Features

- **Hands-Free Triggers**:
  - **Double-press Volume Up**: Triggers a scan of visible screen content.
  - **Double-press Volume Down**: Triggers a scroll scan of page content.
- **AI Integration**: Powered by Retrofit and Kotlinx Serialization, connecting to OpenAI-compatible AI APIs.
- **Secure Storage**: API keys are securely stored on device using Jetpack `EncryptedSharedPreferences`.
- **Modern Android Tech Stack**:
  - Jetpack Compose & Material 3
  - Kotlin Coroutines & Flow
  - LifecycleService & AccessibilityService
  - Gradle Build Config & Local Properties configuration

---

## Getting Started & GitHub Publication Setup

To prepare and build this project locally or publish it to GitHub:

### 1. Configure Local Properties
Create a `local.properties` file in the project root directory and add your API key:
```properties
sdk.dir=C\:\\Users\\YourUsername\\AppData\\Local\\Android\\Sdk
AI_API_KEY=your_api_key_here
```
*(You can use `local.properties.example` as a template).*

### 2. Build the Project
Open the project in Android Studio or build via command line:
```bash
./gradlew assembleDebug
```

### 3. Usage
1. Open the app and enter your API Key (or rely on `local.properties`).
2. Enable the **ChikChik Accessibility Service** in system settings when prompted.
3. Use physical volume button shortcuts anywhere on your device to interact with your screen using AI.

---

## License

Distributed under the MIT License. See `LICENSE` for more information.
