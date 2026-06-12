package com.example.comicversev1.presentation.vip;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.comicversev1.data.repository.VipRepository;
import com.example.comicversev1.databinding.ActivityCheckoutBinding;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

@AndroidEntryPoint
public class CheckoutActivity extends AppCompatActivity {

    public static final String EXTRA_CHECKOUT_URL = "checkout_url";
    public static final String EXTRA_ORDER_CODE = "order_code";
    private static final int MAX_CONFIRM_RETRIES = 5;
    private static final long CONFIRM_RETRY_DELAY_MS = 2000L;

    @Inject
    VipRepository vipRepository;

    private final CompositeDisposable disposable = new CompositeDisposable();
    private final Handler retryHandler = new Handler(Looper.getMainLooper());
    private ActivityCheckoutBinding binding;
    private Long orderCode;
    private boolean paymentHandled;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCheckoutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.toolbar.setNavigationOnClickListener(v -> {
            setResult(RESULT_CANCELED);
            finish();
        });

        String checkoutUrl = getIntent().getStringExtra(EXTRA_CHECKOUT_URL);
        long rawOrderCode = getIntent().getLongExtra(EXTRA_ORDER_CODE, -1L);
        if (rawOrderCode > 0) {
            orderCode = rawOrderCode;
        }

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
                binding.progressBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                if (!paymentHandled) {
                    binding.progressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                String url = request.getUrl().toString();
                return handleRedirectUrl(url) || super.shouldOverrideUrlLoading(view, request);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return handleRedirectUrl(url) || super.shouldOverrideUrlLoading(view, url);
            }
        });
    }

    private boolean handleRedirectUrl(String url) {
        if (url == null) {
            return false;
        }

        if (url.contains("payment.success")) {
            handlePaymentSuccess();
            return true;
        }

        if (url.contains("payment.cancel")) {
            Toast.makeText(this, "Đã hủy thanh toán", Toast.LENGTH_SHORT).show();
            setResult(RESULT_CANCELED);
            finish();
            return true;
        }

        return false;
    }

    private void handlePaymentSuccess() {
        if (paymentHandled) {
            return;
        }
        paymentHandled = true;
        binding.progressBar.setVisibility(View.VISIBLE);

        if (orderCode == null) {
            Toast.makeText(this, "Thanh toán thành công, hệ thống đang cập nhật VIP", Toast.LENGTH_LONG).show();
            setResult(RESULT_OK);
            finish();
            return;
        }

        Toast.makeText(this, "Đang xác nhận thanh toán...", Toast.LENGTH_SHORT).show();
        confirmVipOrder(0);
    }

    private void confirmVipOrder(int attempt) {
        disposable.add(
                vipRepository.confirmVipOrder(orderCode)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(response -> {
                            if (response.isSuccess() && response.isVipActivated()) {
                                Toast.makeText(this, "Thanh toán thành công. VIP đã được kích hoạt!", Toast.LENGTH_LONG).show();
                                setResult(RESULT_OK);
                                finish();
                                return;
                            }

                            if (attempt < MAX_CONFIRM_RETRIES && shouldRetryConfirmation(response.getStatus())) {
                                retryHandler.postDelayed(() -> confirmVipOrder(attempt + 1), CONFIRM_RETRY_DELAY_MS);
                                return;
                            }

                            String message = response.getMessage();
                            if (message == null || message.isEmpty()) {
                                message = "Thanh toán thành công, hệ thống đang cập nhật VIP";
                            }
                            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                            setResult(RESULT_OK);
                            finish();
                        }, error -> {
                            if (attempt < MAX_CONFIRM_RETRIES) {
                                retryHandler.postDelayed(() -> confirmVipOrder(attempt + 1), CONFIRM_RETRY_DELAY_MS);
                                return;
                            }

                            Toast.makeText(this, "Thanh toán thành công, hệ thống đang cập nhật VIP", Toast.LENGTH_LONG).show();
                            setResult(RESULT_OK);
                            finish();
                        })
        );
    }

    private boolean shouldRetryConfirmation(String status) {
        return status == null
                || status.isEmpty()
                || "PENDING".equalsIgnoreCase(status);
    }

    @Override
    protected void onDestroy() {
        retryHandler.removeCallbacksAndMessages(null);
        disposable.clear();
        super.onDestroy();
    }
}
