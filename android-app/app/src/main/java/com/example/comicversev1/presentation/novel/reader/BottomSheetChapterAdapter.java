package com.example.comicversev1.presentation.novel.reader;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.comicversev1.R;
import com.example.comicversev1.domain.entity.ChapterItem;

import java.util.ArrayList;
import java.util.List;

public class BottomSheetChapterAdapter extends RecyclerView.Adapter<BottomSheetChapterAdapter.ChapterViewHolder> implements Filterable {

    private final List<ChapterItem> mChapterListFull;
    private final List<ChapterItem> mChapterList;
    private final OnChapterClickListener mListener;

    public interface OnChapterClickListener {
        void onChapterClick(int chapterId);
    }

    public BottomSheetChapterAdapter(List<ChapterItem> chapters, OnChapterClickListener listener) {
        this.mChapterListFull = new ArrayList<>(chapters);
        this.mChapterList = new ArrayList<>(chapters);
        this.mListener = listener;
    }

    @NonNull
    @Override
    public ChapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_bottom_sheet_chapter, parent, false);
        return new ChapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChapterViewHolder holder, int position) {
        ChapterItem chapter = mChapterList.get(position);
        
        String chapTitle = chapter.getTitle();
        if (chapTitle != null && !chapTitle.toLowerCase().contains("chương") && !chapTitle.toLowerCase().contains("chap")) {
            chapTitle = "Chương " + chapTitle;
        }
        holder.tvTitle.setText(chapTitle);
        // Date placeholder
        holder.tvDate.setText("Vừa cập nhật");

        holder.itemView.setOnClickListener(v -> {
            if (mListener != null) {
                mListener.onChapterClick(chapter.getId());
            }
        });
    }

    @Override
    public int getItemCount() {
        return mChapterList.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                List<ChapterItem> filteredList = new ArrayList<>();
                if (constraint == null || constraint.length() == 0) {
                    filteredList.addAll(mChapterListFull);
                } else {
                    String filterPattern = constraint.toString().toLowerCase().trim();
                    for (ChapterItem item : mChapterListFull) {
                        if (item.getTitle() != null && item.getTitle().toLowerCase().contains(filterPattern)) {
                            filteredList.add(item);
                        }
                    }
                }
                FilterResults results = new FilterResults();
                results.values = filteredList;
                return results;
            }

            @Override
            @SuppressWarnings("unchecked")
            protected void publishResults(CharSequence constraint, FilterResults results) {
                mChapterList.clear();
                if (results.values != null) {
                    mChapterList.addAll((List<ChapterItem>) results.values);
                }
                notifyDataSetChanged();
            }
        };
    }

    static class ChapterViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle;
        TextView tvDate;

        public ChapterViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_item_chapter_title);
            tvDate = itemView.findViewById(R.id.tv_item_chapter_date);
        }
    }
}
