package com.alfauzan.data.helpers;

import android.app.Activity;
import android.view.WindowManager;

public class SecurityHelper {

    private SecurityHelper() {
        // Prevent instantiation
    }

    /**
     * Prevents screenshots and screen recording on a given activity window.
     * Call this in onCreate(), before setContentView().
     */
    public static void applySecureWindow(Activity activity) {
        activity.getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE
        );
    }

    /**
     * Basic credential validation — username must be non-empty, password >= 4 chars.
     */
    public static boolean validateCredentials(String username, String password) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }
        if (password == null || password.length() < 4) {
            return false;
        }
        return true;
    }

    /**
     * Checks that a given URL starts with https://.
     */
    public static boolean isHttpsUrl(String url) {
        return url != null && url.startsWith("https://");
    }
}
