package com.example.comicversev1.presentation.auth;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.comicversev1.domain.usecase.RequestPasswordResetUseCase;
import com.example.comicversev1.domain.usecase.ResetPasswordUseCase;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

@HiltViewModel
public class ForgotPasswordViewModel extends ViewModel {

    private final RequestPasswordResetUseCase requestPasswordResetUseCase;
    private final ResetPasswordUseCase resetPasswordUseCase;
    private final CompositeDisposable disposables = new CompositeDisposable();
    private boolean codeRequested;

    private final MutableLiveData<PasswordResetUiState> _uiState =
            new MutableLiveData<>(PasswordResetUiState.idle());
    public LiveData<PasswordResetUiState> uiState() { return _uiState; }

    @Inject
    public ForgotPasswordViewModel(RequestPasswordResetUseCase requestPasswordResetUseCase,
                                   ResetPasswordUseCase resetPasswordUseCase) {
        this.requestPasswordResetUseCase = requestPasswordResetUseCase;
        this.resetPasswordUseCase = resetPasswordUseCase;
    }

    public void requestCode(String email) {
        _uiState.setValue(PasswordResetUiState.loading(codeRequested));
        disposables.add(requestPasswordResetUseCase.execute(email)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        response -> {
                            codeRequested = true;
                            String message = response.getMessage() != null
                                    ? response.getMessage()
                                    : "Nếu email tồn tại, mã đặt lại đã được gửi.";
                            _uiState.setValue(PasswordResetUiState.codeSent(message));
                        },
                        throwable -> _uiState.setValue(PasswordResetUiState.error(codeRequested, throwable.getMessage()))
                ));
    }

    public void resetPassword(String email, String otp, String newPassword) {
        _uiState.setValue(PasswordResetUiState.loading(true));
        disposables.add(resetPasswordUseCase.execute(email, otp, newPassword)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        response -> _uiState.setValue(PasswordResetUiState.resetSuccess("Đổi mật khẩu thành công.")),
                        throwable -> _uiState.setValue(PasswordResetUiState.error(true, throwable.getMessage()))
                ));
    }

    @Override
    protected void onCleared() {
        disposables.clear();
    }
}
