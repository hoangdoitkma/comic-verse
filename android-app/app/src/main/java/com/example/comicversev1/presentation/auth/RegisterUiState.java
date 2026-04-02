package com.example.comicversev1.presentation.auth;

public class RegisterUiState {
    private final boolean loading;
    private final boolean success;
    private final String errorMessage;

    private RegisterUiState(boolean loading, boolean success, String errorMessage) {
        this.loading = loading;
        this.success = success;
        this.errorMessage = errorMessage;
    }

    public static RegisterUiState idle() { return new RegisterUiState(false, false, null); }
    public static RegisterUiState loading() { return new RegisterUiState(true, false, null); }
    public static RegisterUiState success() { return new RegisterUiState(false, true, null); }
    public static RegisterUiState error(String message) { return new RegisterUiState(false, false, message); }

    public boolean isLoading() { return loading; }
    public boolean isSuccess() { return success; }
    public String getErrorMessage() { return errorMessage; }
}
