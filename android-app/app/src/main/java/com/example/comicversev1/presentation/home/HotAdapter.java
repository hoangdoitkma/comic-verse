package com.example.comicversev1.presentation.home;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.comicversev1.databinding.ItemHomeHotBinding;
import com.example.comicversev1.domain.entity.HomeContent;

import java.util.ArrayList;
import java.util.List;

public class HotAdapter extends RecyclerView.Adapter<HotAdapter.HotViewHolder> {

    private final List<HomeContent.ComicCard> items = new ArrayList<>();

    public void submitList(List<HomeContent.ComicCard> data) {
        items.clear();
        if (data != null) {
            items.addAll(data);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public HotViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemHomeHotBinding binding = ItemHomeHotBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new HotViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull HotViewHolder holder, int position) {
        HomeContent.ComicCard item = items.get(position);
        holder.binding.textRank.setText(String.valueOf(position + 1));
        Glide.with(holder.binding.getRoot().getContext())
                .load(item.getCoverUrl())
                .centerCrop()
                .into(holder.binding.imageCover);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class HotViewHolder extends RecyclerView.ViewHolder {
        final ItemHomeHotBinding binding;

        HotViewHolder(ItemHomeHotBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}

