package com.example.comicversev1.presentation.profile;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.bumptech.glide.Glide;
import com.example.comicversev1.BuildConfig;
import com.example.comicversev1.R;
import com.example.comicversev1.data.api.ApiService;
import com.example.comicversev1.data.local.dao.ReadingHistoryDao;
import com.example.comicversev1.data.model.UserProfileDTO;
import com.example.comicversev1.databinding.FragmentProfileBinding;
import com.example.comicversev1.utils.Constants;
import com.example.comicversev1.utils.update.AppUpdateManager;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

@AndroidEntryPoint
public class ProfileFragment extends Fragment {

    @Inject
    ApiService apiService;

    @Inject
    ReadingHistoryDao readingHistoryDao;

    private CompositeDisposable disposable = new CompositeDisposable();
    private FragmentProfileBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupBottomNav();
        setupView();
        setupThemeMode();
    }

    private void setupThemeMode() {
        SharedPreferences settingsPrefs = requireActivity().getSharedPreferences("SettingsPrefs", Context.MODE_PRIVATE);
        int themeMode = settingsPrefs.getInt("theme_mode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        AppCompatDelegate.setDefaultNightMode(themeMode);
    }

    private void setupView() {
        // Setup Version
        binding.textVersion.setText("Phiên bản: " + BuildConfig.VERSION_NAME);

        // Feature: Reader Settings
        binding.rowReaderSettings.textTitle.setText("Cài đặt trình đọc");
        binding.rowReaderSettings.icon.setImageResource(android.R.drawable.ic_menu_preferences);

        // Feature: Sync Data
        binding.rowReadingHistory.textTitle.setText("Đồng bộ dữ liệu");
        binding.rowReadingHistory.icon.setImageResource(android.R.drawable.ic_popup_sync);
        binding.rowReadingHistory.getRoot().setOnClickListener(v -> syncData());

        // Feature: Download List
        binding.rowDownloadList.textTitle.setText("Danh sách tải");
        binding.rowDownloadList.icon.setImageResource(android.R.drawable.ic_menu_save);

        // Feature: Clear Cache
        calculateCacheSize();
        binding.rowClearCache.icon.setImageResource(android.R.drawable.ic_menu_delete);
        binding.rowClearCache.getRoot().setOnClickListener(v -> clearGlideCache());

        // Feature: Check Update
        binding.rowCheckUpdate.textTitle.setText("Kiểm tra cập nhật phần mềm");
        binding.rowCheckUpdate.textSubtitle.setText("Phiên bản hiện tại: " + BuildConfig.VERSION_NAME);
        binding.rowCheckUpdate.textSubtitle.setVisibility(View.VISIBLE);
        if (binding.rowCheckUpdate.icon.getDrawable() == null) {
            binding.rowCheckUpdate.icon.setImageResource(android.R.drawable.ic_popup_sync);
        }
        binding.rowCheckUpdate.getRoot().setOnClickListener(v -> checkAppUpdateManually());

        // Contact & Support
        binding.rowEmail.textTitle.setText("Gửi mail đến hỗ trợ");
        binding.rowEmail.icon.setImageResource(android.R.drawable.ic_dialog_email);
        binding.rowEmail.getRoot().setOnClickListener(v -> openEmail("hoangdoitkma@gmail.com"));

        binding.rowFacebook.textTitle.setText("Fanpage Facebook");
        binding.rowFacebook.icon.setImageResource(android.R.drawable.ic_menu_share);
        binding.rowFacebook.getRoot().setOnClickListener(v -> openUrl("https://facebook.com/duchoang3m"));

        binding.rowWebsite.textTitle.setText("Trang web chính thức");
        binding.rowWebsite.icon.setImageResource(android.R.drawable.ic_menu_info_details);
        binding.rowWebsite.getRoot().setOnClickListener(v -> openUrl("https://facebook.com/duchoang3m"));

        // Feature: Theme setup
        binding.rowTheme.textTitle.setText("Giao diện ứng dụng");
        binding.rowTheme.textSubtitle.setText("Sáng, tối, theo hệ thống");
        binding.rowTheme.textSubtitle.setVisibility(View.VISIBLE);
        binding.rowTheme.icon.setImageResource(android.R.drawable.ic_menu_view);
        binding.rowTheme.getRoot().setOnClickListener(v -> showThemePicker());

        // Feature: Notification setup
        SharedPreferences settingsPrefs = requireActivity().getSharedPreferences("SettingsPrefs", Context.MODE_PRIVATE);
        boolean notifEnabled = settingsPrefs.getBoolean("notif_enabled", true);
        binding.rowNotification.textTitle.setText("Nhận thông báo");
        binding.rowNotification.icon.setImageResource(android.R.drawable.ic_popup_reminder);
        binding.rowNotification.switchToggle.setVisibility(View.VISIBLE);
        binding.rowNotification.switchToggle.setChecked(notifEnabled);
        binding.rowNotification.switchToggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
            settingsPrefs.edit().putBoolean("notif_enabled", isChecked).apply();
            Toast.makeText(requireContext(), isChecked ? "Đã bật thông báo" : "Đã tắt thông báo", Toast.LENGTH_SHORT).show();
        });
        binding.rowNotification.getRoot().setOnClickListener(v -> binding.rowNotification.switchToggle.toggle());

        // Profile / Login logic
        SharedPreferences prefs = requireActivity().getSharedPreferences(Constants.PREF_AUTH, Context.MODE_PRIVATE);
        String token = prefs.getString(Constants.KEY_ACCESS_TOKEN, "");
        String name = prefs.getString(Constants.KEY_DISPLAY_NAME, "Người dùng ẩn danh");

        if (!token.isEmpty()) {
            binding.textProfileName.setText(name);
            binding.cardProfile.setOnClickListener(v -> {
                NavHostFragment.findNavController(this).navigate(R.id.profileDetailFragment);
            });
            fetchUserProfile();
        } else {
            binding.textProfileName.setText("Đăng nhập hoặc đăng ký");
            binding.textVipStatus.setVisibility(View.GONE);
            binding.cardProfile.setOnClickListener(v -> {
                NavHostFragment.findNavController(this).navigate(R.id.loginFragment);
            });
        }
    }

    private void fetchUserProfile() {
        disposable.add(
                apiService.getUserProfile()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(response -> {
                            if (response.isSuccess() && response.getData() != null) {
                                UserProfileDTO profile = response.getData();
                                binding.textProfileName.setText(profile.displayName);
                                if (profile.avatarUrl != null && !profile.avatarUrl.isEmpty()) {
                                    Glide.with(this).load(profile.avatarUrl)
                                         .placeholder(R.mipmap.ic_launcher)
                                         .into(binding.imageAvatar);
                                }
                                if (profile.vip) {
                                    binding.textVipStatus.setVisibility(View.VISIBLE);
                                    binding.textVipStatus.setTextColor(android.graphics.Color.parseColor("#FFD700"));
                                    if (profile.vipEndDate != null && !profile.vipEndDate.isEmpty()) {
                                        try {
                                            LocalDateTime endDate = LocalDateTime.parse(profile.vipEndDate);
                                            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
                                            binding.textVipStatus.setText("Thành viên VIP - Hạn sử dụng: " + endDate.format(formatter));
                                        } catch (Exception e) {
                                            binding.textVipStatus.setText("Thành viên VIP");
                                        }
                                    } else {
                                        binding.textVipStatus.setText("Thành viên VIP Trọn đời");
                                    }
                                } else {
                                    binding.textVipStatus.setVisibility(View.VISIBLE);
                                    binding.textVipStatus.setTextColor(android.graphics.Color.parseColor("#B3B3B3"));
                                    binding.textVipStatus.setText("Thành viên Thường");
                                }
                            }
                        }, error -> {
                            Log.e("ProfileFragment", "Error fetching profile", error);
                            binding.textVipStatus.setVisibility(View.VISIBLE);
                            binding.textVipStatus.setTextColor(android.graphics.Color.parseColor("#B3B3B3"));
                            binding.textVipStatus.setText("Thành viên Thường");
                        }));
    }

    private void showThemePicker() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext());
        View view = getLayoutInflater().inflate(R.layout.bottom_sheet_theme, null);
        RadioGroup radioGroupTheme = view.findViewById(R.id.radioGroupTheme);
        RadioButton radioLight = view.findViewById(R.id.radioLight);
        RadioButton radioDark = view.findViewById(R.id.radioDark);
        RadioButton radioSystem = view.findViewById(R.id.radioSystem);

        SharedPreferences settingsPrefs = requireActivity().getSharedPreferences("SettingsPrefs", Context.MODE_PRIVATE);
        int themeMode = settingsPrefs.getInt("theme_mode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);

        if (themeMode == AppCompatDelegate.MODE_NIGHT_NO) radioLight.setChecked(true);
        else if (themeMode == AppCompatDelegate.MODE_NIGHT_YES) radioDark.setChecked(true);
        else radioSystem.setChecked(true);

        radioGroupTheme.setOnCheckedChangeListener((group, checkedId) -> {
            int newMode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
            if (checkedId == R.id.radioLight) newMode = AppCompatDelegate.MODE_NIGHT_NO;
            else if (checkedId == R.id.radioDark) newMode = AppCompatDelegate.MODE_NIGHT_YES;

            settingsPrefs.edit().putInt("theme_mode", newMode).apply();
            AppCompatDelegate.setDefaultNightMode(newMode);
            bottomSheetDialog.dismiss();
        });

        bottomSheetDialog.setContentView(view);
        bottomSheetDialog.show();
    }

    private void setupBottomNav() {
        binding.bottomNavigation.setSelectedItemId(R.id.menu_more);
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.menu_home) {
                NavHostFragment.findNavController(this).navigate(R.id.homeFragment);
                return true;
            }
            if (item.getItemId() == R.id.menu_novel) {
                NavHostFragment.findNavController(this).navigate(R.id.novelFragment);
                return true;
            }
            if (item.getItemId() == R.id.menu_favorite) {
                NavHostFragment.findNavController(this).navigate(R.id.favoriteFragment);
                return true;
            }
            if (item.getItemId() == R.id.menu_more) {
                return true;
            }
            return true;
        });
    }

    private void openUrl(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        try {
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Không tìm thấy trình duyệt", Toast.LENGTH_SHORT).show();
        }
    }

    private void openEmail(String email) {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:" + email));
        try {
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Không tìm thấy ứng dụng email", Toast.LENGTH_SHORT).show();
        }
    }

    private void calculateCacheSize() {
        new Thread(() -> {
            long size = 0;
            File cacheDir = Glide.getPhotoCacheDir(requireContext());
            if (cacheDir != null && cacheDir.exists()) {
                size = getFolderSize(cacheDir);
            }
            long finalSize = size;
            requireActivity().runOnUiThread(() -> {
                double sizeMb = (double) finalSize / (1024 * 1024);
                String formattedSize = String.format("%.1f", sizeMb);
                binding.rowClearCache.textTitle.setText("Xóa bộ nhớ cache ảnh (" + formattedSize + " MB)");
            });
        }).start();
    }

    private void clearGlideCache() {
        Toast.makeText(requireContext(), "Đang xóa bộ nhớ cache...", Toast.LENGTH_SHORT).show();
        Glide.get(requireContext()).clearMemory();
        new Thread(() -> {
            Glide.get(requireContext()).clearDiskCache();
            requireActivity().runOnUiThread(() -> {
                Toast.makeText(requireContext(), "Đã xóa bộ nhớ cache", Toast.LENGTH_SHORT).show();
                binding.rowClearCache.textTitle.setText("Xóa bộ nhớ cache ảnh (0.0 MB)");
            });
        }).start();
    }

    private long getFolderSize(File file) {
        long size = 0;
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File child : files) {
                    size += getFolderSize(child);
                }
            }
        } else {
            size = file.length();
        }
        return size;
    }

    private void checkAppUpdateManually() {
        Toast.makeText(requireContext(), "Đang kiểm tra...", Toast.LENGTH_SHORT).show();
        AppUpdateManager appUpdateManager = new AppUpdateManager(requireContext());
        appUpdateManager.checkForUpdate(new AppUpdateManager.CheckUpdateCallback() {
            @Override
            public void onUpdateAvailable(com.example.comicversev1.data.model.AppUpdateInfo updateInfo) {
                if (getActivity() == null || getActivity().isFinishing() || getActivity().isDestroyed()) return;
                getActivity().runOnUiThread(() -> {
                    try {
                        com.example.comicversev1.presentation.dialog.UpdateDialog dialog = com.example.comicversev1.presentation.dialog.UpdateDialog.newInstance(updateInfo, () -> {
                            appUpdateManager.startDownload(updateInfo);
                        });
                        dialog.show(getParentFragmentManager(), "UpdateDialog");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }

            @Override
            public void onNoUpdate() {
                if (getActivity() == null) return;
                getActivity().runOnUiThread(() -> {
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Bạn đang sử dụng phiên bản mới nhất!", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onError(String message) {
                if (getActivity() == null) return;
                getActivity().runOnUiThread(() -> {
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Lỗi kiểm tra cập nhật!", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void syncData() {
        SharedPreferences prefs = requireActivity().getSharedPreferences(Constants.PREF_AUTH, Context.MODE_PRIVATE);
        String token = prefs.getString(Constants.KEY_ACCESS_TOKEN, "");
        if (token.isEmpty()) {
            Toast.makeText(requireContext(), "Vui lòng đăng nhập để đồng bộ", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(requireContext(), "Đang đồng bộ dữ liệu...", Toast.LENGTH_SHORT).show();
        disposable.add(
                readingHistoryDao.getAllHistory()
                        .subscribeOn(Schedulers.io())
                        .flatMapCompletable(historyList -> {
                            if (historyList == null || historyList.isEmpty()) {
                                return Completable.complete();
                            }
                            return Observable.fromIterable(historyList)
                                    .map(entity -> new com.example.comicversev1.data.model.ReadingHistoryRequest(
                                            entity.comicId, entity.chapterId, entity.pageIndex))
                                    .toList()
                                    .flatMapCompletable(requests -> apiService.syncReadingHistory(requests));
                        })
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(() -> {
                            Toast.makeText(requireContext(), "Đồng bộ thành công!", Toast.LENGTH_SHORT).show();
                        }, error -> {
                            Log.e("ProfileFragment", "Sync error", error);
                            Toast.makeText(requireContext(), "Đồng bộ thất bại!", Toast.LENGTH_SHORT).show();
                        })
        );
    }
}
