package com.example.comicversev1.presentation.profile;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.bumptech.glide.Glide;
import com.example.comicversev1.R;
import com.example.comicversev1.data.api.ApiService;
import com.example.comicversev1.data.model.ChangePasswordRequest;
import com.example.comicversev1.data.model.UpdateProfileRequest;
import com.example.comicversev1.data.model.UserProfileDTO;
import com.example.comicversev1.databinding.FragmentProfileDetailBinding;
import com.example.comicversev1.utils.Constants;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

@AndroidEntryPoint
public class ProfileDetailFragment extends Fragment {

    @Inject
    ApiService apiService;

    private FragmentProfileDetailBinding binding;
    private CompositeDisposable disposable = new CompositeDisposable();

    private boolean isEditMode = false;
    private Uri pendingAvatarUri = null;
    private UserProfileDTO currentUserProfile = null;

    private ActivityResultLauncher<String> openDocumentLauncher;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileDetailBinding.inflate(inflater, container, false);

        openDocumentLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        pendingAvatarUri = uri;
                        binding.imageAvatar.setImageURI(uri);
                    }
                }
        );

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupListeners();
        toggleEditMode(false);
        fetchUserProfile();
    }

    private void setupListeners() {
        binding.btnBack.setOnClickListener(v -> NavHostFragment.findNavController(this).navigateUp());
        
        binding.fabEdit.setOnClickListener(v -> {
            toggleEditMode(true);
        });

        binding.btnSave.setOnClickListener(v -> {
            saveChanges();
        });

        binding.btnChangePassword.setOnClickListener(v -> showChangePasswordDialog());

        binding.btnLogout.setOnClickListener(v -> logout());

        binding.btnUpdateAvatar.setOnClickListener(v -> {
            openDocumentLauncher.launch("image/*");
        });
    }

    private void toggleEditMode(boolean enable) {
        isEditMode = enable;
        if (enable) {
            binding.layoutViewMode.setVisibility(View.GONE);
            binding.fabEdit.setVisibility(View.GONE);
            binding.layoutEditMode.setVisibility(View.VISIBLE);
            binding.btnSave.setVisibility(View.VISIBLE);
            
            if (currentUserProfile != null) {
                binding.editEmail.setText(currentUserProfile.email);
                binding.editDisplayName.setText(currentUserProfile.displayName);
            }
        } else {
            binding.layoutViewMode.setVisibility(View.VISIBLE);
            binding.fabEdit.setVisibility(View.VISIBLE);
            binding.layoutEditMode.setVisibility(View.GONE);
            binding.btnSave.setVisibility(View.GONE);
            
            // if we cancel edit, we revert the avatar visually
            if (pendingAvatarUri != null) {
                pendingAvatarUri = null;
                if (currentUserProfile != null && currentUserProfile.avatarUrl != null) {
                    Glide.with(this).load(currentUserProfile.avatarUrl).into(binding.imageAvatar);
                } else {
                    binding.imageAvatar.setImageResource(R.mipmap.ic_launcher);
                }
            }
        }
    }

    private void fetchUserProfile() {
        disposable.add(
                apiService.getUserProfile()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(response -> {
                            if (response.isSuccess() && response.getData() != null) {
                                currentUserProfile = response.getData();
                                updateUI(currentUserProfile);
                            } else {
                                Toast.makeText(requireContext(), "Lỗi tải thông tin", Toast.LENGTH_SHORT).show();
                            }
                        }, error -> {
                            Log.e("ProfileDetailFragment", "Error fetching profile", error);
                            // Fallback from SharedPreferences
                            SharedPreferences prefs = requireActivity().getSharedPreferences(Constants.PREF_AUTH, Context.MODE_PRIVATE);
                            currentUserProfile = new UserProfileDTO();
                            currentUserProfile.email = prefs.getString(Constants.KEY_EMAIL, "Unknown");
                            currentUserProfile.displayName = prefs.getString(Constants.KEY_DISPLAY_NAME, "Unknown");
                            updateUI(currentUserProfile);
                        })
        );
    }

    private void updateUI(UserProfileDTO profile) {
        binding.textProfileName.setText(profile.displayName);
        if (profile.avatarUrl != null && !profile.avatarUrl.isEmpty() && pendingAvatarUri == null) {
            Glide.with(this).load(profile.avatarUrl)
                 .placeholder(R.mipmap.ic_launcher)
                 .into(binding.imageAvatar);
        }

        binding.viewEmail.setText(profile.email);
        binding.viewDisplayName.setText(profile.displayName);

        if (profile.vip) {
            binding.textVipStatus.setVisibility(View.VISIBLE);
            binding.textVipStatus.setTextColor(android.graphics.Color.parseColor("#FFD700"));
            if (profile.vipEndDate != null && !profile.vipEndDate.isEmpty()) {
                try {
                    LocalDateTime endDate = LocalDateTime.parse(profile.vipEndDate);
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
                    String exp = "Hạn sử dụng VIP: " + endDate.format(formatter);
                    binding.textVipStatus.setText("Thành viên VIP");
                    binding.viewAccountType.setText("VIP - " + exp);
                } catch (Exception e) {
                    binding.textVipStatus.setText("Thành viên VIP");
                    binding.viewAccountType.setText("Thành viên VIP");
                }
            } else {
                binding.textVipStatus.setText("Thành viên VIP Trọn đời");
                binding.viewAccountType.setText("Thành viên VIP Trọn đời");
            }
        } else {
            binding.textVipStatus.setVisibility(View.VISIBLE);
            binding.textVipStatus.setTextColor(android.graphics.Color.parseColor("#B3B3B3"));
            binding.textVipStatus.setText("Thành viên Thường");
            binding.viewAccountType.setText("Bình thường");
        }
    }

    private void saveChanges() {
        String newName = binding.editDisplayName.getText() != null ? 
                         binding.editDisplayName.getText().toString().trim() : "";

        if (newName.isEmpty()) {
            Toast.makeText(requireContext(), "Tên hiển thị không được để trống", Toast.LENGTH_SHORT).show();
            return;
        }

        binding.btnSave.setEnabled(false);
        Toast.makeText(requireContext(), "Đang lưu...", Toast.LENGTH_SHORT).show();

        Completable updateNameCompletable = Completable.complete();
        Completable updateAvatarCompletable = Completable.complete();

        if (currentUserProfile != null && !newName.equals(currentUserProfile.displayName)) {
            UpdateProfileRequest request = new UpdateProfileRequest(newName);
            updateNameCompletable = apiService.updateProfile(request)
                    .flatMapCompletable(response -> {
                        if (response.isSuccess()) {
                            SharedPreferences prefs = requireActivity().getSharedPreferences(Constants.PREF_AUTH, Context.MODE_PRIVATE);
                            prefs.edit().putString(Constants.KEY_DISPLAY_NAME, newName).apply();
                            return Completable.complete();
                        } else {
                            return Completable.error(new Exception("Update Name Failed: " + response.getMessage()));
                        }
                    });
        }

        if (pendingAvatarUri != null) {
            updateAvatarCompletable = Single.defer(() -> {
                try {
                    InputStream inputStream = requireContext().getContentResolver().openInputStream(pendingAvatarUri);
                    if (inputStream == null) return Single.error(new Exception("Null InputStream"));
                    byte[] bytes = new byte[inputStream.available()];
                    inputStream.read(bytes);

                    String mimeType = requireContext().getContentResolver().getType(pendingAvatarUri);
                    if (mimeType == null) mimeType = "image/jpeg";
                    RequestBody requestFile = RequestBody.create(MediaType.parse(mimeType), bytes);
                    MultipartBody.Part body = MultipartBody.Part.createFormData("file", "avatar.jpg", requestFile);

                    return apiService.uploadAvatar(body);
                } catch (Exception e) {
                    return Single.error(e);
                }
            }).flatMapCompletable(responseObj -> {
                com.example.comicversev1.data.model.BaseResponse<String> response = (com.example.comicversev1.data.model.BaseResponse<String>) responseObj;
                if (response.isSuccess()) {
                    return Completable.complete();
                } else {
                    return Completable.error(new Exception("Upload Avatar Failed: " + response.getMessage()));
                }
            });
        }

        disposable.add(
            updateAvatarCompletable.andThen(updateNameCompletable)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    () -> {
                        Toast.makeText(requireContext(), "Đã lưu thay đổi", Toast.LENGTH_SHORT).show();
                        pendingAvatarUri = null;
                        binding.btnSave.setEnabled(true);
                        toggleEditMode(false);
                        fetchUserProfile();
                    },
                    error -> {
                        Toast.makeText(requireContext(), "Lỗi khi lưu thay đổi", Toast.LENGTH_SHORT).show();
                        Log.e("ProfileDetailFragment", "Save error", error);
                        binding.btnSave.setEnabled(true);
                    }
                )
        );
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

    private void logout() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Đăng xuất")
                .setMessage("Bạn có chắc chắn muốn đăng xuất không?")
                .setPositiveButton("Đăng xuất", (dialog, which) -> {
                    SharedPreferences prefs = requireActivity().getSharedPreferences(Constants.PREF_AUTH, Context.MODE_PRIVATE);
                    prefs.edit()
                            .remove(Constants.KEY_ACCESS_TOKEN)
                            .remove(Constants.KEY_REFRESH_TOKEN)
                            .remove(Constants.KEY_DISPLAY_NAME)
                            .remove(Constants.KEY_EMAIL)
                            .apply();
                    Toast.makeText(requireContext(), "Đã đăng xuất", Toast.LENGTH_SHORT).show();

                    try {
                        new androidx.lifecycle.ViewModelProvider(requireActivity())
                                .get(com.example.comicversev1.presentation.home.HomeViewModel.class)
                                .refresh();
                        new androidx.lifecycle.ViewModelProvider(requireActivity())
                                .get(com.example.comicversev1.presentation.novel.NovelViewModel.class)
                                .refresh();
                    } catch (Exception e) {
                        // ignore
                    }

                    // Navigate back to home or pop entirely
                    NavHostFragment.findNavController(this).popBackStack(R.id.profileFragment, false);
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        disposable.clear();
        binding = null;
    }
}
