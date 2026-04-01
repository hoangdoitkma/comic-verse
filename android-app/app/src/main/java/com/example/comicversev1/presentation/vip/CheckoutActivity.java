package com.example.comicversev1.presentation.vip;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.comicversev1.databinding.ActivityCheckoutBinding;

public class CheckoutActivity extends AppCompatActivity {

    public static final String EXTRA_CHECKOUT_URL = "checkout_url";
    private ActivityCheckoutBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCheckoutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.toolbar.setNavigationOnClickListener(v -> finish());

        String checkoutUrl = getIntent().getStringExtra(EXTRA_CHECKOUT_URL);
        if (checkoutUrl == null || checkoutUrl.isEmpty()) {
            Toast.makeText(this, "Link thanh toán không hợp lệ", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setupWebView();
        binding.webView.loadUrl(checkoutUrl);
    }

    private void setupWebView() {
        binding.webView.getSettings().setJavaScriptEnabled(true);
        binding.webView.getSettings().setDomStorageEnabled(true);

        binding.webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                binding.progressBar.setVisibility(android.view.View.VISIBLE);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                binding.progressBar.setVisibility(android.view.View.GONE);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                String url = request.getUrl().toString();
                // Bắt sự kiện redirect về returnUrl hoặc cancelUrl
                if (url.contains("payment.success")) {
                    Toast.makeText(CheckoutActivity.this, "Giao dịch đang được xử lý thành công, vui lòng chờ hệ thống cập nhật VIP!", Toast.LENGTH_LONG).show();
                    setResult(RESULT_OK);
                    finish();
                    return true;
                } else if (url.contains("payment.cancel")) {
                    Toast.makeText(CheckoutActivity.this, "Đã hủy thanh toán", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_CANCELED);
                    finish();
                    return true;
                }
                return super.shouldOverrideUrlLoading(view, request);
            }
        });
    }
}
