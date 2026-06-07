package com.example.comicversev1.presentation.discover;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.comicversev1.R;

public class SearchResultSectionAdapter extends RecyclerView.Adapter<SearchResultSectionAdapter.ViewHolder> {

    private final String title;
    private final RecyclerView.Adapter<?> childAdapter;
    private final RecyclerView.LayoutManager layoutManager;
    private final boolean showAlways;

    public SearchResultSectionAdapter(String title,
                                      RecyclerView.Adapter<?> childAdapter,
                                      RecyclerView.LayoutManager layoutManager,
                                      boolean showAlways) {
        this.title = title;
        this.childAdapter = childAdapter;
        this.layoutManager = layoutManager;
        this.showAlways = showAlways;
        this.childAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override public void onChanged() { notifyDataSetChanged(); }
            @Override public void onItemRangeInserted(int positionStart, int itemCount) { notifyDataSetChanged(); }
            @Override public void onItemRangeRemoved(int positionStart, int itemCount) { notifyDataSetChanged(); }
        });
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.layout_search_result_section, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.textTitle.setText(title);
        if (holder.recyclerShelf.getAdapter() == null) {
            holder.recyclerShelf.setLayoutManager(layoutManager);
            holder.recyclerShelf.setAdapter(childAdapter);
        }
    }

    @Override
    public int getItemCount() {
        return (showAlways || childAdapter.getItemCount() > 0) ? 1 : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView textTitle;
        final RecyclerView recyclerShelf;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            textTitle = itemView.findViewById(R.id.textTitle);
            recyclerShelf = itemView.findViewById(R.id.recyclerShelf);
        }
    }
}
