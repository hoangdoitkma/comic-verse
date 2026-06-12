package com.example.comicversev1.presentation.auth;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.comicversev1.domain.usecase.LoginUseCase;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

@HiltViewModel
public class LoginViewModel extends ViewModel {

    private final LoginUseCase loginUseCase;
    private final CompositeDisposable disposables = new CompositeDisposable();

    private final MutableLiveData<LoginUiState> _uiState = new MutableLiveData<>(LoginUiState.idle());
    public LiveData<LoginUiState> uiState() { return _uiState; }

    @Inject
    public LoginViewModel(LoginUseCase loginUseCase) {
        this.loginUseCase = loginUseCase;
    }

    public void login(String email, String password) {
        _uiState.setValue(LoginUiState.loading());
        disposables.add(
                loginUseCase.execute(email, password)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                response -> _uiState.setValue(LoginUiState.success()),
                                throwable -> _uiState.setValue(LoginUiState.error(throwable.getMessage()))
                        )
        );
    }

    public void loginWithGoogle(String idToken) {
        _uiState.setValue(LoginUiState.loading());
        disposables.add(
                loginUseCase.executeGoogle(idToken)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                response -> _uiState.setValue(LoginUiState.success()),
                                throwable -> _uiState.setValue(LoginUiState.error(throwable.getMessage()))
                        )
        );
    }

    @Override
    protected void onCleared() {
        disposables.clear();
    }
}

