package com.example.comicversev1.data.repository;

import com.example.comicversev1.data.model.PaymentResponse;
import com.example.comicversev1.data.model.VipPackageDTO;

import java.util.List;

import io.reactivex.rxjava3.core.Single;

public interface VipRepository {
    Single<List<VipPackageDTO>> getVipPackages();

    Single<PaymentResponse> createVipOrder(int packageId);

    Single<PaymentResponse> confirmVipOrder(long orderCode);
}
