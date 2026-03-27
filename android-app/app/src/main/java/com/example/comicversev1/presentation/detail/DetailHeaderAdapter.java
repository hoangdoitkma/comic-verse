package com.example.comicversev1.presentation.detail;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.util.TypedValue;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.comicversev1.R;
import com.example.comicversev1.domain.entity.ComicDetailEntity;

public class DetailHeaderAdapter extends RecyclerView.Adapter<DetailHeaderAdapter.ViewHolder> {

    private ComicDetailEntity comic;

    public void setComic(ComicDetailEntity comic) {
        this.comic = comic;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_detail_header, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.layoutTags.removeAllViews();
        
        if (comic != null) {
            holder.txtTitle.setText(comic.getTitle() != null ? comic.getTitle() : "N/A");
            holder.txtSummary.setText(comic.getAiSummary() != null && !comic.getAiSummary().isEmpty() ? comic.getAiSummary() : "Không có thông tin nội dung.");
            Glide.with(holder.itemView.getContext())
                 .load(comic.getCoverImage())
                 .error(android.R.drawable.ic_menu_gallery)
                 .into(holder.imgCover);
            
            // Giả lập mock tags hoặc N/A theo user 
            // TODO: Ở API chuẩn, comic.getTags() sẽ trả về mảng tag. Tạm thời show N/A nếu chưa map được Array.
            addTag(holder.layoutTags, "N/A");
            
        } else {
            holder.txtTitle.setText("Đang tải dữ liệu...");
            holder.txtSummary.setText("Đang kết nối để lấy thông tin...");
            addTag(holder.layoutTags, "N/A");
            holder.imgCover.setImageResource(android.R.drawable.ic_menu_gallery);
        }
    }
    
    private void addTag(LinearLayout container, String text) {
        TextView tv = new TextView(container.getContext());
        tv.setText(text);
        tv.setTextAppearance(R.style.TagChip);
        tv.setBackgroundResource(R.drawable.bg_ad_chip); // Reusing the same rounded bg
        
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT, 
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMarginEnd(16);
        tv.setLayoutParams(params);
        container.addView(tv);
    }

    @Override
    public int getItemCount() {
        return 1;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgCover;
        TextView txtTitle;
        TextView txtSummary;
        LinearLayout layoutTags;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgCover = itemView.findViewById(R.id.imgCover);
            txtTitle = itemView.findViewById(R.id.txtTitle);
            txtSummary = itemView.findViewById(R.id.txtSummary);
            layoutTags = itemView.findViewById(R.id.layoutTags);
        }
    }
}
