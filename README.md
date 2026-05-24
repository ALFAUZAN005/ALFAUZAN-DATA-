# ALFAUZAN DATA — Android Fintech App

**Package:** `com.alfauzan.data`  
**Min SDK:** 23 (Android 6.0)  
**Target SDK:** 34 (Android 14)  
**Build System:** Gradle 8.1  
**Language:** Java (no Kotlin)

---

## Project Structure

```
AlfauzanData/
├── app/
│   ├── build.gradle
│   ├── proguard-rules.pro
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/alfauzan/data/
│       │   ├── activities/
│       │   │   ├── SplashActivity.java
│       │   │   ├── LoginActivity.java
│       │   │   ├── MainActivity.java
│       │   │   └── SecureWebViewActivity.java
│       │   ├── helpers/
│       │   │   ├── Constants.java
│       │   │   ├── SessionManager.java
│       │   │   ├── BiometricHelper.java
│       │   │   ├── SecurityHelper.java
│       │   │   └── NetworkHelper.java
│       │   └── services/
│       │       └── AuthService.java
│       └── res/
│           ├── layout/          (4 layouts)
│           ├── drawable/        (15 drawables)
│           ├── values/          (colors, strings, dimens, themes, styles)
│           ├── anim/            (4 animations)
│           ├── raw/             (fingerprint Lottie JSON)
│           ├── xml/             (network_security_config)
│           └── mipmap-*/        (launcher icons all densities)
├── build.gradle
├── settings.gradle
└── gradle.properties
```

---

## Setup Instructions for CodeAssist / AIDE

1. **Import Project**: Open CodeAssist → Import → select the `AlfauzanData` folder.
2. **Set SDK Path**: Edit `local.properties` and set your `sdk.dir`.
3. **Build**: Tap **Build → Make Project**.
4. **Run**: Connect device or start emulator → tap **Run**.

---

## App Flow

### First Launch
1. Splash screen (2.8 seconds, animated logo)
2. Login screen — enter **Username** + **Password** (min 4 chars)
3. On success: session saved securely → opens Dashboard WebView
4. Biometric auto-enabled for next login

### Returning User
1. Splash → Login screen shows **"Welcome Back, USERNAME"**
2. **Fingerprint button** auto-triggers biometric (if supported)
3. OR enter password manually → tap Login
4. **"Not my Account? Logout"** clears session → returns to first-launch view

---

## Security Features

| Feature | Implementation |
|---|---|
| Screenshot Prevention | `FLAG_SECURE` on all sensitive screens |
| Credential Storage | `EncryptedSharedPreferences` (AES-256-GCM) |
| Biometric Auth | `BiometricPrompt` API (real, not simulated) |
| HTTPS Enforcement | `network_security_config.xml` + SSL error cancellation |
| WebView Hardening | File access disabled, external URLs blocked |
| Session Management | Encrypted session, full clear on logout |

---

## Dependencies

```groovy
implementation 'androidx.appcompat:appcompat:1.6.1'
implementation 'com.google.android.material:material:1.9.0'
implementation 'androidx.constraintlayout:constraintlayout:2.1.4'
implementation 'androidx.biometric:biometric:1.1.0'
implementation 'androidx.security:security-crypto:1.1.0-alpha06'
implementation 'androidx.swiperefreshlayout:swiperefreshlayout:1.1.0'
implementation 'com.airbnb.android:lottie:6.1.0'
implementation 'androidx.core:core:1.12.0'
```

---

## Dashboard URL

```
https://alfauzandata.com.ng/index.html/dashboard.php
```

Loaded inside `SecureWebViewActivity` with:
- Pull-to-refresh
- Progress indicator
- Offline error UI with retry button
- Logout confirmation

---

## Customisation

- **App name/tagline**: `res/values/strings.xml`
- **Colors**: `res/values/colors.xml`
- **Dashboard URL**: `helpers/Constants.java` → `DASHBOARD_URL`
- **Logo**: Replace `res/drawable/ic_logo.xml` or add a PNG
