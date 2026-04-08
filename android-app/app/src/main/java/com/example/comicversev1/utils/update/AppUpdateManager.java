package com.example.comicversev1.utils.update;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.example.comicversev1.data.model.AppUpdateInfo;
import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class AppUpdateManager {

    private static final String TAG = "AppUpdateManager";
    // URL bucket S3 thực tế lấy từ cấu hình AWS
    private static final String UPDATE_URL = "https://comicverse-storage.s3.ap-southeast-1.amazonaws.com/android-updates/version.json";

    private final Context context;
    private final OkHttpClient client;
    private long downloadId = -1;
    private String downloadedApkName = "comicverse_update.apk";

    public interface CheckUpdateCallback {
        void onUpdateAvailable(AppUpdateInfo updateInfo);
        void onNoUpdate();
        void onError(String message);
    }

    public AppUpdateManager(Context context) {
        this.context = context;
        this.client = new OkHttpClient();
    }

    public void checkForUpdate(CheckUpdateCallback callback) {
        Request request = new Request.Builder()
                .url(UPDATE_URL)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (callback != null) {
                    callback.onError(e.getMessage());
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String json = response.body().string();
                        Gson gson = new Gson();
                        AppUpdateInfo updateInfo = gson.fromJson(json, AppUpdateInfo.class);

                        int currentVersionCode = getCurrentVersionCode();
                        if (updateInfo.getVersionCode() > currentVersionCode) {
                            if (callback != null) {
                                callback.onUpdateAvailable(updateInfo);
                            }
                        } else {
                            if (callback != null) {
                                callback.onNoUpdate();
                            }
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Parse Json failed", e);
                        if (callback != null) {
                            callback.onError("Parse config failed: " + e.getMessage());
                        }
                    }
                } else {
                    if (callback != null) {
                        callback.onError("Server returns error: " + response.code());
                    }
                }
            }
        });
    }

    private int getCurrentVersionCode() {
        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return pInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public void startDownload(AppUpdateInfo updateInfo) {
        try {
            downloadedApkName = updateInfo.getApkName() != null && !updateInfo.getApkName().isEmpty() 
                    ? updateInfo.getApkName() : "comicverse_update.apk";

            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(updateInfo.getDownloadUrl()));
            request.setTitle("Cập nhật ComicVerse " + updateInfo.getVersionName());
            request.setDescription("Đang tải xuống bản cập nhật...");
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

            // Lưu vào thư mục Files/Download của App để tránh bị Permission Denied
            File destinationPath = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), downloadedApkName);
            if (destinationPath.exists()) {
                destinationPath.delete();
            }

            request.setDestinationUri(Uri.fromFile(destinationPath));

            DownloadManager manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
            downloadId = manager.enqueue(request);

            // Đăng ký Receiver
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.registerReceiver(onDownloadComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE), Context.RECEIVER_EXPORTED);
            } else {
                context.registerReceiver(onDownloadComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
            }

            Toast.makeText(context, "Đang tải bản cập nhật trong nền...", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            Log.e(TAG, "Error starting download", e);
            Toast.makeText(context, "Lỗi khi tải bản cập nhật!", Toast.LENGTH_SHORT).show();
        }
    }

    private final BroadcastReceiver onDownloadComplete = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
            if (downloadId == id) {
                try {
                    context.unregisterReceiver(this);
                } catch (Exception ignored) {}

                installApk();
            }
        }
    };

    private void installApk() {
        File file = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), downloadedApkName);
        if (!file.exists()) {
            Toast.makeText(context, "Không tìm thấy file tải về!", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            Uri contentUri = FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", file);

            Intent installIntent = new Intent(Intent.ACTION_VIEW);
            installIntent.setDataAndType(contentUri, "application/vnd.android.package-archive");
            installIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            installIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            context.startActivity(installIntent);
        } catch (Exception e) {
            Log.e(TAG, "Install failed", e);
            Toast.makeText(context, "Lỗi mở bộ cài đặt: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
