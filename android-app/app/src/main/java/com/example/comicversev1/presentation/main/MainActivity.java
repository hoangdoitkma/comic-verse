package com.example.comicversev1.presentation.main;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.example.comicversev1.R;
import com.example.comicversev1.data.model.AppUpdateInfo;
import com.example.comicversev1.presentation.dialog.UpdateDialog;
import com.example.comicversev1.utils.update.AppUpdateManager;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity {

    private AppUpdateManager appUpdateManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        if (navHostFragment != null) {
            NavController navController = navHostFragment.getNavController();
            NavigationUI.setupActionBarWithNavController(this, navController);
        }
        
        checkForAppUpdates();
    }

    private void checkForAppUpdates() {
        appUpdateManager = new AppUpdateManager(this);
        appUpdateManager.checkForUpdate(new AppUpdateManager.CheckUpdateCallback() {
            @Override
            public void onUpdateAvailable(AppUpdateInfo updateInfo) {
                runOnUiThread(() -> {
                    UpdateDialog dialog = UpdateDialog.newInstance(updateInfo, () -> {
                        appUpdateManager.startDownload(updateInfo);
                    });
                    dialog.show(getSupportFragmentManager(), "UpdateDialog");
                });
            }

            @Override
            public void onNoUpdate() {
                // Đã là phiên bản mới nhất, không làm gì cả
            }

            @Override
            public void onError(String message) {
                android.util.Log.e("AppUpdateCheck", "Failed to check update: " + message);
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        if (navHostFragment != null) {
            return navHostFragment.getNavController().navigateUp() || super.onSupportNavigateUp();
        }
        return super.onSupportNavigateUp();
    }
}
