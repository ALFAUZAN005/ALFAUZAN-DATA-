# ALFAUZAN DATA - ProGuard Rules

# Keep app classes
-keep class com.alfauzan.data.** { *; }

# AndroidX / AppCompat
-keep class androidx.appcompat.** { *; }
-keep class androidx.core.** { *; }
-keep class androidx.biometric.** { *; }
-keep class androidx.security.crypto.** { *; }
-keep class androidx.swiperefreshlayout.** { *; }

# Material Components
-keep class com.google.android.material.** { *; }

# Lottie
-keep class com.airbnb.lottie.** { *; }
-dontwarn com.airbnb.lottie.**

# WebView
-keepclassmembers class * extends android.webkit.WebViewClient {
    public void *(android.webkit.WebView, java.lang.String, android.graphics.Bitmap);
    public boolean *(android.webkit.WebView, java.lang.String);
}
-keepclassmembers class * extends android.webkit.WebChromeClient {
    public void *(android.webkit.WebView, java.lang.String);
}

# ViewBinding
-keep class com.alfauzan.data.databinding.** { *; }

# Encrypted SharedPreferences / Tink
-keep class com.google.crypto.tink.** { *; }
-dontwarn com.google.crypto.tink.**

# General Android
-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable
-keep public class * extends java.lang.Exception

# Suppress warnings
-dontwarn javax.annotation.**
-dontwarn org.conscrypt.**
