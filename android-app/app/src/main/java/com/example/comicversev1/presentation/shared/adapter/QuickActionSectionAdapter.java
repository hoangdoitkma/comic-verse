package com.example.comicversev1.presentation.shared.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.comicversev1.R;

public class QuickActionSectionAdapter extends RecyclerView.Adapter<QuickActionSectionAdapter.ViewHolder> {

    private final RecyclerView.Adapter<?> childAdapter;

    public QuickActionSectionAdapter(RecyclerView.Adapter<?> childAdapter) {
        this.childAdapter = childAdapter;
        this.childAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override public void onChanged() { notifyDataSetChanged(); }
            @Override public void onItemRangeInserted(int p, int c) { notifyDataSetChanged(); }
            @Override public void onItemRangeRemoved(int p, int c) { notifyDataSetChanged(); }
        });
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_landing_quick_actions, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (holder.recyclerQuickActions.getAdapter() == null) {
            holder.recyclerQuickActions.setLayoutManager(new GridLayoutManager(holder.itemView.getContext(), 3));
            holder.recyclerQuickActions.setAdapter(childAdapter);
        }
    }

    @Override
    public int getItemCount() {
        return childAdapter.getItemCount() > 0 ? 1 : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        RecyclerView recyclerQuickActions;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            recyclerQuickActions = itemView.findViewById(R.id.recyclerQuickActions);
        }
    }
}
