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

@AndroidEntryPoint
public class ProfileFragment extends Fragment {

    @Inject
    ApiService apiService;

    private CompositeDisposable disposable = new CompositeDisposable();

    private FragmentProfileBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupBottomNav();
        setupView();
    }

    private void setupView() {
        // Setup Version
        binding.textVersion.setText("Phiên bản: " + BuildConfig.VERSION_NAME);

        // Feature: Reader Settings
        binding.rowReaderSettings.textTitle.setText("Cài đặt trình đọc");
        binding.rowReaderSettings.icon.setImageResource(android.R.drawable.ic_menu_preferences);

        // Feature: Reading History
        binding.rowReadingHistory.textTitle.setText("Lịch sử đọc truyện tranh");
        binding.rowReadingHistory.icon.setImageResource(android.R.drawable.ic_menu_recent_history);

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
        binding.rowEmail.getRoot().setOnClickListener(v -> openEmail("comicfan.official@gmail.com"));

        // Contact & Support: Facebook
        binding.rowFacebook.textTitle.setText("Fanpage Facebook");
        binding.rowFacebook.icon.setImageResource(android.R.drawable.ic_menu_share);
        binding.rowFacebook.getRoot().setOnClickListener(v -> openUrl("https://facebook.com"));

        // Contact & Support: Website
        binding.rowWebsite.textTitle.setText("Trang web chính thức");
        binding.rowWebsite.icon.setImageResource(android.R.drawable.ic_menu_info_details);
        binding.rowWebsite.getRoot().setOnClickListener(v -> openUrl("https://mottruyenfan.com"));

        // Profile / Login logic
        android.content.SharedPreferences prefs = requireActivity().getSharedPreferences(com.example.comicversev1.utils.Constants.PREF_AUTH, android.content.Context.MODE_PRIVATE);
        String token = prefs.getString(com.example.comicversev1.utils.Constants.KEY_ACCESS_TOKEN, "");
        String name = prefs.getString(com.example.comicversev1.utils.Constants.KEY_DISPLAY_NAME, "Người dùng ẩn danh");

        if (!token.isEmpty()) {
            binding.textProfileName.setText(name);
            binding.cardProfile.setOnClickListener(v -> {
                new androidx.appcompat.app.AlertDialog.Builder(requireContext())
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
                            // Refresh logic by navigating again to the same fragment
                            NavHostFragment.findNavController(this).navigate(R.id.profileFragment, null, new androidx.navigation.NavOptions.Builder()
                                    .setPopUpTo(R.id.profileFragment, true)
                                    .build());
                        })
                        .setNegativeButton("Hủy", null)
                        .show();
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
                        com.example.comicversev1.data.model.UserProfileDTO profile = response.getData();
                        binding.textProfileName.setText(profile.displayName);
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
                    // Có thể mạng lỗi, vẫn hiển thị status thường cho đỡ trống
                    binding.textVipStatus.setVisibility(View.VISIBLE);
                    binding.textVipStatus.setTextColor(android.graphics.Color.parseColor("#B3B3B3"));
                    binding.textVipStatus.setText("Thành viên Thường");
                })
        );
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
}
