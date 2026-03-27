package com.example.comicversev1.presentation.home;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.comicversev1.databinding.ItemHomeLargeBinding;
import com.example.comicversev1.domain.entity.HomeContent;

import java.util.ArrayList;
import java.util.List;

public class LargeCardAdapter extends RecyclerView.Adapter<LargeCardAdapter.LargeCardViewHolder> {

    private final List<HomeContent.ComicCard> items = new ArrayList<>();
    private boolean showHotBadge;

    public LargeCardAdapter(boolean showHotBadge) {
        this.showHotBadge = showHotBadge;
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
    public LargeCardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemHomeLargeBinding binding = ItemHomeLargeBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new LargeCardViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull LargeCardViewHolder holder, int position) {
        HomeContent.ComicCard item = items.get(position);
        holder.binding.textTitle.setText(item.getTitle());
        holder.binding.textChapter.setText(item.getChapterLabel());
        holder.binding.textTime.setText(item.getTimeLabel());
        holder.binding.textHotBadge.setVisibility(showHotBadge ? View.VISIBLE : View.GONE);
        Glide.with(holder.binding.getRoot().getContext())
                .load(item.getCoverUrl())
                .centerCrop()
                .into(holder.binding.imageCover);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class LargeCardViewHolder extends RecyclerView.ViewHolder {
        final ItemHomeLargeBinding binding;

        LargeCardViewHolder(ItemHomeLargeBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}

