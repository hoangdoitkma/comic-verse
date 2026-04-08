package com.example.comicversev1.presentation.profile;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.bumptech.glide.Glide;
import com.example.comicversev1.BuildConfig;
import com.example.comicversev1.R;
import com.example.comicversev1.databinding.FragmentProfileBinding;

import java.io.File;

import dagger.hilt.android.AndroidEntryPoint;

import com.example.comicversev1.data.api.ApiService;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import android.util.Log;
import javax.inject.Inject;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;
import com.example.comicversev1.data.local.dao.ReadingHistoryDao;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import android.content.SharedPreferences;
import android.content.Context;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.app.AlertDialog;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.LinearLayout;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import com.example.comicversev1.data.model.ChangePasswordRequest;
import com.example.comicversev1.data.model.UpdateProfileRequest;
import com.example.comicversev1.utils.Constants;

@AndroidEntryPoint
public class ProfileFragment extends Fragment {

    @Inject
    ApiService apiService;

    @Inject
    ReadingHistoryDao readingHistoryDao;

    private CompositeDisposable disposable = new CompositeDisposable();

    private FragmentProfileBinding binding;

    private ActivityResultLauncher<String> openDocumentLauncher;
    private android.net.Uri selectedImageUri = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        openDocumentLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        selectedImageUri = uri;
                        binding.imageAvatar.setImageURI(uri);
                        uploadAvatar(uri);
                    }
                }
        );
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

        // Feature: Sync Data (formerly Reading History)
        binding.rowReadingHistory.textTitle.setText("Đồng bộ dữ liệu");
        binding.rowReadingHistory.icon.setImageResource(android.R.drawable.ic_popup_sync);
        binding.rowReadingHistory.getRoot().setOnClickListener(v -> syncData());

        // Feature: Download List
        binding.rowDownloadList.textTitle.setText("Danh sách tải");
        binding.rowDownloadList.icon.setImageResource(android.R.drawable.ic_menu_save);

        // Feature: Clear Cache
        calculateCacheSize();
        binding.rowClearCache.icon.setImageResource(android.R.drawable.ic_menu_delete);
        binding.rowClearCache.getRoot().setOnClickListener(v -> {
            clearGlideCache();
        });

        // Contact & Support: Email
        binding.rowEmail.textTitle.setText("Gửi mail đến hỗ trợ");
        binding.rowEmail.icon.setImageResource(android.R.drawable.ic_dialog_email);
        binding.rowEmail.getRoot().setOnClickListener(v -> openEmail("hoangdoitkma@gmail.com"));

        // Contact & Support: Facebook
        binding.rowFacebook.textTitle.setText("Fanpage Facebook");
        binding.rowFacebook.icon.setImageResource(android.R.drawable.ic_menu_share);
        binding.rowFacebook.getRoot().setOnClickListener(v -> openUrl("https://facebook.com/duchoang3m"));

        // Contact & Support: Website
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

        // Account management features
        binding.rowAccountInfo.textTitle.setText("Thông tin tài khoản");
        binding.rowAccountInfo.icon.setImageResource(android.R.drawable.ic_menu_edit);
        binding.rowAccountInfo.getRoot().setOnClickListener(v -> showUpdateProfileDialog());

        binding.rowChangePassword.textTitle.setText("Đổi mật khẩu");
        binding.rowChangePassword.icon.setImageResource(android.R.drawable.ic_secure);
        if (binding.rowChangePassword.icon.getDrawable() == null) {
            binding.rowChangePassword.icon.setImageResource(android.R.drawable.ic_lock_idle_lock);
        }
        binding.rowChangePassword.getRoot().setOnClickListener(v -> showChangePasswordDialog());

        // Profile / Login logic
        android.content.SharedPreferences prefs = requireActivity().getSharedPreferences(
                Constants.PREF_AUTH, Context.MODE_PRIVATE);
        String token = prefs.getString(Constants.KEY_ACCESS_TOKEN, "");
        String name = prefs.getString(Constants.KEY_DISPLAY_NAME, "Người dùng ẩn danh");

        if (!token.isEmpty()) {
            binding.rowAccountInfo.getRoot().setVisibility(View.VISIBLE);
            binding.rowChangePassword.getRoot().setVisibility(View.VISIBLE);
            binding.textProfileName.setText(name);
            binding.imageAvatar.setOnClickListener(v -> {
                openDocumentLauncher.launch("image/*");
            });
            binding.cardProfile.setOnClickListener(v -> {
                new AlertDialog.Builder(requireContext())
                        .setTitle("Đăng xuất")
                        .setMessage("Bạn có chắc chắn muốn đăng xuất không?")
                        .setPositiveButton("Đăng xuất", (dialog, which) -> {
                            prefs.edit()
                                    .remove(com.example.comicversev1.utils.Constants.KEY_ACCESS_TOKEN)
                                    .remove(com.example.comicversev1.utils.Constants.KEY_REFRESH_TOKEN)
                                    .remove(com.example.comicversev1.utils.Constants.KEY_DISPLAY_NAME)
                                    .remove(com.example.comicversev1.utils.Constants.KEY_EMAIL)
                                    .apply();
                            Toast.makeText(requireContext(), "Đã đăng xuất", Toast.LENGTH_SHORT).show();
                            
                            // Clear cached username/greeting in other ViewModels
                            try {
                                new androidx.lifecycle.ViewModelProvider(requireActivity())
                                        .get(com.example.comicversev1.presentation.home.HomeViewModel.class)
                                        .refresh();
                                new androidx.lifecycle.ViewModelProvider(requireActivity())
                                        .get(com.example.comicversev1.presentation.novel.NovelViewModel.class)
                                        .refresh();
                            } catch (Exception e) {
                                // Ignore if ViewModels are not yet created
                            }

                            // Refresh logic by navigating again to the same fragment
                            NavHostFragment.findNavController(this).navigate(R.id.profileFragment, null,
                                    new androidx.navigation.NavOptions.Builder()
                                            .setPopUpTo(R.id.profileFragment, true)
                                            .build());
                        })
                        .setNegativeButton("Hủy", null)
                        .show();
            });
            fetchUserProfile();
        } else {
            binding.rowAccountInfo.getRoot().setVisibility(View.GONE);
            binding.rowChangePassword.getRoot().setVisibility(View.GONE);
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
                                com.example.comicversev1.data.model.UserProfileDTO profile = response.getData();
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
                                            DateTimeFormatter formatter = DateTimeFormatter
                                                    .ofPattern("dd/MM/yyyy HH:mm");
                                            binding.textVipStatus.setText(
                                                    "Thành viên VIP - Hạn sử dụng: " + endDate.format(formatter));
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
                            // Có thể mạng lỗi, vẫn hiển thị status thường cho đỡ trống
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

    private void showChangePasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Đổi mật khẩu");
        
        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);
        
        final EditText oldPassInput = new EditText(requireContext());
        oldPassInput.setHint("Mật khẩu cũ");
        oldPassInput.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(oldPassInput);
        
        final EditText newPassInput = new EditText(requireContext());
        newPassInput.setHint("Mật khẩu mới");
        newPassInput.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(newPassInput);
        
        builder.setView(layout);
        builder.setPositiveButton("Cập nhật", (dialog, which) -> {
            String oldPass = oldPassInput.getText().toString();
            String newPass = newPassInput.getText().toString();
            if (oldPass.isEmpty() || newPass.isEmpty()) {
                Toast.makeText(requireContext(), "Vui lòng nhập đủ mật khẩu", Toast.LENGTH_SHORT).show();
                return;
            }
            ChangePasswordRequest request = new ChangePasswordRequest(oldPass, newPass);
            disposable.add(apiService.changePassword(request)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(response -> {
                        if (response.isSuccess()) {
                            Toast.makeText(requireContext(), "Đổi mật khẩu thành công", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(requireContext(), response.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }, error -> Toast.makeText(requireContext(), "Lỗi khi đổi mật khẩu", Toast.LENGTH_SHORT).show())
            );
        });
        builder.setNegativeButton("Hủy", null);
        builder.show();
    }

    private void showUpdateProfileDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Cập nhật tên hiển thị");
        
        final EditText nameInput = new EditText(requireContext());
        nameInput.setText(binding.textProfileName.getText());
        LinearLayout layout = new LinearLayout(requireContext());
        layout.setPadding(50, 20, 50, 10);
        layout.addView(nameInput, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        builder.setView(layout);
        
        builder.setPositiveButton("Cập nhật", (dialog, which) -> {
            String newName = nameInput.getText().toString();
            if (newName.isEmpty()) return;
            UpdateProfileRequest request = new UpdateProfileRequest(newName);
            disposable.add(apiService.updateProfile(request)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(response -> {
                        if (response.isSuccess()) {
                            binding.textProfileName.setText(newName);
                            SharedPreferences prefs = requireActivity().getSharedPreferences(Constants.PREF_AUTH, Context.MODE_PRIVATE);
                            prefs.edit().putString(Constants.KEY_DISPLAY_NAME, newName).apply();
                            Toast.makeText(requireContext(), "Đã cập nhật tên", Toast.LENGTH_SHORT).show();
                        }
                    }, error -> Toast.makeText(requireContext(), "Lỗi khi cập nhật tên", Toast.LENGTH_SHORT).show())
            );
        });
        builder.setNegativeButton("Hủy", null);
        builder.show();
    }

    private void uploadAvatar(android.net.Uri uri) {
        try {
            java.io.InputStream inputStream = requireContext().getContentResolver().openInputStream(uri);
            if (inputStream == null) return;
            byte[] bytes = new byte[inputStream.available()];
            inputStream.read(bytes);
            
            String mimeType = requireContext().getContentResolver().getType(uri);
            if (mimeType == null) mimeType = "image/jpeg";
            RequestBody requestFile = RequestBody.create(MediaType.parse(mimeType), bytes);
            MultipartBody.Part body = MultipartBody.Part.createFormData("file", "avatar.jpg", requestFile);
            
            Toast.makeText(requireContext(), "Đang tải ảnh lên...", Toast.LENGTH_SHORT).show();
            disposable.add(apiService.uploadAvatar(body)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(response -> {
                        if (response.isSuccess()) {
                            fetchUserProfile(); // Reload profile with new avatar
                            Toast.makeText(requireContext(), "Cập nhật ảnh đại diện thành công", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(requireContext(), "Lỗi upload: " + response.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }, error -> Toast.makeText(requireContext(), "Lỗi kết nối khi upload", Toast.LENGTH_SHORT).show())
            );
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(requireContext(), "Lỗi đọc file ảnh", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupBottomNav() {
        // Highlight correct menu
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
        // Clear memory cache on main thread
        Glide.get(requireContext()).clearMemory();

        // Clear disk cache on background thread
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

    private void syncData() {
        android.content.SharedPreferences prefs = requireActivity().getSharedPreferences(
                com.example.comicversev1.utils.Constants.PREF_AUTH, android.content.Context.MODE_PRIVATE);
        String token = prefs.getString(com.example.comicversev1.utils.Constants.KEY_ACCESS_TOKEN, "");
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
