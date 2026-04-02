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

import com.example.comicversev1.databinding.FragmentRegisterBinding;
import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class RegisterFragment extends Fragment {

    private FragmentRegisterBinding binding;
    private RegisterViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentRegisterBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(RegisterViewModel.class);
        setupUi();
        observeState();
    }

    private void setupUi() {
        binding.btnBack.setOnClickListener(v -> {
            NavHostFragment.findNavController(this).navigateUp();
        });

        binding.btnRegister.setOnClickListener(v -> {
            String email = binding.edtEmail.getText().toString();
            String nickname = binding.edtNickname.getText().toString();
            String password = binding.edtPassword.getText().toString();
            String confirmPassword = binding.edtConfirmPassword.getText().toString();

            if (email.isEmpty() || nickname.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(requireContext(), "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.equals(confirmPassword)) {
                Toast.makeText(requireContext(), "Mật khẩu không khớp", Toast.LENGTH_SHORT).show();
                return;
            }

            viewModel.register(email, password, nickname);
        });
    }

    private void observeState() {
        viewModel.uiState().observe(getViewLifecycleOwner(), state -> {
            binding.progressBar.setVisibility(state.isLoading() ? View.VISIBLE : View.GONE);
            binding.btnRegister.setEnabled(!state.isLoading());
            
            if (state.isSuccess()) {
                Toast.makeText(requireContext(), "Đăng ký thành công! Vui lòng đăng nhập.", Toast.LENGTH_SHORT).show();
                viewModel.resetState();
                NavHostFragment.findNavController(this).navigateUp();
            }
            if (state.getErrorMessage() != null) {
                Toast.makeText(requireContext(), state.getErrorMessage(), Toast.LENGTH_SHORT).show();
                viewModel.resetState();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
