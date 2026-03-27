package com.example.comicversev1.presentation.auth;

public class LoginUiState {
    private final boolean loading;
    private final boolean success;
    private final String errorMessage;

    private LoginUiState(boolean loading, boolean success, String errorMessage) {
        this.loading = loading;
        this.success = success;
        this.errorMessage = errorMessage;
    }

    public static LoginUiState idle() { return new LoginUiState(false, false, null); }
    public static LoginUiState loading() { return new LoginUiState(true, false, null); }
    public static LoginUiState success() { return new LoginUiState(false, true, null); }
    public static LoginUiState error(String message) { return new LoginUiState(false, false, message); }

    public boolean isLoading() { return loading; }
    public boolean isSuccess() { return success; }
    public String getErrorMessage() { return errorMessage; }
}

