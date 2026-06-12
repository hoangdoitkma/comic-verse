package com.example.comicversev1.data.repository;

import com.example.comicversev1.data.api.ApiService;
import com.example.comicversev1.data.model.PaymentConfirmRequest;
import com.example.comicversev1.data.model.PaymentRequest;
import com.example.comicversev1.data.model.PaymentResponse;
import com.example.comicversev1.data.model.VipPackageDTO;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.rxjava3.core.Single;

@Singleton
public class VipRepositoryImpl implements VipRepository {

    private final ApiService apiService;

    @Inject
    public VipRepositoryImpl(ApiService apiService) {
        this.apiService = apiService;
    }

    @Override
    public Single<List<VipPackageDTO>> getVipPackages() {
        return apiService.getVipPackages()
                .map(response -> {
                    if (response.isSuccess() && response.getData() != null) {
                        return response.getData();
                    }
                    return Collections.<VipPackageDTO>emptyList();
                });
    }

    @Override
    public Single<PaymentResponse> createVipOrder(int packageId) {
        return apiService.createVipOrder(new PaymentRequest(packageId, 0));
    }

    @Override
    public Single<PaymentResponse> confirmVipOrder(long orderCode) {
        return apiService.confirmVipOrder(new PaymentConfirmRequest(orderCode));
    }
}
