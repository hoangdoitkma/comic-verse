package com.example.comicversev1.presentation.detail;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.comicversev1.R;
import com.example.comicversev1.domain.entity.ChapterItem;

import java.util.ArrayList;
import java.util.List;

public class ChapterListAdapter extends RecyclerView.Adapter<ChapterListAdapter.ChapterViewHolder> {

    public interface OnChapterClickListener {
        void onChapterClick(ChapterItem item);
    }

    private final List<ChapterItem> items = new ArrayList<>();
    private final OnChapterClickListener listener;

    public ChapterListAdapter(OnChapterClickListener listener) {
        this.listener = listener;
    }

    public void submitList(List<ChapterItem> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ChapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_detail_chapter_row, parent, false);
        return new ChapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChapterViewHolder holder, int position) {
        ChapterItem item = items.get(position);
        holder.bind(item, listener);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ChapterViewHolder extends RecyclerView.ViewHolder {
        private final TextView title;
        private final TextView date;
        private final TextView index;
        private final TextView access;

        ChapterViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.txtChapterTitle);
            date = itemView.findViewById(R.id.txtChapterDate);
            index = itemView.findViewById(R.id.txtChapterIndex);
            access = itemView.findViewById(R.id.txtChapterAccess);
        }

        void bind(ChapterItem item, OnChapterClickListener listener) {
            title.setText(formatChapterTitle(item.getTitle()));
            index.setText(String.valueOf(getBindingAdapterPosition() + 1));
            // Mocking date format since it doesn't exist in entity yet. Normally uses item.getCreatedAt()
            date.setText("Vừa cập nhật");
            boolean isVip = item.getAccessType() != null && "VIP".equalsIgnoreCase(item.getAccessType());
            access.setText(isVip ? "VIP" : "FREE");
            access.setTextColor(itemView.getContext().getColor(isVip ? R.color.icon_yellow : R.color.icon_green));
            access.setBackgroundResource(isVip ? R.drawable.bg_detail_access_vip : R.drawable.bg_detail_access_free);
            itemView.setOnClickListener(v -> listener.onChapterClick(item));
        }

        private String formatChapterTitle(String rawTitle) {
            if (rawTitle == null || rawTitle.trim().isEmpty()) {
                return "Chương ?";
            }
            String title = rawTitle.trim();
            String lower = title.toLowerCase();
            if (lower.contains("chương") || lower.contains("chap")) {
                return title;
            }
            return "Chương " + title;
        }
    }
}
