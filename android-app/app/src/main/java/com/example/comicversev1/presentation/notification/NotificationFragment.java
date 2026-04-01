package com.example.comicversev1.presentation.notification;

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

import com.example.comicversev1.R;
import com.example.comicversev1.data.api.ApiService;
import com.example.comicversev1.data.model.NotificationDTO;
import com.example.comicversev1.databinding.FragmentNotificationBinding;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

@AndroidEntryPoint
public class NotificationFragment extends Fragment {

    @Inject
    ApiService apiService;

    private FragmentNotificationBinding binding;
    private NotificationAdapter adapter;
    private final CompositeDisposable disposables = new CompositeDisposable();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentNotificationBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupToolbar();
        setupRecyclerView();
        loadNotifications();
    }

    private void setupToolbar() {
        binding.toolbarNotification.setNavigationOnClickListener(v -> {
            NavHostFragment.findNavController(this).navigateUp();
        });

        binding.textReadAll.setOnClickListener(v -> markAllAsRead());
    }

    private void setupRecyclerView() {
        adapter = new NotificationAdapter();
        adapter.setOnNotificationClickListener((notification, position) -> {
            // Mark as read
            if (notification.isRead == null || !notification.isRead) {
                markAsRead(notification.id, position);
            }
        });

        binding.recyclerNotifications.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerNotifications.setAdapter(adapter);

        // Pull to refresh
        binding.swipeRefresh.setColorSchemeColors(
                getResources().getColor(android.R.color.holo_blue_light, null)
        );
        binding.swipeRefresh.setProgressBackgroundColorSchemeColor(
                getResources().getColor(android.R.color.black, null)
        );
        binding.swipeRefresh.setOnRefreshListener(this::loadNotifications);
    }

    private void loadNotifications() {
        binding.swipeRefresh.setRefreshing(true);
        disposables.add(
                apiService.getNotifications()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(response -> {
                            binding.swipeRefresh.setRefreshing(false);
                            if (response.isSuccess() && response.getData() != null) {
                                List<NotificationDTO> notifications = response.getData();
                                adapter.submitList(notifications);
                                updateEmptyState(notifications.isEmpty());
                            } else {
                                updateEmptyState(true);
                            }
                        }, error -> {
                            binding.swipeRefresh.setRefreshing(false);
                            updateEmptyState(true);
                            Toast.makeText(requireContext(),
                                    "Không thể tải thông báo. Vui lòng thử lại.",
                                    Toast.LENGTH_SHORT).show();
                        })
        );
    }

    private void markAsRead(int notificationId, int position) {
        disposables.add(
                apiService.markNotificationAsRead(notificationId)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(() -> {
                            adapter.markAsRead(position);
                        }, error -> {
                            // Silently ignore read mark errors
                        })
        );
    }

    private void markAllAsRead() {
        disposables.add(
                apiService.markAllNotificationsAsRead()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(() -> {
                            adapter.markAllAsRead();
                            Toast.makeText(requireContext(),
                                    "Đã đánh dấu tất cả đã đọc",
                                    Toast.LENGTH_SHORT).show();
                        }, error -> {
                            Toast.makeText(requireContext(),
                                    "Không thể cập nhật. Vui lòng thử lại.",
                                    Toast.LENGTH_SHORT).show();
                        })
        );
    }

    private void updateEmptyState(boolean isEmpty) {
        binding.layoutEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        binding.recyclerNotifications.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onDestroyView() {
        disposables.clear();
        binding = null;
        super.onDestroyView();
    }
}
