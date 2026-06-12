package com.example.comicversev1.presentation.profile;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.bumptech.glide.Glide;
import com.example.comicversev1.BuildConfig;
import com.example.comicversev1.R;
import com.example.comicversev1.data.model.UserProfileDTO;
import com.example.comicversev1.data.repository.UserDataRepository;
import com.example.comicversev1.data.repository.UserProfileRepository;
import com.example.comicversev1.databinding.FragmentProfileBinding;
import com.example.comicversev1.utils.Constants;
import com.example.comicversev1.utils.ReaderSettings;
import com.example.comicversev1.utils.update.AppUpdateManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

@AndroidEntryPoint
public class ProfileFragment extends Fragment {

    private static final String TAG = "ProfileFragment";
    private static final String SETTINGS_PREFS = "SettingsPrefs";
    private static final String KEY_NOTIFICATIONS_ENABLED = "notif_enabled";
    private static final String SUPPORT_EMAIL = "comicfan.official@gmail.com";

    @Inject
    UserProfileRepository userProfileRepository;

    @Inject
    UserDataRepository userDataRepository;

    private final CompositeDisposable disposable = new CompositeDisposable();
    private FragmentProfileBinding binding;
    private ActivityResultLauncher<String> notificationPermissionLauncher;
    private boolean suppressNotificationSwitchChange = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        notificationPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (getActivity() == null) return;
                    SharedPreferences settingsPrefs = requireActivity().getSharedPreferences(SETTINGS_PREFS, Context.MODE_PRIVATE);
                    settingsPrefs.edit().putBoolean(KEY_NOTIFICATIONS_ENABLED, isGranted).apply();
                    setNotificationSwitchChecked(isGranted);
                    updateNotificationSubtitle(isGranted);
                    Toast.makeText(
                            requireContext(),
                            isGranted ? "Đã bật thông báo" : "Chưa cấp quyền thông báo",
                            Toast.LENGTH_SHORT
                    ).show();
                });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
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
        SharedPreferences settingsPrefs = requireActivity().getSharedPreferences(SETTINGS_PREFS, Context.MODE_PRIVATE);
        int themeMode = settingsPrefs.getInt("theme_mode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        AppCompatDelegate.setDefaultNightMode(themeMode);
    }

    private void setupView() {
        binding.btnBack.setOnClickListener(v -> navigateBackFromProfile());
        binding.textVersion.setText("Phiên bản: " + BuildConfig.VERSION_NAME);

        binding.rowReaderSettings.textTitle.setText("Cài đặt trình đọc");
        binding.rowReaderSettings.icon.setImageResource(android.R.drawable.ic_menu_preferences);

        binding.rowReadingHistory.textTitle.setText("Đồng bộ dữ liệu");
        binding.rowReadingHistory.icon.setImageResource(android.R.drawable.ic_popup_sync);
        binding.rowReadingHistory.getRoot().setOnClickListener(v -> syncData());

        binding.rowDownloadList.textTitle.setText("Danh sách tải");
        binding.rowDownloadList.icon.setImageResource(android.R.drawable.ic_menu_save);

        calculateCacheSize();
        binding.rowClearCache.icon.setImageResource(android.R.drawable.ic_menu_delete);
        binding.rowClearCache.getRoot().setOnClickListener(v -> clearGlideCache());

        binding.rowCheckUpdate.textTitle.setText("Kiểm tra cập nhật phần mềm");
        binding.rowCheckUpdate.textSubtitle.setText("Phiên bản hiện tại: " + BuildConfig.VERSION_NAME);
        binding.rowCheckUpdate.textSubtitle.setVisibility(View.VISIBLE);
        if (binding.rowCheckUpdate.icon.getDrawable() == null) {
            binding.rowCheckUpdate.icon.setImageResource(android.R.drawable.ic_popup_sync);
        }
        binding.rowCheckUpdate.getRoot().setOnClickListener(v -> checkAppUpdateManually());

        binding.rowEmail.textTitle.setText("Gửi mail đến hỗ trợ");
        binding.rowEmail.icon.setImageResource(android.R.drawable.ic_dialog_email);
        binding.rowEmail.getRoot().setOnClickListener(v -> openEmail("hoangdoitkma@gmail.com"));

        binding.rowFacebook.textTitle.setText("Fanpage Facebook");
        binding.rowFacebook.icon.setImageResource(android.R.drawable.ic_menu_share);
        binding.rowFacebook.getRoot().setOnClickListener(v -> openUrl("https://facebook.com/duchoang3m"));

        binding.rowWebsite.textTitle.setText("Trang web chính thức");
        binding.rowWebsite.icon.setImageResource(android.R.drawable.ic_menu_info_details);
        binding.rowWebsite.getRoot().setOnClickListener(v -> openUrl("https://facebook.com/duchoang3m"));

        binding.rowTheme.textTitle.setText("Giao diện ứng dụng");
        binding.rowTheme.textSubtitle.setText("Sáng, tối, theo hệ thống");
        binding.rowTheme.textSubtitle.setVisibility(View.VISIBLE);
        binding.rowTheme.icon.setImageResource(android.R.drawable.ic_menu_view);
        binding.rowTheme.getRoot().setOnClickListener(v -> showThemePicker());

        SharedPreferences settingsPrefs = requireActivity().getSharedPreferences(SETTINGS_PREFS, Context.MODE_PRIVATE);
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
        applySettingsRowFixups(settingsPrefs);

        SharedPreferences prefs = requireActivity().getSharedPreferences(Constants.PREF_AUTH, Context.MODE_PRIVATE);
        String token = prefs.getString(Constants.KEY_ACCESS_TOKEN, "");
        String name = prefs.getString(Constants.KEY_DISPLAY_NAME, "Người dùng ẩn danh");

        if (!token.isEmpty()) {
            binding.textProfileName.setText(name);
            binding.cardProfile.setOnClickListener(v ->
                    NavHostFragment.findNavController(this).navigate(R.id.profileDetailFragment));
            fetchUserProfile();
        } else {
            binding.textProfileName.setText("Đăng nhập hoặc đăng ký");
            binding.textVipStatus.setVisibility(View.GONE);
            binding.cardProfile.setOnClickListener(v ->
                    NavHostFragment.findNavController(this).navigate(R.id.loginFragment));
        }
    }

    private void fetchUserProfile() {
        disposable.add(userProfileRepository.getUserProfile()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::renderUserProfile, error -> {
                    Log.e(TAG, "Error fetching profile", error);
                    binding.textVipStatus.setVisibility(View.VISIBLE);
                    binding.textVipStatus.setTextColor(android.graphics.Color.parseColor("#B3B3B3"));
                    binding.textVipStatus.setText("Thành viên Thường");
                }));
    }

    private void renderUserProfile(UserProfileDTO profile) {
        binding.textProfileName.setText(profile.displayName);
        SharedPreferences prefs = requireActivity().getSharedPreferences(Constants.PREF_AUTH, Context.MODE_PRIVATE);
        if (profile.displayName != null && !profile.displayName.trim().isEmpty()) {
            prefs.edit().putString(Constants.KEY_DISPLAY_NAME, profile.displayName.trim()).apply();
        }
        if (profile.avatarUrl != null && !profile.avatarUrl.isEmpty()) {
            prefs.edit().putString(Constants.KEY_AVATAR_URL, profile.avatarUrl).apply();
            Glide.with(this)
                    .load(profile.avatarUrl)
                    .placeholder(R.mipmap.ic_launcher)
                    .into(binding.imageAvatar);
        }

        binding.textVipStatus.setVisibility(View.VISIBLE);
        if (profile.vip) {
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
            binding.textVipStatus.setTextColor(android.graphics.Color.parseColor("#B3B3B3"));
            binding.textVipStatus.setText("Thành viên Thường");
        }
    }

    private void showThemePicker() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext());
        View view = getLayoutInflater().inflate(R.layout.bottom_sheet_theme, null);
        RadioGroup radioGroupTheme = view.findViewById(R.id.radioGroupTheme);
        RadioButton radioLight = view.findViewById(R.id.radioLight);
        RadioButton radioDark = view.findViewById(R.id.radioDark);
        RadioButton radioSystem = view.findViewById(R.id.radioSystem);

        SharedPreferences settingsPrefs = requireActivity().getSharedPreferences(SETTINGS_PREFS, Context.MODE_PRIVATE);
        int themeMode = settingsPrefs.getInt("theme_mode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);

        if (themeMode == AppCompatDelegate.MODE_NIGHT_NO) {
            radioLight.setChecked(true);
        } else if (themeMode == AppCompatDelegate.MODE_NIGHT_YES) {
            radioDark.setChecked(true);
        } else {
            radioSystem.setChecked(true);
        }

        radioGroupTheme.setOnCheckedChangeListener((group, checkedId) -> {
            int newMode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
            if (checkedId == R.id.radioLight) {
                newMode = AppCompatDelegate.MODE_NIGHT_NO;
            } else if (checkedId == R.id.radioDark) {
                newMode = AppCompatDelegate.MODE_NIGHT_YES;
            }

            settingsPrefs.edit().putInt("theme_mode", newMode).apply();
            AppCompatDelegate.setDefaultNightMode(newMode);
            bottomSheetDialog.dismiss();
        });

        bottomSheetDialog.setContentView(view);
        bottomSheetDialog.show();
    }

    private void applySettingsRowFixups(SharedPreferences settingsPrefs) {
        binding.textVersion.setText("Phiên bản: " + BuildConfig.VERSION_NAME);

        binding.rowReaderSettings.textTitle.setText("Cài đặt trình đọc");
        updateReaderSettingsSubtitle();
        binding.rowReaderSettings.textSubtitle.setVisibility(View.VISIBLE);
        binding.rowReaderSettings.getRoot().setOnClickListener(v -> showReaderSettingsBottomSheet());

        binding.rowReadingHistory.textTitle.setText("Đồng bộ lịch sử và yêu thích");
        binding.rowReadingHistory.textSubtitle.setText("Đẩy dữ liệu local lên máy chủ");
        binding.rowReadingHistory.textSubtitle.setVisibility(View.VISIBLE);

        binding.rowDownloadList.textTitle.setText("Danh sách tải");
        binding.rowDownloadList.textSubtitle.setText("Đọc offline - đang phát triển");
        binding.rowDownloadList.textSubtitle.setVisibility(View.VISIBLE);
        binding.rowDownloadList.getRoot().setOnClickListener(v -> showFeatureInProgress(
                "Danh sách tải",
                "Tính năng tải truyện để đọc offline chưa được kích hoạt. Cần chốt phạm vi lưu file, xóa file và quyền đọc offline trước khi triển khai."
        ));

        binding.rowClearCache.textSubtitle.setText("Chỉ xóa ảnh tạm, không xóa lịch sử đọc");
        binding.rowClearCache.textSubtitle.setVisibility(View.VISIBLE);

        binding.rowEmail.textTitle.setText("Gửi mail đến hỗ trợ");
        binding.rowEmail.textSubtitle.setText(SUPPORT_EMAIL);
        binding.rowEmail.textSubtitle.setVisibility(View.VISIBLE);
        binding.rowEmail.getRoot().setOnClickListener(v -> openEmail(SUPPORT_EMAIL));

        binding.rowFacebook.textTitle.setText("Fanpage Facebook");
        binding.rowFacebook.textSubtitle.setText("Theo dõi thông tin và hỗ trợ");
        binding.rowFacebook.textSubtitle.setVisibility(View.VISIBLE);

        binding.rowWebsite.textTitle.setText("Trang web chính thức");
        binding.rowWebsite.textSubtitle.setText("Đang cập nhật");
        binding.rowWebsite.textSubtitle.setVisibility(View.VISIBLE);
        binding.rowWebsite.getRoot().setOnClickListener(v -> showFeatureInProgress(
                "Trang web chính thức",
                "Website chính thức đang được cập nhật. Bạn có thể dùng email hỗ trợ hoặc fanpage Facebook trong thời gian này."
        ));

        setupNotificationRow(settingsPrefs);
    }

    private void setupNotificationRow(SharedPreferences settingsPrefs) {
        boolean storedEnabled = settingsPrefs.getBoolean(KEY_NOTIFICATIONS_ENABLED, true);
        boolean effectiveEnabled = storedEnabled && hasNotificationPermission();
        if (storedEnabled != effectiveEnabled) {
            settingsPrefs.edit().putBoolean(KEY_NOTIFICATIONS_ENABLED, effectiveEnabled).apply();
        }

        binding.rowNotification.textTitle.setText("Nhận thông báo");
        binding.rowNotification.textSubtitle.setVisibility(View.VISIBLE);
        binding.rowNotification.switchToggle.setOnCheckedChangeListener(null);
        binding.rowNotification.switchToggle.setVisibility(View.VISIBLE);
        setNotificationSwitchChecked(effectiveEnabled);
        updateNotificationSubtitle(effectiveEnabled);
        binding.rowNotification.switchToggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (suppressNotificationSwitchChange) return;
            if (isChecked) {
                enableNotifications(settingsPrefs);
            } else {
                settingsPrefs.edit().putBoolean(KEY_NOTIFICATIONS_ENABLED, false).apply();
                updateNotificationSubtitle(false);
                Toast.makeText(requireContext(), "Đã tắt thông báo trong app", Toast.LENGTH_SHORT).show();
            }
        });
        binding.rowNotification.getRoot().setOnClickListener(v -> binding.rowNotification.switchToggle.performClick());
    }

    private void enableNotifications(SharedPreferences settingsPrefs) {
        if (hasNotificationPermission()) {
            settingsPrefs.edit().putBoolean(KEY_NOTIFICATIONS_ENABLED, true).apply();
            setNotificationSwitchChecked(true);
            updateNotificationSubtitle(true);
            Toast.makeText(requireContext(), "Đã bật thông báo", Toast.LENGTH_SHORT).show();
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
        }
    }

    private boolean hasNotificationPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return true;
        }
        return ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void setNotificationSwitchChecked(boolean checked) {
        if (binding == null) return;
        suppressNotificationSwitchChange = true;
        binding.rowNotification.switchToggle.setChecked(checked);
        suppressNotificationSwitchChange = false;
    }

    private void updateNotificationSubtitle(boolean enabled) {
        if (binding == null) return;
        String subtitle;
        if (enabled) {
            subtitle = "Đang bật trong app";
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasNotificationPermission()) {
            subtitle = "Chưa cấp quyền thông báo hệ thống";
        } else {
            subtitle = "Đang tắt trong app";
        }
        binding.rowNotification.textSubtitle.setText(subtitle);
        binding.rowNotification.textSubtitle.setVisibility(View.VISIBLE);
    }

    private void showFeatureInProgress(String title, String message) {
        View view = getLayoutInflater().inflate(R.layout.dialog_feature_notice, null);
        TextView titleView = view.findViewById(R.id.textFeatureTitle);
        TextView messageView = view.findViewById(R.id.textFeatureMessage);
        MaterialButton okButton = view.findViewById(R.id.btnFeatureOk);

        titleView.setText(title);
        messageView.setText(message);

        AlertDialog dialog = new AlertDialog.Builder(requireContext()).create();
        dialog.setView(view);
        dialog.setOnShowListener(d -> {
            if (dialog.getWindow() != null) {
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            }
        });
        okButton.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void showReaderSettingsBottomSheet() {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        View view = getLayoutInflater().inflate(R.layout.bottom_sheet_reader_settings, null);
        dialog.setContentView(view);

        SharedPreferences comicPrefs = requireActivity().getSharedPreferences(ReaderSettings.PREF_COMIC, Context.MODE_PRIVATE);
        SharedPreferences novelPrefs = requireActivity().getSharedPreferences(ReaderSettings.PREF_NOVEL, Context.MODE_PRIVATE);

        SwitchCompat switchFitWidth = view.findViewById(R.id.switchComicFitWidth);
        SwitchCompat switchKeepScreenOn = view.findViewById(R.id.switchComicKeepScreenOn);
        SwitchCompat switchAutoLoadNext = view.findViewById(R.id.switchComicAutoLoadNext);
        TextView textComicSpacing = view.findViewById(R.id.textComicSpacing);
        SeekBar seekComicSpacing = view.findViewById(R.id.seekComicSpacing);

        SwitchCompat switchNovelAutoScroll = view.findViewById(R.id.switchNovelAutoScroll);
        TextView textNovelSize = view.findViewById(R.id.textNovelSize);
        SeekBar seekNovelSize = view.findViewById(R.id.seekNovelSize);
        TextView textNovelSpeed = view.findViewById(R.id.textNovelSpeed);
        SeekBar seekNovelSpeed = view.findViewById(R.id.seekNovelSpeed);
        RadioGroup radioNovelTheme = view.findViewById(R.id.radioNovelTheme);
        MaterialButton closeButton = view.findViewById(R.id.btnCloseReaderSettings);

        int comicSpacing = comicPrefs.getInt(
                ReaderSettings.KEY_COMIC_IMAGE_SPACING_DP,
                ReaderSettings.DEFAULT_COMIC_IMAGE_SPACING_DP
        );
        switchFitWidth.setChecked(comicPrefs.getBoolean(
                ReaderSettings.KEY_COMIC_FIT_WIDTH,
                ReaderSettings.DEFAULT_COMIC_FIT_WIDTH
        ));
        switchKeepScreenOn.setChecked(comicPrefs.getBoolean(
                ReaderSettings.KEY_COMIC_KEEP_SCREEN_ON,
                ReaderSettings.DEFAULT_COMIC_KEEP_SCREEN_ON
        ));
        switchAutoLoadNext.setChecked(comicPrefs.getBoolean(
                ReaderSettings.KEY_COMIC_AUTO_LOAD_NEXT,
                ReaderSettings.DEFAULT_COMIC_AUTO_LOAD_NEXT
        ));
        seekComicSpacing.setProgress(comicSpacing);
        textComicSpacing.setText("Khoảng cách giữa ảnh: " + comicSpacing + "dp");

        float novelTextSize = novelPrefs.getFloat(
                ReaderSettings.KEY_NOVEL_TEXT_SIZE,
                ReaderSettings.DEFAULT_NOVEL_TEXT_SIZE
        );
        int novelTheme = novelPrefs.getInt(
                ReaderSettings.KEY_NOVEL_THEME,
                ReaderSettings.DEFAULT_NOVEL_THEME
        );
        int novelSpeed = novelPrefs.getInt(
                ReaderSettings.KEY_NOVEL_SCROLL_SPEED,
                ReaderSettings.DEFAULT_NOVEL_SCROLL_SPEED
        );
        seekNovelSize.setProgress(Math.max(0, Math.min(20, (int) novelTextSize - 12)));
        textNovelSize.setText("Cỡ chữ truyện chữ: " + (int) novelTextSize + "sp");
        switchNovelAutoScroll.setChecked(novelPrefs.getBoolean(
                ReaderSettings.KEY_NOVEL_AUTO_SCROLL_ENABLED,
                ReaderSettings.DEFAULT_NOVEL_AUTO_SCROLL_ENABLED
        ));
        seekNovelSpeed.setProgress(novelSpeed);
        textNovelSpeed.setText("Tốc độ tự cuộn: " + Math.max(1, novelSpeed));

        if (novelTheme == 1) {
            radioNovelTheme.check(R.id.radioThemeSepia);
        } else if (novelTheme == 2) {
            radioNovelTheme.check(R.id.radioThemeDark);
        } else {
            radioNovelTheme.check(R.id.radioThemeLight);
        }

        switchFitWidth.setOnCheckedChangeListener((buttonView, isChecked) ->
                comicPrefs.edit().putBoolean(ReaderSettings.KEY_COMIC_FIT_WIDTH, isChecked).apply());
        switchKeepScreenOn.setOnCheckedChangeListener((buttonView, isChecked) ->
                comicPrefs.edit().putBoolean(ReaderSettings.KEY_COMIC_KEEP_SCREEN_ON, isChecked).apply());
        switchAutoLoadNext.setOnCheckedChangeListener((buttonView, isChecked) ->
                comicPrefs.edit().putBoolean(ReaderSettings.KEY_COMIC_AUTO_LOAD_NEXT, isChecked).apply());

        seekComicSpacing.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                textComicSpacing.setText("Khoảng cách giữa ảnh: " + progress + "dp");
                if (fromUser) {
                    comicPrefs.edit().putInt(ReaderSettings.KEY_COMIC_IMAGE_SPACING_DP, progress).apply();
                }
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        seekNovelSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float size = 12f + progress;
                textNovelSize.setText("Cỡ chữ truyện chữ: " + (int) size + "sp");
                if (fromUser) {
                    novelPrefs.edit().putFloat(ReaderSettings.KEY_NOVEL_TEXT_SIZE, size).apply();
                }
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        radioNovelTheme.setOnCheckedChangeListener((group, checkedId) -> {
            int theme = 0;
            if (checkedId == R.id.radioThemeSepia) {
                theme = 1;
            } else if (checkedId == R.id.radioThemeDark) {
                theme = 2;
            }
            novelPrefs.edit().putInt(ReaderSettings.KEY_NOVEL_THEME, theme).apply();
        });

        switchNovelAutoScroll.setOnCheckedChangeListener((buttonView, isChecked) ->
                novelPrefs.edit().putBoolean(ReaderSettings.KEY_NOVEL_AUTO_SCROLL_ENABLED, isChecked).apply());

        seekNovelSpeed.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int speed = Math.max(1, progress);
                textNovelSpeed.setText("Tốc độ tự cuộn: " + speed);
                if (fromUser) {
                    novelPrefs.edit().putInt(ReaderSettings.KEY_NOVEL_SCROLL_SPEED, speed).apply();
                }
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        closeButton.setOnClickListener(v -> {
            updateReaderSettingsSubtitle();
            dialog.dismiss();
        });
        dialog.setOnDismissListener(d -> updateReaderSettingsSubtitle());
        dialog.show();
    }

    private void updateReaderSettingsSubtitle() {
        if (binding == null) return;
        SharedPreferences novelPrefs = requireActivity().getSharedPreferences(ReaderSettings.PREF_NOVEL, Context.MODE_PRIVATE);
        int novelTextSize = (int) novelPrefs.getFloat(
                ReaderSettings.KEY_NOVEL_TEXT_SIZE,
                ReaderSettings.DEFAULT_NOVEL_TEXT_SIZE
        );
        int theme = novelPrefs.getInt(ReaderSettings.KEY_NOVEL_THEME, ReaderSettings.DEFAULT_NOVEL_THEME);
        String themeName = theme == 1 ? "sepia" : theme == 2 ? "tối" : "sáng";
        binding.rowReaderSettings.textSubtitle.setText("Mặc định: chữ " + novelTextSize + "sp, nền " + themeName);
    }

    private void navigateBackFromProfile() {
        boolean didNavigateUp = NavHostFragment.findNavController(this).navigateUp();
        if (!didNavigateUp) {
            NavHostFragment.findNavController(this).navigate(R.id.homeFragment);
        }
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
                binding.rowClearCache.textTitle.setText(String.format("Xóa bộ nhớ cache ảnh (%.1f MB)", sizeMb));
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
                if (getActivity() == null || getActivity().isFinishing() || getActivity().isDestroyed()) {
                    return;
                }
                getActivity().runOnUiThread(() -> {
                    com.example.comicversev1.presentation.dialog.UpdateDialog dialog =
                            com.example.comicversev1.presentation.dialog.UpdateDialog.newInstance(
                                    updateInfo,
                                    () -> appUpdateManager.startDownload(updateInfo));
                    dialog.show(getParentFragmentManager(), "UpdateDialog");
                });
            }

            @Override
            public void onNoUpdate() {
                if (getActivity() == null) return;
                getActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(), "Bạn đang sử dụng phiên bản mới nhất!", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onError(String message) {
                if (getActivity() == null) return;
                getActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(), "Lỗi kiểm tra cập nhật!", Toast.LENGTH_SHORT).show());
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
        disposable.add(userDataRepository.syncLocalDataToServer()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        () -> Toast.makeText(requireContext(), "Đồng bộ thành công!", Toast.LENGTH_SHORT).show(),
                        error -> {
                            Log.e(TAG, "Sync error", error);
                            Toast.makeText(requireContext(), "Đồng bộ thất bại!", Toast.LENGTH_SHORT).show();
                        }
                ));
    }

    @Override
    public void onDestroyView() {
        disposable.clear();
        binding = null;
        super.onDestroyView();
    }
}
