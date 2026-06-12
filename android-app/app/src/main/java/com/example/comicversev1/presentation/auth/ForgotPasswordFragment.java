package com.example.comicversev1.presentation.auth;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.example.comicversev1.databinding.FragmentForgotPasswordBinding;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ForgotPasswordFragment extends Fragment {

    private FragmentForgotPasswordBinding binding;
    private ForgotPasswordViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentForgotPasswordBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(ForgotPasswordViewModel.class);
        setupUi();
        observeState();
    }

    private void setupUi() {
        binding.btnBack.setOnClickListener(v -> NavHostFragment.findNavController(this).navigateUp());

        binding.btnSendCode.setOnClickListener(v -> {
            String email = binding.edtEmail.getText().toString().trim();
            if (email.isEmpty()) {
                Toast.makeText(requireContext(), "Vui lòng nhập email", Toast.LENGTH_SHORT).show();
                return;
            }
            viewModel.requestCode(email);
        });

        binding.btnResetPassword.setOnClickListener(v -> {
            String email = binding.edtEmail.getText().toString().trim();
            String otp = binding.edtOtp.getText().toString().trim();
            String newPassword = binding.edtNewPassword.getText().toString();
            String confirmPassword = binding.edtConfirmPassword.getText().toString();

            if (otp.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(requireContext(), "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!otp.matches("\\d{6}")) {
                Toast.makeText(requireContext(), "Mã xác nhận gồm 6 chữ số", Toast.LENGTH_SHORT).show();
                return;
            }
            if (newPassword.length() < 6) {
                Toast.makeText(requireContext(), "Mật khẩu mới tối thiểu 6 ký tự", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!newPassword.equals(confirmPassword)) {
                Toast.makeText(requireContext(), "Mật khẩu xác nhận không khớp", Toast.LENGTH_SHORT).show();
                return;
            }

            viewModel.resetPassword(email, otp, newPassword);
        });
    }

    private void observeState() {
        viewModel.uiState().observe(getViewLifecycleOwner(), state -> {
            binding.progressBar.setVisibility(state.isLoading() ? View.VISIBLE : View.GONE);
            binding.btnSendCode.setEnabled(!state.isLoading());
            binding.btnResetPassword.setEnabled(!state.isLoading());
            binding.layoutResetFields.setVisibility(state.isCodeSent() ? View.VISIBLE : View.GONE);

            if (state.isCodeSent()) {
                binding.textSubtitle.setText("Nhập mã 6 số đã gửi tới email và đặt mật khẩu mới.");
                binding.btnSendCode.setText("Gửi lại mã");
            }

            if (state.getMessage() != null) {
                Toast.makeText(requireContext(), state.getMessage(), Toast.LENGTH_SHORT).show();
            }

            if (state.getErrorMessage() != null) {
                Toast.makeText(requireContext(), state.getErrorMessage(), Toast.LENGTH_SHORT).show();
            }

            if (state.isResetSuccess()) {
                NavHostFragment.findNavController(this).navigateUp();
            }
        });
    }

    @Override
    public void onDestroyView() {
        binding = null;
        super.onDestroyView();
    }
}
