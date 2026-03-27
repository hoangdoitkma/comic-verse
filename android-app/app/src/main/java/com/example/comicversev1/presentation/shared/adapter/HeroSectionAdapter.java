package com.example.comicversev1.presentation.shared.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.example.comicversev1.R;

public class HeroSectionAdapter extends RecyclerView.Adapter<HeroSectionAdapter.ViewHolder> {

    private final RecyclerView.Adapter<?> childAdapter;

    public HeroSectionAdapter(RecyclerView.Adapter<?> childAdapter) {
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
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_landing_hero, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (holder.recyclerHero.getAdapter() == null) {
            holder.recyclerHero.setLayoutManager(new LinearLayoutManager(holder.itemView.getContext(), LinearLayoutManager.HORIZONTAL, false));
            holder.recyclerHero.setAdapter(childAdapter);
            
            // Only attach snap helper once
            if (holder.recyclerHero.getOnFlingListener() == null) {
                new LinearSnapHelper().attachToRecyclerView(holder.recyclerHero);
            }
        }
    }

    @Override
    public int getItemCount() {
        return childAdapter.getItemCount() > 0 ? 1 : 0; // Hide section if empty
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        RecyclerView recyclerHero;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            recyclerHero = itemView.findViewById(R.id.recyclerHero);
        }
    }
}
