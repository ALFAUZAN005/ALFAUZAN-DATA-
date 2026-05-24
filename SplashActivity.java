package com.alfauzan.data.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.appcompat.app.AppCompatActivity;

import com.alfauzan.data.R;
import com.alfauzan.data.databinding.ActivitySplashBinding;
import com.alfauzan.data.helpers.Constants;
import com.alfauzan.data.helpers.SecurityHelper;

public class SplashActivity extends AppCompatActivity {

    private ActivitySplashBinding binding;
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SecurityHelper.applySecureWindow(this);
        binding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        handler = new Handler(Looper.getMainLooper());
        startSplashAnimations();
    }

    private void startSplashAnimations() {
        // Animate logo with scale + fade
        Animation scaleIn = AnimationUtils.loadAnimation(this, R.anim.scale_in);
        scaleIn.setDuration(700);
        binding.splashContent.startAnimation(scaleIn);
        binding.splashContent.setAlpha(1f);

        // Animate loading layout after delay
        handler.postDelayed(() -> {
            Animation fadeIn = AnimationUtils.loadAnimation(SplashActivity.this, R.anim.fade_in);
            binding.loadingLayout.startAnimation(fadeIn);
            binding.loadingLayout.setAlpha(1f);
        }, 800);

        // Navigate to LoginActivity after splash delay
        handler.postDelayed(this::navigateToLogin, Constants.SPLASH_DELAY_MS);
    }

    private void navigateToLogin() {
        Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
        binding = null;
    }
}
