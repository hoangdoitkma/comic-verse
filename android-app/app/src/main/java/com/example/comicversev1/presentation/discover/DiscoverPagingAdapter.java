package com.example.comicversev1.presentation.discover;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.paging.PagingDataAdapter;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.comicversev1.R;
import com.example.comicversev1.domain.entity.ComicEntity;

public class DiscoverPagingAdapter extends PagingDataAdapter<ComicEntity, DiscoverPagingAdapter.ComicViewHolder> {

    public interface OnComicClickListener {
        void onComicClick(ComicEntity comic);
    }

    private final OnComicClickListener listener;

    public DiscoverPagingAdapter(OnComicClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    @NonNull
    @Override
    public ComicViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comic_card, parent, false);
        return new ComicViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ComicViewHolder holder, int position) {
        ComicEntity item = getItem(position);
        if (item == null) return;
        holder.bind(item, listener);
    }

    static class ComicViewHolder extends RecyclerView.ViewHolder {
        private final TextView title;
        private final ImageView cover;
        private final ImageView vipIcon;

        ComicViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.txtComicTitle);
            cover = itemView.findViewById(R.id.imgComicCover);
            vipIcon = itemView.findViewById(R.id.ivVipIcon);
        }

        void bind(ComicEntity entity, OnComicClickListener listener) {
            title.setText(entity.getTitle());
            Glide.with(cover.getContext()).load(entity.getCoverImage()).into(cover);
            if ("VIP".equals(entity.getAccessType())) {
                vipIcon.setVisibility(View.VISIBLE);
            } else {
                vipIcon.setVisibility(View.GONE);
            }
            itemView.setOnClickListener(v -> listener.onComicClick(entity));
        }
    }

    private static final DiffUtil.ItemCallback<ComicEntity> DIFF_CALLBACK = new DiffUtil.ItemCallback<ComicEntity>() {
        @Override
        public boolean areItemsTheSame(@NonNull ComicEntity oldItem, @NonNull ComicEntity newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull ComicEntity oldItem, @NonNull ComicEntity newItem) {
            return oldItem.getTitle().equals(newItem.getTitle()) &&
                    oldItem.getCoverImage().equals(newItem.getCoverImage());
        }
    };
}

