package com.alfauzan.data.helpers;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

public class SessionManager {

    private static final String TAG = "SessionManager";
    private static SessionManager instance;

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    private SessionManager(Context context) {
        try {
            MasterKey masterKey = new MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();

            sharedPreferences = EncryptedSharedPreferences.create(
                    context,
                    Constants.PREF_FILE,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
            editor = sharedPreferences.edit();
        } catch (Exception e) {
            Log.e(TAG, "Failed to init EncryptedSharedPreferences: " + e.getMessage());
            sharedPreferences = context.getSharedPreferences(Constants.PREF_FILE + "_fallback", Context.MODE_PRIVATE);
            editor = sharedPreferences.edit();
        }
    }

    public static synchronized SessionManager getInstance(Context context) {
        if (instance == null) {
            instance = new SessionManager(context.getApplicationContext());
        }
        return instance;
    }

    public void saveSession(String username, String password) {
        editor.putBoolean(Constants.KEY_IS_LOGGED_IN, true);
        editor.putString(Constants.KEY_USERNAME, username);
        editor.putString(Constants.KEY_PASSWORD, password);
        editor.putBoolean(Constants.KEY_BIOMETRIC_ENABLED, true);
        editor.apply();
    }

    public boolean isLoggedIn() {
        return sharedPreferences.getBoolean(Constants.KEY_IS_LOGGED_IN, false);
    }

    public String getUsername() {
        return sharedPreferences.getString(Constants.KEY_USERNAME, "");
    }

    public String getPassword() {
        return sharedPreferences.getString(Constants.KEY_PASSWORD, "");
    }

    public boolean isBiometricEnabled() {
        return sharedPreferences.getBoolean(Constants.KEY_BIOMETRIC_ENABLED, false);
    }

    public void setBiometricEnabled(boolean enabled) {
        editor.putBoolean(Constants.KEY_BIOMETRIC_ENABLED, enabled);
        editor.apply();
    }

    public void clearSession() {
        editor.clear();
        editor.apply();
    }

    public boolean hasCredentials() {
        String username = getUsername();
        return username != null && !username.isEmpty();
    }
}
