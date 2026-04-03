package com.example.comicversev1.presentation.vip;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.comicversev1.databinding.FragmentVipCenterBinding;

import java.util.ArrayList;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

import com.example.comicversev1.data.api.ApiService;
import com.example.comicversev1.data.model.PaymentRequest;
import android.content.Intent;
import android.util.Log;
import javax.inject.Inject;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

@AndroidEntryPoint
public class VipCenterFragment extends Fragment {

    @Inject
    ApiService apiService;

    private CompositeDisposable disposable = new CompositeDisposable();

    private FragmentVipCenterBinding binding;
    private VipPackageAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentVipCenterBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Setup Toolbar
        binding.toolbar.setNavigationOnClickListener(v -> NavHostFragment.findNavController(this).navigateUp());

        // Setup RecyclerView Packages
        setupRecyclerView();

        // Setup Register Button
        binding.btnRegisterVip.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Chức năng đăng ký VIP đang được phát triển!", Toast.LENGTH_SHORT).show();
        });
    }

    private void setupRecyclerView() {
        adapter = new VipPackageAdapter(new ArrayList<>(), vipPackage -> {
            
            // Client-side login check
            android.content.SharedPreferences prefs = requireActivity().getSharedPreferences(
                com.example.comicversev1.utils.Constants.PREF_AUTH, android.content.Context.MODE_PRIVATE);
            String token = prefs.getString(com.example.comicversev1.utils.Constants.KEY_ACCESS_TOKEN, "");
            
            if (token.isEmpty()) {
                Toast.makeText(requireContext(), "Yêu cầu đăng nhập trước khi thanh toán", Toast.LENGTH_SHORT).show();
                try {
                    NavHostFragment.findNavController(this).navigate(com.example.comicversev1.R.id.loginFragment);
                } catch (Exception e) {}
                return;
            }

            Toast.makeText(requireContext(), "Đang tạo đơn hàng...", Toast.LENGTH_SHORT).show();
            // Call API: now user ID is captured by backend from Auth Header, send 0 or 1
            disposable.add(
                apiService.createVipOrder(new PaymentRequest(vipPackage.id, 0)) 
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(response -> {
                        if (response.isSuccess() && response.getCheckoutUrl() != null) {
                            Intent intent = new Intent(requireContext(), CheckoutActivity.class);
                            intent.putExtra(CheckoutActivity.EXTRA_CHECKOUT_URL, response.getCheckoutUrl());
                            startActivity(intent);
                        } else {
                            Toast.makeText(requireContext(), "Lỗi khi tạo đơn hàng (Cần đăng nhập từ Khác)", Toast.LENGTH_SHORT).show();
                        }
                    }, error -> {
                        Log.e("VipCenterFragment", "Error calling createVipOrder", error);
                        Toast.makeText(requireContext(), "Yêu cầu đăng nhập trước khi thanh toán", Toast.LENGTH_SHORT).show();
                    })
            );
        });
        binding.rvVipPackages.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvVipPackages.setAdapter(adapter);

        loadVipPackages();
    }

    private void loadVipPackages() {
        disposable.add(
            apiService.getVipPackages()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(response -> {
                    if (response.isSuccess() && response.getData() != null) {
                        adapter.setItems(response.getData());
                    } else {
                        Toast.makeText(requireContext(), "Lỗi tải thông tin gói VIP", Toast.LENGTH_SHORT).show();
                    }
                }, error -> {
                    Log.e("VipCenterFragment", "Lỗi tải danh sách VIP", error);
                    Toast.makeText(requireContext(), "Lỗi kết nối máy chủ", Toast.LENGTH_SHORT).show();
                })
        );
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        disposable.clear();
        binding = null;
    }
}
