package com.alfauzan.data.services;

import android.content.Context;

import com.alfauzan.data.helpers.SessionManager;

public class AuthService {

    public interface AuthCallback {
        void onSuccess(String username);
        void onFailure(String errorMessage);
    }

    private final SessionManager sessionManager;

    public AuthService(Context context) {
        this.sessionManager = SessionManager.getInstance(context);
    }

    /**
     * Authenticates a user with username + password.
     * In a production environment this would call a secure backend API.
     * For this app the credentials are validated locally and stored securely.
     */
    public void login(String username, String password, AuthCallback callback) {
        if (username == null || username.trim().isEmpty()) {
            callback.onFailure("Username cannot be empty.");
            return;
        }
        if (password == null || password.length() < 4) {
            callback.onFailure("Password must be at least 4 characters.");
            return;
        }

        // Save session on first successful login
        sessionManager.saveSession(username.trim(), password);
        callback.onSuccess(username.trim());
    }

    /**
     * Validates returning user password against stored credentials.
     */
    public void loginWithPassword(String password, AuthCallback callback) {
        String storedPassword = sessionManager.getPassword();
        String storedUsername = sessionManager.getUsername();

        if (storedPassword == null || storedPassword.isEmpty()) {
            callback.onFailure("No stored credentials found. Please log in again.");
            return;
        }
        if (password == null || password.isEmpty()) {
            callback.onFailure("Please enter your password.");
            return;
        }
        if (storedPassword.equals(password)) {
            callback.onSuccess(storedUsername);
        } else {
            callback.onFailure("Incorrect password. Please try again.");
        }
    }

    public void logout() {
        sessionManager.clearSession();
    }
}
