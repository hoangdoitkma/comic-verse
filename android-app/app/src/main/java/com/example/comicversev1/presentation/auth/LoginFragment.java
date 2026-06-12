package com.example.comicversev1.presentation.auth;

import android.os.CancellationSignal;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.credentials.Credential;
import androidx.credentials.CredentialManager;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.CustomCredential;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.exceptions.GetCredentialCancellationException;
import androidx.credentials.exceptions.GetCredentialException;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.example.comicversev1.BuildConfig;
import com.example.comicversev1.R;
import com.example.comicversev1.databinding.FragmentLoginBinding;
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class LoginFragment extends Fragment {

    private static final String TAG = "LoginFragment";

    private FragmentLoginBinding binding;
    private LoginViewModel viewModel;
    private CredentialManager credentialManager;
    private ExecutorService credentialExecutor;

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
        credentialManager = CredentialManager.create(requireContext());
        credentialExecutor = Executors.newSingleThreadExecutor();
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

        binding.textForgotPassword.setOnClickListener(v -> {
            NavHostFragment.findNavController(this).navigate(R.id.action_login_to_forgotPassword);
        });

        binding.btnGoogle.setOnClickListener(v -> {
            startGoogleSignIn();
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
            binding.btnGoogle.setEnabled(!state.isLoading());
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

    private void startGoogleSignIn() {
        if (BuildConfig.GOOGLE_WEB_CLIENT_ID == null || BuildConfig.GOOGLE_WEB_CLIENT_ID.trim().isEmpty()) {
            Toast.makeText(requireContext(), "Google login chưa được cấu hình", Toast.LENGTH_SHORT).show();
            return;
        }

        GetSignInWithGoogleOption googleIdOption = new GetSignInWithGoogleOption.Builder(BuildConfig.GOOGLE_WEB_CLIENT_ID)
                .build();

        GetCredentialRequest request = new GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build();

        credentialManager.getCredentialAsync(
                requireActivity(),
                request,
                new CancellationSignal(),
                credentialExecutor,
                new CredentialManagerCallback<GetCredentialResponse, GetCredentialException>() {
                    @Override
                    public void onResult(GetCredentialResponse result) {
                        handleGoogleCredential(result);
                    }

                    @Override
                    public void onError(@NonNull GetCredentialException e) {
                        if (e instanceof GetCredentialCancellationException) {
                            Log.d(TAG, "Google sign-in was cancelled by the user");
                            return;
                        }

                        Log.w(TAG, "Google sign-in failed: " + e.getClass().getSimpleName(), e);
                        if (getActivity() == null) return;
                        requireActivity().runOnUiThread(() ->
                                Toast.makeText(requireContext(), "Không thể đăng nhập Google: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    }
                }
        );
    }

    private void handleGoogleCredential(GetCredentialResponse response) {
        try {
            Credential credential = response.getCredential();
            if (credential instanceof CustomCredential
                    && GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL.equals(credential.getType())) {
                GoogleIdTokenCredential googleCredential =
                        GoogleIdTokenCredential.createFrom(((CustomCredential) credential).getData());
                String idToken = googleCredential.getIdToken();
                if (getActivity() == null) return;
                requireActivity().runOnUiThread(() -> viewModel.loginWithGoogle(idToken));
                return;
            }

            if (getActivity() == null) return;
            requireActivity().runOnUiThread(() ->
                    Toast.makeText(requireContext(), "Google credential không hợp lệ", Toast.LENGTH_SHORT).show());
        } catch (RuntimeException e) {
            if (getActivity() == null) return;
            requireActivity().runOnUiThread(() ->
                    Toast.makeText(requireContext(), "Không đọc được Google ID token", Toast.LENGTH_SHORT).show());
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (credentialExecutor != null) {
            credentialExecutor.shutdownNow();
        }
        binding = null;
    }
}

