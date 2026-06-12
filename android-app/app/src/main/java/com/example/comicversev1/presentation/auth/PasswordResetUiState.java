package com.example.comicversev1.presentation.auth;

public class PasswordResetUiState {
    private final boolean loading;
    private final boolean codeSent;
    private final boolean resetSuccess;
    private final String message;
    private final String errorMessage;

    private PasswordResetUiState(boolean loading, boolean codeSent, boolean resetSuccess, String message, String errorMessage) {
        this.loading = loading;
        this.codeSent = codeSent;
        this.resetSuccess = resetSuccess;
        this.message = message;
        this.errorMessage = errorMessage;
    }

    public static PasswordResetUiState idle() {
        return new PasswordResetUiState(false, false, false, null, null);
    }

    public static PasswordResetUiState loading(boolean codeSent) {
        return new PasswordResetUiState(true, codeSent, false, null, null);
    }

    public static PasswordResetUiState codeSent(String message) {
        return new PasswordResetUiState(false, true, false, message, null);
    }

    public static PasswordResetUiState resetSuccess(String message) {
        return new PasswordResetUiState(false, true, true, message, null);
    }

    public static PasswordResetUiState error(boolean codeSent, String message) {
        return new PasswordResetUiState(false, codeSent, false, null, message);
    }

    public boolean isLoading() { return loading; }
    public boolean isCodeSent() { return codeSent; }
    public boolean isResetSuccess() { return resetSuccess; }
    public String getMessage() { return message; }
    public String getErrorMessage() { return errorMessage; }
}
