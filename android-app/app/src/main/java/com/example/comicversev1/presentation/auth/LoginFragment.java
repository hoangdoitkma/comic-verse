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

import com.example.comicversev1.databinding.FragmentLoginBinding;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class LoginFragment extends Fragment {

    private FragmentLoginBinding binding;
    private LoginViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentLoginBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(LoginViewModel.class);
        setupUi();
        observeState();
    }

    private void setupUi() {
        binding.btnBack.setOnClickListener(v -> {
            NavHostFragment.findNavController(this).navigateUp();
        });

        binding.textRegister.setOnClickListener(v -> {
            NavHostFragment.findNavController(this).navigate(com.example.comicversev1.R.id.action_login_to_register);
        });

        binding.btnGoogle.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Login with Google clicked", Toast.LENGTH_SHORT).show();
        });

        binding.btnLogin.setOnClickListener(v -> {
            String email = binding.edtEmail.getText().toString();
            String password = binding.edtPassword.getText().toString();
            
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext(), "Vui lòng nhập email và mật khẩu", Toast.LENGTH_SHORT).show();
                return;
            }
            
            viewModel.login(email, password);
        });
    }

    private void observeState() {
        viewModel.uiState().observe(getViewLifecycleOwner(), state -> {
            binding.progressBar.setVisibility(state.isLoading() ? View.VISIBLE : View.GONE);
            binding.btnLogin.setEnabled(!state.isLoading());
            if (state.isSuccess()) {
                Toast.makeText(requireContext(), "Login success", Toast.LENGTH_SHORT).show();
                try {
                    new ViewModelProvider(requireActivity())
                            .get(com.example.comicversev1.presentation.home.HomeViewModel.class)
                            .refresh();
                    new ViewModelProvider(requireActivity())
                            .get(com.example.comicversev1.presentation.novel.NovelViewModel.class)
                            .refresh();
                } catch (Exception e) {}
                NavHostFragment.findNavController(this).navigate(com.example.comicversev1.R.id.action_login_to_home);
            }
            if (state.getErrorMessage() != null) {
                Toast.makeText(requireContext(), state.getErrorMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

