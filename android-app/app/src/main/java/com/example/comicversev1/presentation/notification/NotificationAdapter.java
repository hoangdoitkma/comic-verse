package com.example.comicversev1.presentation.notification;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.comicversev1.R;
import com.example.comicversev1.data.model.NotificationDTO;
import com.example.comicversev1.databinding.ItemNotificationBinding;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotifViewHolder> {

    public interface OnNotificationClickListener {
        void onNotificationClick(NotificationDTO notification, int position);
    }

    private final List<NotificationDTO> items = new ArrayList<>();
    private OnNotificationClickListener listener;

    public void setOnNotificationClickListener(OnNotificationClickListener listener) {
        this.listener = listener;
    }

    public void submitList(List<NotificationDTO> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    public void markAsRead(int position) {
        if (position >= 0 && position < items.size()) {
            items.get(position).isRead = true;
            notifyItemChanged(position);
        }
    }

    public void markAllAsRead() {
        for (NotificationDTO item : items) {
            item.isRead = true;
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public NotifViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemNotificationBinding binding = ItemNotificationBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new NotifViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull NotifViewHolder holder, int position) {
        NotificationDTO item = items.get(position);
        holder.bind(item, position);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class NotifViewHolder extends RecyclerView.ViewHolder {
        private final ItemNotificationBinding binding;

        NotifViewHolder(ItemNotificationBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(NotificationDTO item, int position) {
            binding.textNotifTitle.setText(item.title != null ? item.title : "");
            binding.textNotifMessage.setText(item.message != null ? item.message : "");
            binding.textNotifTime.setText(formatRelativeTime(item.createdAt));

            // Set icon based on type
            int iconRes = getIconForType(item.type);
            binding.imageNotifIcon.setImageResource(iconRes);

            // Unread indicator
            boolean unread = item.isRead == null || !item.isRead;
            binding.dotUnread.setVisibility(unread ? View.VISIBLE : View.GONE);

            // Background for unread items
            if (unread) {
                binding.notificationItemRoot.setBackgroundResource(R.drawable.bg_notification_unread);
            } else {
                binding.notificationItemRoot.setBackgroundResource(android.R.color.transparent);
            }

            // Title style
            binding.textNotifTitle.setTextColor(
                    unread ? 0xFFFFFFFF : 0xFFB0B0B0
            );

            // Click
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onNotificationClick(item, position);
                }
            });
        }
    }

    private int getIconForType(String type) {
        if (type == null) return R.drawable.ic_notif_system;
        switch (type) {
            case "NEW_CHAPTER":
            case "UPDATE":
                return R.drawable.ic_notif_chapter;
            case "COMMENT_REPLY":
                return R.drawable.ic_notif_comment;
            case "PROMOTION":
            case "APPROVED":
                return R.drawable.ic_notif_promotion;
            case "REJECTED":
                return R.drawable.ic_notif_system;
            case "SYSTEM":
            default:
                return R.drawable.ic_notif_system;
        }
    }

    private String formatRelativeTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.isEmpty()) return "";
        try {
            // Handle ISO datetime with or without milliseconds
            LocalDateTime time;
            try {
                time = LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            } catch (Exception e) {
                // Try with custom format
                time = LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
            }
            
            Duration diff = Duration.between(time, LocalDateTime.now());
            long minutes = diff.toMinutes();
            
            if (minutes < 1) return "Vừa xong";
            if (minutes < 60) return minutes + " phút trước";
            
            long hours = diff.toHours();
            if (hours < 24) return hours + " giờ trước";
            
            long days = diff.toDays();
            if (days < 7) return days + " ngày trước";
            if (days < 30) return (days / 7) + " tuần trước";
            if (days < 365) return (days / 30) + " tháng trước";
            
            return (days / 365) + " năm trước";
        } catch (Exception e) {
            return dateTimeStr;
        }
    }
}
