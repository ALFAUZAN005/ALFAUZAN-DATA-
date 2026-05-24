package com.alfauzan.data.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Bundle;
import android.view.View;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.alfauzan.data.R;
import com.alfauzan.data.databinding.ActivitySecureWebviewBinding;
import com.alfauzan.data.helpers.Constants;
import com.alfauzan.data.helpers.NetworkHelper;
import com.alfauzan.data.helpers.SecurityHelper;
import com.alfauzan.data.helpers.SessionManager;
import com.alfauzan.data.services.AuthService;

public class SecureWebViewActivity extends AppCompatActivity {

    private ActivitySecureWebviewBinding binding;
    private SessionManager sessionManager;
    private AuthService authService;
    private boolean isPageLoaded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SecurityHelper.applySecureWindow(this);
        binding = ActivitySecureWebviewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sessionManager = SessionManager.getInstance(this);
        authService = new AuthService(this);

        setupToolbar();
        setupWebView();
        setupSwipeRefresh();
        setupRetryButton();
        loadDashboard();
    }

    private void setupToolbar() {
        binding.btnWebBack.setOnClickListener(v -> {
            if (binding.secureWebView.canGoBack()) {
                binding.secureWebView.goBack();
            }
        });

        binding.btnLogout.setOnClickListener(v -> showLogoutDialog());
    }

    private void setupWebView() {
        WebSettings webSettings = binding.secureWebView.getSettings();

        // Enable JavaScript safely
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setDatabaseEnabled(true);

        // Security hardening
        webSettings.setAllowFileAccess(false);
        webSettings.setAllowContentAccess(false);
        webSettings.setAllowFileAccessFromFileURLs(false);
        webSettings.setAllowUniversalAccessFromFileURLs(false);

        // Performance
        webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setBuiltInZoomControls(false);
        webSettings.setDisplayZoomControls(false);

        // Custom user agent
        String defaultUserAgent = webSettings.getUserAgentString();
        webSettings.setUserAgentString(defaultUserAgent + Constants.USER_AGENT_SUFFIX);

        // WebViewClient - intercepts navigation
        binding.secureWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                isPageLoaded = false;
                binding.webProgress.setVisibility(View.VISIBLE);
                binding.offlineLayout.setVisibility(View.GONE);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                isPageLoaded = true;
                binding.webProgress.setVisibility(View.GONE);
                binding.swipeRefresh.setRefreshing(false);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Uri uri = request.getUrl();
                String url = uri.toString();

                // Enforce HTTPS only
                if (!SecurityHelper.isHttpsUrl(url)) {
                    return true; // Block non-HTTPS
                }

                // Keep all alfauzandata.com.ng links internal
                if (url.contains("alfauzandata.com.ng")) {
                    view.loadUrl(url);
                    return true;
                }

                // Open external links in browser
                try {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(browserIntent);
                } catch (Exception e) {
                    // Ignore if no browser available
                }
                return true;
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request,
                                        WebResourceError error) {
                super.onReceivedError(view, request, error);
                if (request.isForMainFrame()) {
                    binding.webProgress.setVisibility(View.GONE);
                    binding.swipeRefresh.setRefreshing(false);
                    showOfflineUI();
                }
            }

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler,
                                           SslError error) {
                // Always cancel SSL errors — never proceed
                handler.cancel();
                showSslErrorDialog();
            }
        });

        // WebChromeClient - handles progress
        binding.secureWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                binding.webProgress.setProgress(newProgress);
                if (newProgress == 100) {
                    binding.webProgress.setVisibility(View.GONE);
                } else {
                    binding.webProgress.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onReceivedTitle(WebView view, String title) {
                super.onReceivedTitle(view, title);
                if (title != null && !title.isEmpty() && !title.startsWith("http")) {
                    binding.tvToolbarTitle.setText(title);
                } else {
                    binding.tvToolbarTitle.setText(R.string.app_name);
                }
            }
        });
    }

    private void setupSwipeRefresh() {
        binding.swipeRefresh.setColorSchemeColors(
                getResources().getColor(R.color.royalBlue),
                getResources().getColor(R.color.royalBlueBright),
                getResources().getColor(R.color.neonBlue)
        );

        binding.swipeRefresh.setOnRefreshListener(() -> {
            if (NetworkHelper.isNetworkAvailable(this)) {
                binding.offlineLayout.setVisibility(View.GONE);
                binding.secureWebView.reload();
            } else {
                binding.swipeRefresh.setRefreshing(false);
                showOfflineUI();
            }
        });
    }

    private void setupRetryButton() {
        binding.btnRetry.setOnClickListener(v -> {
            if (NetworkHelper.isNetworkAvailable(this)) {
                binding.offlineLayout.setVisibility(View.GONE);
                loadDashboard();
            } else {
                showOfflineUI();
            }
        });
    }

    private void loadDashboard() {
        if (NetworkHelper.isNetworkAvailable(this)) {
            binding.offlineLayout.setVisibility(View.GONE);
            binding.secureWebView.loadUrl(Constants.DASHBOARD_URL);
        } else {
            showOfflineUI();
        }
    }

    private void showOfflineUI() {
        binding.offlineLayout.setVisibility(View.VISIBLE);
        binding.swipeRefresh.setRefreshing(false);
    }

    private void showSslErrorDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Security Warning")
                .setMessage("SSL certificate error. Connection is not secure. Please try again later.")
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.app_name)
                .setMessage(R.string.logout_confirm)
                .setPositiveButton(R.string.btn_logout, (dialog, which) -> {
                    dialog.dismiss();
                    performLogout();
                })
                .setNegativeButton(R.string.btn_cancel, (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void performLogout() {
        // Clear WebView data
        binding.secureWebView.clearHistory();
        binding.secureWebView.clearCache(true);
        binding.secureWebView.clearFormData();

        authService.logout();

        Intent intent = new Intent(SecureWebViewActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        finish();
    }

    @Override
    public void onBackPressed() {
        if (binding.secureWebView.canGoBack()) {
            binding.secureWebView.goBack();
        } else {
            // Do nothing — prevent accidental back to login
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        binding.secureWebView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        binding.secureWebView.onResume();
    }

    @Override
    protected void onDestroy() {
        if (binding != null && binding.secureWebView != null) {
            binding.secureWebView.stopLoading();
            binding.secureWebView.destroy();
        }
        super.onDestroy();
        binding = null;
    }
}
