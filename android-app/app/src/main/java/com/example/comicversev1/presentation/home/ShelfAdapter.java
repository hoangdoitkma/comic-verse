package com.example.comicversev1.presentation.home;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.comicversev1.databinding.ItemHomeShelfBinding;
import com.example.comicversev1.domain.entity.HomeContent;

import java.util.ArrayList;
import java.util.List;

public class ShelfAdapter extends RecyclerView.Adapter<ShelfAdapter.ShelfViewHolder> {

    public interface OnComicClickListener {
        void onComicClick(HomeContent.ComicCard item);
    }

    private final OnComicClickListener listener;
    private final List<HomeContent.ComicCard> items = new ArrayList<>();

    public ShelfAdapter(OnComicClickListener listener) {
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
    public ShelfViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemHomeShelfBinding binding = ItemHomeShelfBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ShelfViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ShelfViewHolder holder, int position) {
        HomeContent.ComicCard item = items.get(position);
        holder.binding.textTitle.setText(item.getTitle());
        Glide.with(holder.binding.getRoot().getContext())
                .load(item.getCoverUrl())
                .centerCrop()
                .into(holder.binding.imageCover);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onComicClick(item);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ShelfViewHolder extends RecyclerView.ViewHolder {
        final ItemHomeShelfBinding binding;

        ShelfViewHolder(ItemHomeShelfBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}

