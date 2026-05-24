package com.alfauzan.data.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.alfauzan.data.R;
import com.alfauzan.data.databinding.ActivityLoginBinding;
import com.alfauzan.data.helpers.BiometricHelper;
import com.alfauzan.data.helpers.Constants;
import com.alfauzan.data.helpers.NetworkHelper;
import com.alfauzan.data.helpers.SecurityHelper;
import com.alfauzan.data.helpers.SessionManager;
import com.alfauzan.data.services.AuthService;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private SessionManager sessionManager;
    private AuthService authService;

    private boolean isPasswordVisible = false;
    private boolean isReturnPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SecurityHelper.applySecureWindow(this);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sessionManager = SessionManager.getInstance(this);
        authService = new AuthService(this);

        decideLoginView();
    }

    private void decideLoginView() {
        if (sessionManager.isLoggedIn() && sessionManager.hasCredentials()) {
            showReturningUserView();
        } else {
            showFirstLaunchView();
        }
    }

    // ===================== FIRST LAUNCH =====================

    private void showFirstLaunchView() {
        binding.firstLaunchView.setVisibility(View.VISIBLE);
        binding.returningUserView.setVisibility(View.GONE);

        Animation slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up);
        binding.loginCard.startAnimation(slideUp);

        setupFirstLaunchListeners();
    }

    private void setupFirstLaunchListeners() {
        binding.btnBack.setOnClickListener(v -> finish());

        binding.tvShowPassword.setOnClickListener(v -> togglePasswordVisibility());

        binding.btnLogin.setOnClickListener(v -> attemptFirstLogin());

        binding.tvForgotPassword.setOnClickListener(v ->
                showInfoDialog("Forgot Password", "Please contact support at support@alfauzandata.com.ng to reset your password.")
        );

        binding.tvSignUp.setOnClickListener(v ->
                showInfoDialog("Sign Up", "Please visit alfauzandata.com.ng to create your account.")
        );

        binding.etPassword.setOnEditorActionListener((v, actionId, event) -> {
            attemptFirstLogin();
            return true;
        });
    }

    private void togglePasswordVisibility() {
        isPasswordVisible = !isPasswordVisible;
        if (isPasswordVisible) {
            binding.etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            binding.tvShowPassword.setText(R.string.btn_hide);
        } else {
            binding.etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            binding.tvShowPassword.setText(R.string.btn_show);
        }
        binding.etPassword.setSelection(binding.etPassword.getText().length());
    }

    private void attemptFirstLogin() {
        String username = binding.etUsername.getText().toString().trim();
        String password = binding.etPassword.getText().toString();

        if (username.isEmpty()) {
            binding.etUsername.setError(getString(R.string.error_username_empty));
            binding.etUsername.requestFocus();
            return;
        }
        if (password.isEmpty()) {
            binding.etPassword.setError(getString(R.string.error_password_empty));
            binding.etPassword.requestFocus();
            return;
        }
        if (password.length() < 4) {
            binding.etPassword.setError(getString(R.string.error_password_short));
            binding.etPassword.requestFocus();
            return;
        }

        showLoginProgress(true);

        authService.login(username, password, new AuthService.AuthCallback() {
            @Override
            public void onSuccess(String uname) {
                runOnUiThread(() -> {
                    showLoginProgress(false);
                    navigateToDashboard();
                });
            }

            @Override
            public void onFailure(String errorMessage) {
                runOnUiThread(() -> {
                    showLoginProgress(false);
                    showErrorDialog(errorMessage);
                });
            }
        });
    }

    private void showLoginProgress(boolean show) {
        binding.loginProgress.setVisibility(show ? View.VISIBLE : View.GONE);
        binding.btnLogin.setEnabled(!show);
        binding.btnLogin.setAlpha(show ? 0.7f : 1.0f);
    }

    // ===================== RETURNING USER =====================

    private void showReturningUserView() {
        binding.firstLaunchView.setVisibility(View.GONE);
        binding.returningUserView.setVisibility(View.VISIBLE);

        Animation slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up);
        binding.returningCard.startAnimation(slideUp);

        String savedUsername = sessionManager.getUsername().toUpperCase();
        binding.tvSavedUsername.setText(savedUsername);

        boolean biometricAvailable = BiometricHelper.isBiometricAvailable(this);
        binding.fingerprintLayout.setVisibility(biometricAvailable ? View.VISIBLE : View.GONE);

        setupReturningUserListeners();

        // Auto-trigger biometric if available
        if (biometricAvailable && sessionManager.isBiometricEnabled()) {
            binding.returningCard.post(this::triggerBiometricAuth);
        }
    }

    private void setupReturningUserListeners() {
        binding.tvShowReturnPassword.setOnClickListener(v -> toggleReturnPasswordVisibility());

        binding.btnFingerprint.setOnClickListener(v -> triggerBiometricAuth());

        binding.btnReturnLogin.setOnClickListener(v -> attemptReturnLogin());

        binding.tvNotMyAccount.setOnClickListener(v -> showLogoutConfirmation());

        binding.etReturnPassword.setOnEditorActionListener((v, actionId, event) -> {
            attemptReturnLogin();
            return true;
        });
    }

    private void toggleReturnPasswordVisibility() {
        isReturnPasswordVisible = !isReturnPasswordVisible;
        if (isReturnPasswordVisible) {
            binding.etReturnPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            binding.tvShowReturnPassword.setText(R.string.btn_hide);
        } else {
            binding.etReturnPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            binding.tvShowReturnPassword.setText(R.string.btn_show);
        }
        binding.etReturnPassword.setSelection(binding.etReturnPassword.getText().length());
    }

    private void triggerBiometricAuth() {
        BiometricHelper.showBiometricPrompt(
                this,
                getString(R.string.biometric_title),
                getString(R.string.biometric_subtitle),
                getString(R.string.biometric_cancel),
                new BiometricHelper.BiometricCallback() {
                    @Override
                    public void onSuccess() {
                        navigateToDashboard();
                    }

                    @Override
                    public void onFailure(String message) {
                        // User may retry or use password — no action needed
                    }

                    @Override
                    public void onError(String errorMessage) {
                        showErrorDialog(getString(R.string.biometric_error));
                    }
                }
        );
    }

    private void attemptReturnLogin() {
        String password = binding.etReturnPassword.getText().toString();

        if (password.isEmpty()) {
            binding.etReturnPassword.setError(getString(R.string.error_password_empty));
            binding.etReturnPassword.requestFocus();
            return;
        }

        showReturnLoginProgress(true);

        authService.loginWithPassword(password, new AuthService.AuthCallback() {
            @Override
            public void onSuccess(String username) {
                runOnUiThread(() -> {
                    showReturnLoginProgress(false);
                    navigateToDashboard();
                });
            }

            @Override
            public void onFailure(String errorMessage) {
                runOnUiThread(() -> {
                    showReturnLoginProgress(false);
                    showErrorDialog(errorMessage);
                });
            }
        });
    }

    private void showReturnLoginProgress(boolean show) {
        binding.returnLoginProgress.setVisibility(show ? View.VISIBLE : View.GONE);
        binding.btnReturnLogin.setEnabled(!show);
        binding.btnReturnLogin.setAlpha(show ? 0.7f : 1.0f);
    }

    private void showLogoutConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.app_name)
                .setMessage(R.string.logout_confirm)
                .setPositiveButton(R.string.btn_logout, (dialog, which) -> {
                    authService.logout();
                    dialog.dismiss();
                    showFirstLaunchView();
                })
                .setNegativeButton(R.string.btn_cancel, (dialog, which) -> dialog.dismiss())
                .show();
    }

    // ===================== NAVIGATION =====================

    private void navigateToDashboard() {
        if (!NetworkHelper.isNetworkAvailable(this)) {
            showErrorDialog("No internet connection. Please check your network and try again.");
            return;
        }
        Intent intent = new Intent(LoginActivity.this, SecureWebViewActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_up, R.anim.fade_out);
        finish();
    }

    // ===================== DIALOGS =====================

    private void showErrorDialog(String message) {
        new AlertDialog.Builder(this)
                .setTitle("Error")
                .setMessage(message)
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void showInfoDialog(String title, String message) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
