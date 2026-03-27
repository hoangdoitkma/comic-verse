package com.example.comicversev1.presentation.shared.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.example.comicversev1.R;

public class ContinueReadingSectionAdapter extends RecyclerView.Adapter<ContinueReadingSectionAdapter.ViewHolder> {

    private String subtitle = "";
    private int progress = 0;
    private boolean isVisible = false;

    public void setContinueData(String subtitle, int progress) {
        this.subtitle = subtitle;
        this.progress = progress;
        this.isVisible = (subtitle != null && !subtitle.isEmpty());
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_landing_continue, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.textSubtitle.setText(subtitle);
        holder.progressBar.setProgress(progress);
    }

    @Override
    public int getItemCount() {
        return isVisible ? 1 : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textSubtitle;
        LinearProgressIndicator progressBar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textSubtitle = itemView.findViewById(R.id.textContinueSubtitle);
            progressBar = itemView.findViewById(R.id.progressContinue);
        }
    }
}
