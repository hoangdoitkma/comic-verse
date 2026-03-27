package com.example.comicversev1.presentation.detail;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.comicversev1.R;

public class DetailChapterHeaderAdapter extends RecyclerView.Adapter<DetailChapterHeaderAdapter.ViewHolder> {

    private boolean isVisible = false;

    public void setVisible(boolean visible) {
        if (this.isVisible != visible) {
            this.isVisible = visible;
            notifyDataSetChanged();
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_detail_chapter_header, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {}

    @Override
    public int getItemCount() {
        return 1;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
