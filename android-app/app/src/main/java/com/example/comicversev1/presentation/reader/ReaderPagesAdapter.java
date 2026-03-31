package com.example.comicversev1.presentation.reader;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.comicversev1.R;

import java.util.ArrayList;
import java.util.List;

import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.load.DataSource;
import android.graphics.drawable.Drawable;
import android.content.res.Resources;
import androidx.annotation.Nullable;
import com.github.chrisbanes.photoview.PhotoView;

public class ReaderPagesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public interface OnImageLoadStateListener {
        void onImageLoading();
        void onImageLoadedOrError();
    }

    private OnImageLoadStateListener imageLoadStateListener;

    public void setOnImageLoadStateListener(OnImageLoadStateListener listener) {
        this.imageLoadStateListener = listener;
    }

    private static final int TYPE_PAGE = 0;
    private static final int TYPE_SEPARATOR = 1;

    // Wrapper item to cleanly manage metadata
    private static class ChapterImageItem {
        final int type;
        final String imageUrl;      // for TYPE_PAGE
        final String chapterTitle;  // for TYPE_SEPARATOR
        final int chapterId;
        final int imageIndexInChapter;    // Page index within its chapter (0-based)

        // Type Page constructor
        ChapterImageItem(String imageUrl, int chapterId, int imageIndexInChapter) {
            this.type = TYPE_PAGE;
            this.imageUrl = imageUrl;
            this.chapterTitle = null;
            this.chapterId = chapterId;
            this.imageIndexInChapter = imageIndexInChapter;
        }

        // Type Separator constructor
        ChapterImageItem(String chapterTitle, int chapterId) {
            this.type = TYPE_SEPARATOR;
            this.imageUrl = null;
            this.chapterTitle = chapterTitle;
            this.chapterId = chapterId;
            this.imageIndexInChapter = -1; // Separator does not represent a readable page index
        }
    }

    private final List<ChapterImageItem> items = new ArrayList<>();

    public ReaderPagesAdapter() {
        // GÓC CHÚ Ý KHÔNG SỬ DỤNG: StateRestorationPolicy.PREVENT_WHEN_EMPTY
        // Lý do: Nó sẽ ép Android chờ đến khi mảng items có dữ liệu rồi ghi đè State (thường là 0) của Hệ điều hành 
        // lên đè lệnh `scrollToPositionWithOffset` thủ công của ta. Gây Bug reset về trang 1 khi nhảy truyện!
    }

    /**
     * Submit first chapter with chapter info (Clears previous list)
     */
    public void submitChapter(int chapterId, String chapterTitle, List<String> images) {
        items.clear();
        if (images != null) {
            for (int i = 0; i < images.size(); i++) {
                items.add(new ChapterImageItem(images.get(i), chapterId, i));
            }
        }
        notifyDataSetChanged();
    }

    /**
     * Append a new chapter's images with a separator header
     */
    public void appendChapter(int chapterId, String chapterTitle, List<String> images) {
        if (images == null || images.isEmpty()) return;
        int insertStart = items.size();

        // Thêm separator tiêu đề chương
        items.add(new ChapterImageItem(chapterTitle, chapterId));

        // Thêm từng trang với `imageIndexInChapter` đi kèm (từ 0 đến n-1)
        for (int i = 0; i < images.size(); i++) {
            items.add(new ChapterImageItem(images.get(i), chapterId, i));
        }

        notifyItemRangeInserted(insertStart, images.size() + 1);
    }

    /**
     * Get the Chapter ID for a given absolute position
     */
    public int getChapterIdAtPosition(int position) {
        if (position >= 0 && position < items.size()) {
            return items.get(position).chapterId;
        }
        return -1;
    }

    /**
     * Lấy vị trí TRANG TƯƠNG ĐỐI (trong nột bộ 1 chapter) từ vị trí lưới tuyệt đối
     * Mục đích: Lưu State DB không bị cộng dồn list
     */
    public int getRelativePageIndex(int position) {
        if (position >= 0 && position < items.size()) {
            ChapterImageItem item = items.get(position);
            // Nếu vô tình lọt vào Separator, ta trả về 0 (trang đầu tiên của list con)
            return item.type == TYPE_PAGE ? item.imageIndexInChapter : 0;
        }
        return 0;
    }

    /**
     * Lấy VỊ TRÍ TUYỆT ĐỐI (trên lưới list) từ chapterId và page index tương đối
     * Mục đích: Khôi phục State App tự tìm điểm neo lưới list
     */
    public int getAbsolutePosition(int targetChapterId, int targetRelativePage) {
        int currentIndex = 0;
        Log.d("ReaderPagesAdapter", ">>> getAbsolutePosition Start: targetChapterId=" + targetChapterId + ", targetRelativePage=" + targetRelativePage);
        for (int i = 0; i < items.size(); i++) {
            ChapterImageItem item = items.get(i);
            if (item.chapterId == targetChapterId && item.type == TYPE_PAGE) {
                if (item.imageIndexInChapter == targetRelativePage) {
                    Log.d("ReaderPagesAdapter", ">>> FOUND! Absolute Index = " + i);
                    return i;
                }
            }
        }
        Log.e("ReaderPagesAdapter", ">>> NOT FOUND! Returned -1");
        return -1;
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position).type;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_SEPARATOR) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_chapter_separator, parent, false);
            return new SeparatorViewHolder(view);
        }
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_reader_page, parent, false);
        return new PageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChapterImageItem item = items.get(position);
        if (holder instanceof PageViewHolder) {
            PageViewHolder pageHolder = (PageViewHolder) holder;

            // Bơm chiều cao ảo (bằng nửa màn hình) để RecyclerView không bị dồn cục 0dp
            // Ngăn chặn việc RecyclerView tưởng tất cả các trang đều vừa màn hình và ghim anchor về Item 0!
            int screenHeight = Resources.getSystem().getDisplayMetrics().heightPixels;
            pageHolder.imageView.setMinimumHeight(screenHeight / 2);

            // Tối ưu Glide: 
            // 1. override SIZE_ORIGINAL chặn resize bậy bạ
            // 2. format PREFER_RGB_565 giảm 50% dung lượng RAM (2 bytes/pixel thay vì 4)
            Glide.with(pageHolder.itemView.getContext())
                    .load(item.imageUrl)
                    .override(com.bumptech.glide.request.target.Target.SIZE_ORIGINAL)
                    .format(com.bumptech.glide.load.DecodeFormat.PREFER_RGB_565)
                    .diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.ALL)
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            pageHolder.imageView.setMinimumHeight(0); // Trả lại tự do cho View
                            if (ReaderPagesAdapter.this.imageLoadStateListener != null) {
                                ReaderPagesAdapter.this.imageLoadStateListener.onImageLoadedOrError();
                            }
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            pageHolder.imageView.setMinimumHeight(0); // Trả lại tự do để adjustViewBounds hoạt động
                            if (ReaderPagesAdapter.this.imageLoadStateListener != null) {
                                ReaderPagesAdapter.this.imageLoadStateListener.onImageLoadedOrError();
                            }
                            return false;
                        }
                    })
                    .into(pageHolder.imageView);

            // Báo cho UI biết ảnh này bắt đầu load (có thể dùng để pause AutoScroll)
            if (this.imageLoadStateListener != null) {
                this.imageLoadStateListener.onImageLoading();
            }

            // Tải trước (Preload) 2 trang tiếp theo để lướt mượt không chờ mạng
            for (int i = 1; i <= 2; i++) {
                int nextPos = position + i;
                if (nextPos < items.size() && items.get(nextPos).type == TYPE_PAGE) {
                    Glide.with(pageHolder.itemView.getContext())
                         .load(items.get(nextPos).imageUrl)
                         .diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.ALL)
                         .preload();
                }
            }

            // Xử lý xung đột cảm ứng khi Zoom
            pageHolder.imageView.setOnTouchListener((v, event) -> {
                if (pageHolder.imageView.getScale() > 1.0f) {
                    v.getParent().requestDisallowInterceptTouchEvent(true);
                }
                return false;
            });

        } else if (holder instanceof SeparatorViewHolder) {
            ((SeparatorViewHolder) holder).titleText.setText(item.chapterTitle);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class PageViewHolder extends RecyclerView.ViewHolder {
        PhotoView imageView;

        PageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imgPage);
        }
    }

    static class SeparatorViewHolder extends RecyclerView.ViewHolder {
        TextView titleText;

        SeparatorViewHolder(@NonNull View itemView) {
            super(itemView);
            titleText = itemView.findViewById(R.id.txtChapterTitle);
        }
    }
}
