package com.alfauzan.data.helpers;

public final class Constants {

    private Constants() {
        // Prevent instantiation
    }

    // App
    public static final String APP_NAME = "ALFAUZAN DATA";

    // URLs
    public static final String BASE_URL = "https://alfauzandata.com.ng";
    public static final String DASHBOARD_URL = "https://alfauzandata.com.ng/index.html/dashboard.php";

    // Session Keys (EncryptedSharedPreferences)
    public static final String PREF_FILE = "alfauzan_secure_prefs";
    public static final String KEY_USERNAME = "session_username";
    public static final String KEY_PASSWORD = "session_password";
    public static final String KEY_IS_LOGGED_IN = "session_is_logged_in";
    public static final String KEY_BIOMETRIC_ENABLED = "session_biometric_enabled";

    // Splash Delay
    public static final long SPLASH_DELAY_MS = 2800L;

    // WebView
    public static final String USER_AGENT_SUFFIX = " AlfauzanDataApp/1.0";

    // Animation durations
    public static final int ANIM_DURATION_FAST = 250;
    public static final int ANIM_DURATION_NORMAL = 400;
    public static final int ANIM_DURATION_SLOW = 600;
}
