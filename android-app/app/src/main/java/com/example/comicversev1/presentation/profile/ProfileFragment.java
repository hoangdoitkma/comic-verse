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

@AndroidEntryPoint
public class ProfileFragment extends Fragment {

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
            binding.cardProfile.setFocusable(false);
            binding.cardProfile.setClickable(false);
            binding.cardProfile.setOnClickListener(v -> {
                // Future: Navigate to Profile details or show Logout popup
                Toast.makeText(requireContext(), "Bạn đã đăng nhập dưới tên: " + name, Toast.LENGTH_SHORT).show();
            });
        } else {
            binding.textProfileName.setText("Đăng nhập hoặc đăng ký");
            binding.cardProfile.setOnClickListener(v -> {
                NavHostFragment.findNavController(this).navigate(R.id.loginFragment);
            });
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
