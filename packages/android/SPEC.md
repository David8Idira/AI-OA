# AI-OA Android Technical Specification

## 1. Project Overview

**Project Name:** AI-OA  
**Type:** Android Native Application  
**Core Functionality:** Intelligent Office Automation Platform providing workflow approval, scheduling, messaging, and AI-powered productivity tools.

## 2. Technology Stack & Choices

### Framework & Language
- **Language:** Kotlin 1.9.22
- **Min SDK:** 26 (Android 8.0)
- **Target SDK:** 34 (Android 14)
- **Compile SDK:** 34

### Key Libraries/Dependencies
| Category | Library | Version |
|----------|---------|---------|
| UI | Jetpack Compose BOM | 2024.01.00 |
| Navigation | Navigation Compose | 2.7.6 |
| DI | Hilt | 2.50 |
| Network | Retrofit | 2.9.0 |
| Network | OkHttp | 4.12.0 |
| Async | Kotlin Coroutines | 1.7.3 |
| Serialization | Kotlinx Serialization | 1.6.2 |
| Storage | DataStore Preferences | 1.0.0 |

### State Management
- **Approach:** Unidirectional Data Flow (UDF)
- **Implementation:** StateFlow + ViewModel
- **Side Effects:** Coroutines + Flow

### Architecture Pattern
- **Pattern:** MVVM + Clean Architecture
- **Layers:**
  - **UI Layer:** Compose screens, ViewModels
  - **Domain Layer:** Use cases (implicit in ViewModels for simplicity)
  - **Data Layer:** Repositories, API services, data models

## 3. Feature List

### Authentication
- [x] Login screen with username/password
- [x] Form validation
- [x] Loading states and error handling
- [ ] Token storage and refresh
- [ ] Auto-login on app restart

### Home Dashboard
- [x] Welcome message
- [x] Quick action buttons (审批/日程/消息)
- [x] Logout functionality

### Navigation
- [x] Navigation Compose setup
- [x] Login ↔ Home navigation flow
- [ ] Bottom navigation (future)

### Network
- [x] Retrofit + OkHttp setup
- [x] API client with error handling
- [x] Network connectivity check

## 4. UI/UX Design Direction

### Visual Style
- **Design System:** Material Design 3 (Material You)
- **Theme:** Dynamic colors with fallback to custom theme
- **Style:** Clean, professional, productivity-focused

### Color Scheme
- **Primary:** Blue (#2196F3)
- **Secondary:** Teal (#03DAC6)
- **Background:** Light gray (#FAFAFA) / Dark (#121212)
- **Support:** Light/Dark theme auto-detection

### Layout Approach
- **Structure:** Single activity with Compose navigation
- **Screens:** Full-screen layouts with TopAppBar
- **Components:** Material 3 components (OutlinedTextField, Button, Card)

## 5. Project Structure

```
packages/android/
├── app/
│   ├── src/main/
│   │   ├── java/com/aioa/app/
│   │   │   ├── AioaApplication.kt
│   │   │   ├── MainActivity.kt
│   │   │   ├── config/
│   │   │   │   ├── ApiConfig.kt
│   │   │   │   └── AppConfig.kt
│   │   │   ├── data/
│   │   │   │   ├── api/
│   │   │   │   │   ├── AioaApiClient.kt
│   │   │   │   │   └── AioaApiService.kt
│   │   │   │   └── model/
│   │   │   │       ├── Result.kt
│   │   │   │       └── User.kt
│   │   │   ├── di/
│   │   │   │   └── NetworkModule.kt
│   │   │   ├── navigation/
│   │   │   │   └── AioaNavHost.kt
│   │   │   ├── ui/
│   │   │   │   ├── home/
│   │   │   │   │   └── HomeScreen.kt
│   │   │   │   ├── login/
│   │   │   │   │   ├── LoginScreen.kt
│   │   │   │   │   └── LoginViewModel.kt
│   │   │   │   └── theme/
│   │   │   │       ├── Color.kt
│   │   │   │       ├── Theme.kt
│   │   │   │       └── Type.kt
│   │   │   └── util/
│   │   │       └── NetworkUtils.kt
│   │   ├── res/
│   │   │   ├── values/
│   │   │   │   ├── strings.xml
│   │   │   │   └── themes.xml
│   │   │   └── xml/
│   │   │       └── network_security_config.xml
│   │   └── AndroidManifest.xml
│   └── build.gradle.kts
├── scripts/
│   └── build.sh
├── build.gradle.kts
├── gradle.properties
├── gradlew
├── settings.gradle.kts
└── SPEC.md
```

## 6. Build Instructions

```bash
# Debug build
./scripts/build.sh debug

# Release build
./scripts/build.sh release

# Clean build
./scripts/build.sh debug clean
```

## 7. TODO / Future Enhancements

- [ ] Add repository layer for data management
- [ ] Implement token storage with DataStore
- [ ] Add Hilt ViewModel assisted injection for SavedStateHandle
- [ ] Implement auto-login flow
- [ ] Add splash screen
- [ ] Add settings/preferences screen
- [ ] Setup CI/CD with GitHub Actions
- [ ] Add unit tests and UI tests
- [ ] ProGuard rules for release build