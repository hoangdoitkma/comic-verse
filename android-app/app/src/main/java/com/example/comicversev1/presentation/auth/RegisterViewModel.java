package com.example.comicversev1.presentation.auth;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.comicversev1.domain.usecase.RegisterUseCase;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

@HiltViewModel
public class RegisterViewModel extends ViewModel {

    private final RegisterUseCase registerUseCase;
    private final CompositeDisposable disposables = new CompositeDisposable();

    private final MutableLiveData<RegisterUiState> _uiState = new MutableLiveData<>(RegisterUiState.idle());
    public LiveData<RegisterUiState> uiState() { return _uiState; }

    @Inject
    public RegisterViewModel(RegisterUseCase registerUseCase) {
        this.registerUseCase = registerUseCase;
    }

    public void register(String email, String password, String displayName) {
        _uiState.setValue(RegisterUiState.loading());
        disposables.add(
                registerUseCase.execute(email, password, displayName)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                response -> {
                                    if (response.isSuccess()) {
                                        _uiState.setValue(RegisterUiState.success());
                                    } else {
                                        _uiState.setValue(RegisterUiState.error(response.getMessage()));
                                    }
                                },
                                throwable -> _uiState.setValue(RegisterUiState.error(throwable.getMessage()))
                        )
        );
    }

    public void resetState() {
        _uiState.setValue(RegisterUiState.idle());
    }

    @Override
    protected void onCleared() {
        disposables.clear();
    }
}
