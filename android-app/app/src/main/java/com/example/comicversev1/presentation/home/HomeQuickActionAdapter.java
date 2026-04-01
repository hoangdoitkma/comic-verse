package com.example.comicversev1.presentation.home;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.comicversev1.databinding.ItemHomeQuickActionBinding;
import com.example.comicversev1.domain.entity.HomeContent;

import java.util.ArrayList;
import java.util.List;

public class HomeQuickActionAdapter extends RecyclerView.Adapter<HomeQuickActionAdapter.ActionViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(HomeContent.QuickAction action);
    }

    private final List<HomeContent.QuickAction> items = new ArrayList<>();
    private OnItemClickListener listener;

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void submitList(List<HomeContent.QuickAction> actions) {
        items.clear();
        if (actions != null) {
            items.addAll(actions);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ActionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemHomeQuickActionBinding binding = ItemHomeQuickActionBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ActionViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ActionViewHolder holder, int position) {
        HomeContent.QuickAction action = items.get(position);
        holder.binding.textTitle.setText(action.getTitle());
        holder.binding.textSubtitle.setText(action.getSubtitle());
        holder.binding.imageIcon.setImageResource(action.getIconRes());
        
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(action);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ActionViewHolder extends RecyclerView.ViewHolder {
        final ItemHomeQuickActionBinding binding;

        ActionViewHolder(ItemHomeQuickActionBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}

