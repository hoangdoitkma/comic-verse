package com.example.comicversev1.presentation.discover;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.comicversev1.databinding.ItemSearchResultCardBinding;
import com.example.comicversev1.domain.entity.ComicEntity;

import java.util.ArrayList;
import java.util.List;

public class SearchResultAdapter extends RecyclerView.Adapter<SearchResultAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(ComicEntity item);
    }

    private final OnItemClickListener listener;
    private final List<ComicEntity> items = new ArrayList<>();

    public SearchResultAdapter(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void submitList(List<ComicEntity> data) {
        items.clear();
        if (data != null) {
            items.addAll(data);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemSearchResultCardBinding binding = ItemSearchResultCardBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ComicEntity item = items.get(position);
        holder.binding.textTitle.setText(item.getTitle());
        Glide.with(holder.binding.getRoot().getContext())
                .load(item.getCoverImage())
                .centerCrop()
                .into(holder.binding.imageCover);
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ItemSearchResultCardBinding binding;

        ViewHolder(ItemSearchResultCardBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
