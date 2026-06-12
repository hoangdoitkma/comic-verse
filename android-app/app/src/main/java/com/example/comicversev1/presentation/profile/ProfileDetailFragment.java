package com.example.comicversev1.presentation.profile;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.bumptech.glide.Glide;
import com.example.comicversev1.R;
import com.example.comicversev1.data.model.ChangePasswordRequest;
import com.example.comicversev1.data.model.UpdateProfileRequest;
import com.example.comicversev1.data.model.UserProfileDTO;
import com.example.comicversev1.data.repository.UserDataRepository;
import com.example.comicversev1.data.repository.UserProfileRepository;
import com.example.comicversev1.databinding.FragmentProfileDetailBinding;
import com.example.comicversev1.presentation.home.HomeViewModel;
import com.example.comicversev1.presentation.novel.NovelViewModel;
import com.example.comicversev1.utils.Constants;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

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

    private static final String TAG = "ProfileDetail";

    @Inject
    UserProfileRepository userProfileRepository;

    @Inject
    UserDataRepository userDataRepository;

    private final CompositeDisposable disposable = new CompositeDisposable();
    private FragmentProfileDetailBinding binding;
    private Uri pendingAvatarUri = null;
    private UserProfileDTO currentUserProfile = null;
    private ActivityResultLauncher<String> openDocumentLauncher;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileDetailBinding.inflate(inflater, container, false);
        openDocumentLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        pendingAvatarUri = uri;
                        binding.imageAvatar.setImageURI(uri);
                    }
                });
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
        binding.fabEdit.setOnClickListener(v -> toggleEditMode(true));
        binding.btnSave.setOnClickListener(v -> saveChanges());
        binding.btnChangePassword.setOnClickListener(v -> showChangePasswordDialog());
        binding.btnLogout.setOnClickListener(v -> logout());
        binding.btnUpdateAvatar.setOnClickListener(v -> openDocumentLauncher.launch("image/*"));
    }

    private void toggleEditMode(boolean enable) {
        if (enable) {
            binding.layoutViewMode.setVisibility(View.GONE);
            binding.fabEdit.setVisibility(View.GONE);
            binding.layoutEditMode.setVisibility(View.VISIBLE);
            binding.btnSave.setVisibility(View.VISIBLE);
            if (currentUserProfile != null) {
                binding.editEmail.setText(currentUserProfile.email);
                binding.editDisplayName.setText(currentUserProfile.displayName);
            }
            return;
        }

        binding.layoutViewMode.setVisibility(View.VISIBLE);
        binding.fabEdit.setVisibility(View.VISIBLE);
        binding.layoutEditMode.setVisibility(View.GONE);
        binding.btnSave.setVisibility(View.GONE);

        if (pendingAvatarUri != null) {
            pendingAvatarUri = null;
            if (currentUserProfile != null && currentUserProfile.avatarUrl != null) {
                Glide.with(this).load(currentUserProfile.avatarUrl).into(binding.imageAvatar);
            } else {
                binding.imageAvatar.setImageResource(R.mipmap.ic_launcher);
            }
        }
    }

    private void fetchUserProfile() {
        disposable.add(userProfileRepository.getUserProfile()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(profile -> {
                    currentUserProfile = profile;
                    updateUI(profile);
                }, error -> {
                    Log.e(TAG, "Error fetching profile", error);
                    SharedPreferences prefs = requireActivity().getSharedPreferences(Constants.PREF_AUTH, Context.MODE_PRIVATE);
                    currentUserProfile = new UserProfileDTO();
                    currentUserProfile.email = prefs.getString(Constants.KEY_EMAIL, "Unknown");
                    currentUserProfile.displayName = prefs.getString(Constants.KEY_DISPLAY_NAME, "Unknown");
                    updateUI(currentUserProfile);
                }));
    }

    private void updateUI(UserProfileDTO profile) {
        binding.textProfileName.setText(profile.displayName);
        SharedPreferences prefs = requireActivity().getSharedPreferences(Constants.PREF_AUTH, Context.MODE_PRIVATE);
        if (profile.displayName != null && !profile.displayName.trim().isEmpty()) {
            prefs.edit().putString(Constants.KEY_DISPLAY_NAME, profile.displayName.trim()).apply();
        }
        if (profile.avatarUrl != null && !profile.avatarUrl.isEmpty() && pendingAvatarUri == null) {
            prefs.edit().putString(Constants.KEY_AVATAR_URL, profile.avatarUrl).apply();
            Glide.with(this)
                    .load(profile.avatarUrl)
                    .placeholder(R.mipmap.ic_launcher)
                    .into(binding.imageAvatar);
        }

        binding.viewEmail.setText(profile.email);
        binding.viewDisplayName.setText(profile.displayName);

        binding.textVipStatus.setVisibility(View.VISIBLE);
        if (profile.vip) {
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
            binding.textVipStatus.setTextColor(android.graphics.Color.parseColor("#B3B3B3"));
            binding.textVipStatus.setText("Thành viên Thường");
            binding.viewAccountType.setText("Bình thường");
        }
    }

    private void saveChanges() {
        String newName = binding.editDisplayName.getText() != null
                ? binding.editDisplayName.getText().toString().trim()
                : "";

        if (newName.isEmpty()) {
            Toast.makeText(requireContext(), "Tên hiển thị không được để trống", Toast.LENGTH_SHORT).show();
            return;
        }

        binding.btnSave.setEnabled(false);
        Toast.makeText(requireContext(), "Đang lưu...", Toast.LENGTH_SHORT).show();

        Completable updateNameCompletable = Completable.complete();
        Completable updateAvatarCompletable = Completable.complete();

        if (currentUserProfile != null && !newName.equals(currentUserProfile.displayName)) {
            updateNameCompletable = userProfileRepository.updateProfile(new UpdateProfileRequest(newName))
                    .doOnComplete(() -> {
                        SharedPreferences prefs = requireActivity().getSharedPreferences(Constants.PREF_AUTH, Context.MODE_PRIVATE);
                        prefs.edit().putString(Constants.KEY_DISPLAY_NAME, newName).apply();
                    });
        }

        if (pendingAvatarUri != null) {
            updateAvatarCompletable = createAvatarPart(pendingAvatarUri)
                    .flatMapCompletable(userProfileRepository::uploadAvatar);
        }

        disposable.add(updateAvatarCompletable.andThen(updateNameCompletable)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                    Toast.makeText(requireContext(), "Đã lưu thay đổi", Toast.LENGTH_SHORT).show();
                    pendingAvatarUri = null;
                    binding.btnSave.setEnabled(true);
                    toggleEditMode(false);
                    fetchUserProfile();
                }, error -> {
                    Toast.makeText(requireContext(), "Lỗi khi lưu thay đổi", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Save error", error);
                    binding.btnSave.setEnabled(true);
                }));
    }

    private Single<MultipartBody.Part> createAvatarPart(Uri avatarUri) {
        return Single.fromCallable(() -> {
            try (InputStream inputStream = requireContext().getContentResolver().openInputStream(avatarUri)) {
                if (inputStream == null) {
                    throw new Exception("Null InputStream");
                }
                byte[] bytes = new byte[inputStream.available()];
                int read = inputStream.read(bytes);
                if (read <= 0) {
                    throw new Exception("Empty avatar file");
                }

                String mimeType = requireContext().getContentResolver().getType(avatarUri);
                if (mimeType == null) {
                    mimeType = "image/jpeg";
                }
                RequestBody requestFile = RequestBody.create(MediaType.parse(mimeType), bytes);
                return MultipartBody.Part.createFormData("file", "avatar.jpg", requestFile);
            }
        });
    }

    private void showChangePasswordDialog() {
        View view = getLayoutInflater().inflate(R.layout.dialog_change_password, null);
        TextInputLayout oldPassLayout = view.findViewById(R.id.layoutOldPassword);
        TextInputLayout newPassLayout = view.findViewById(R.id.layoutNewPassword);
        TextInputLayout confirmPassLayout = view.findViewById(R.id.layoutConfirmPassword);
        TextInputEditText oldPassInput = view.findViewById(R.id.editOldPassword);
        TextInputEditText newPassInput = view.findViewById(R.id.editNewPassword);
        TextInputEditText confirmPassInput = view.findViewById(R.id.editConfirmPassword);
        MaterialButton cancelButton = view.findViewById(R.id.btnCancelChangePassword);
        MaterialButton submitButton = view.findViewById(R.id.btnSubmitChangePassword);

        AlertDialog dialog = new AlertDialog.Builder(requireContext()).create();
        dialog.setView(view);
        dialog.setOnShowListener(d -> {
            if (dialog.getWindow() != null) {
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            }
        });

        cancelButton.setOnClickListener(v -> dialog.dismiss());
        submitButton.setOnClickListener(v -> {
            String oldPass = readPasswordInput(oldPassInput);
            String newPass = readPasswordInput(newPassInput);
            String confirmPass = readPasswordInput(confirmPassInput);

            if (!validateChangePasswordForm(
                    oldPassLayout,
                    newPassLayout,
                    confirmPassLayout,
                    oldPass,
                    newPass,
                    confirmPass
            )) {
                return;
            }

            submitButton.setEnabled(false);
            cancelButton.setEnabled(false);
            submitButton.setText("Đang cập nhật...");

            disposable.add(userProfileRepository.changePassword(new ChangePasswordRequest(oldPass, newPass))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            message -> {
                                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            },
                            error -> {
                                String message = error.getMessage();
                                if (message == null || message.trim().isEmpty()) {
                                    message = "Không thể đổi mật khẩu. Vui lòng thử lại.";
                                }
                                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                                submitButton.setEnabled(true);
                                cancelButton.setEnabled(true);
                                submitButton.setText("Cập nhật");
                            }
                    ));
        });

        dialog.show();
    }

    private String readPasswordInput(TextInputEditText input) {
        return input.getText() != null ? input.getText().toString().trim() : "";
    }

    private boolean validateChangePasswordForm(TextInputLayout oldPassLayout,
                                               TextInputLayout newPassLayout,
                                               TextInputLayout confirmPassLayout,
                                               String oldPass,
                                               String newPass,
                                               String confirmPass) {
        oldPassLayout.setError(null);
        newPassLayout.setError(null);
        confirmPassLayout.setError(null);

        if (oldPass.isEmpty()) {
            oldPassLayout.setError("Vui lòng nhập mật khẩu hiện tại");
            return false;
        }
        if (newPass.isEmpty()) {
            newPassLayout.setError("Vui lòng nhập mật khẩu mới");
            return false;
        }
        if (newPass.length() < 6) {
            newPassLayout.setError("Mật khẩu mới cần tối thiểu 6 ký tự");
            return false;
        }
        if (newPass.equals(oldPass)) {
            newPassLayout.setError("Mật khẩu mới phải khác mật khẩu hiện tại");
            return false;
        }
        if (confirmPass.isEmpty()) {
            confirmPassLayout.setError("Vui lòng xác nhận mật khẩu mới");
            return false;
        }
        if (!newPass.equals(confirmPass)) {
            confirmPassLayout.setError("Mật khẩu xác nhận không khớp");
            return false;
        }
        return true;
    }

    private void logout() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Đăng xuất")
                .setMessage("Bạn có chắc chắn muốn đăng xuất không?")
                .setPositiveButton("Đăng xuất", (dialog, which) -> {
                    disposable.add(userDataRepository.clearLocalUserData()
                            .subscribeOn(Schedulers.io())
                            .subscribe(
                                    () -> Log.d(TAG, "Cleared local user data on logout"),
                                    error -> Log.e(TAG, "Failed to clear local user data on logout", error)
                            ));

                    SharedPreferences prefs = requireActivity().getSharedPreferences(Constants.PREF_AUTH, Context.MODE_PRIVATE);
                    prefs.edit()
                            .remove(Constants.KEY_ACCESS_TOKEN)
                            .remove(Constants.KEY_REFRESH_TOKEN)
                            .remove(Constants.KEY_DISPLAY_NAME)
                            .remove(Constants.KEY_EMAIL)
                            .remove(Constants.KEY_AVATAR_URL)
                            .apply();
                    Toast.makeText(requireContext(), "Đã đăng xuất", Toast.LENGTH_SHORT).show();

                    try {
                        new ViewModelProvider(requireActivity()).get(HomeViewModel.class).refresh();
                        new ViewModelProvider(requireActivity()).get(NovelViewModel.class).refresh();
                    } catch (Exception ignored) {
                    }

                    NavHostFragment.findNavController(this).popBackStack(R.id.profileFragment, false);
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    @Override
    public void onDestroyView() {
        disposable.clear();
        binding = null;
        super.onDestroyView();
    }
}
