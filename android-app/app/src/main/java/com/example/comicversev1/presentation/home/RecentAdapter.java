package com.example.comicversev1.presentation.home;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.comicversev1.databinding.ItemHomeRecentBinding;
import com.example.comicversev1.domain.entity.HomeContent;

import java.util.ArrayList;
import java.util.List;

public class RecentAdapter extends RecyclerView.Adapter<RecentAdapter.RecentViewHolder> {

    public interface OnComicClickListener {
        void onComicClick(HomeContent.ComicCard item);
    }

    private final List<HomeContent.ComicCard> items = new ArrayList<>();
    private OnComicClickListener listener;

    public void setListener(OnComicClickListener listener) {
        this.listener = listener;
    }

    public void submitList(List<HomeContent.ComicCard> data) {
        items.clear();
        if (data != null) {
            items.addAll(data);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemHomeRecentBinding binding = ItemHomeRecentBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new RecentViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull RecentViewHolder holder, int position) {
        HomeContent.ComicCard item = items.get(position);
        holder.binding.textTitle.setText(item.getTitle());
        holder.binding.textChapter.setText(item.getChapterLabel());
        holder.binding.progressCircle.setProgress(item.getProgress());
        holder.binding.textProgress.setText(item.getProgress() + "%");

        // Format likes/views
        holder.binding.textLikes.setText(formatCompact(item.getLikes()));
        holder.binding.textViews.setText(formatCompact(item.getViews()));

        // Load cover
        Glide.with(holder.binding.getRoot().getContext())
                .load(item.getCoverUrl())
                .centerCrop()
                .into(holder.binding.imageCover);

        if ("VIP".equals(item.getAccessType())) {
            holder.binding.ivVipIcon.setVisibility(android.view.View.VISIBLE);
        } else {
            holder.binding.ivVipIcon.setVisibility(android.view.View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onComicClick(item);
        });
    }

    /**
     * Format numbers compactly: 1500 → "1,5k", 1200000 → "1,2M"
     */
    private String formatCompact(long value) {
        if (value <= 0) return "N/A";
        if (value >= 1_000_000) {
            return String.format("%.1fM", value / 1_000_000.0).replace(".", ",");
        } else if (value >= 1_000) {
            return String.format("%.1fk", value / 1_000.0).replace(".", ",");
        }
        return String.valueOf(value);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class RecentViewHolder extends RecyclerView.ViewHolder {
        final ItemHomeRecentBinding binding;

        RecentViewHolder(ItemHomeRecentBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
