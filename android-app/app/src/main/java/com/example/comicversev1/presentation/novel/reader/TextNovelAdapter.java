package com.example.comicversev1.presentation.novel.reader;

import android.graphics.Color;
import android.text.Html;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.comicversev1.R;

import java.util.ArrayList;
import java.util.List;

public class TextNovelAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    // Config default values
    private float mTextSizeSp = 16f;
    private int mTextColor = Color.BLACK;

    private final List<TextNovelItem> mItems = new ArrayList<>();
    private OnPaywallUnlockClickListener mUnlockClickListener;
    private OnParagraphLongClickListener mParagraphLongClickListener;

    public interface OnPaywallUnlockClickListener {
        void onUnlockClicked(int chapterId);
    }

    public interface OnParagraphLongClickListener {
        void onLongClick(int chapterId);
    }

    public void setOnPaywallUnlockClickListener(OnPaywallUnlockClickListener listener) {
        this.mUnlockClickListener = listener;
    }

    public void setOnParagraphLongClickListener(OnParagraphLongClickListener listener) {
        this.mParagraphLongClickListener = listener;
    }

    public void submitList(List<TextNovelItem> newItems) {
        mItems.clear();
        if (newItems != null) {
            mItems.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    public void clearItems() {
        mItems.clear();
        notifyDataSetChanged();
    }

    public void appendItems(List<TextNovelItem> newItems) {
        if (newItems != null && !newItems.isEmpty()) {
            int startPos = mItems.size();
            mItems.addAll(newItems);
            notifyItemRangeInserted(startPos, newItems.size());
        }
    }

    public void updateSettings(float textSizeSp, int textColor) {
        if (this.mTextSizeSp == textSizeSp && this.mTextColor == textColor) {
            return;
        }
        this.mTextSizeSp = textSizeSp;
        this.mTextColor = textColor;
        
        // Notify items that need appearance update (Paragraphs & Title)
        for (int i = 0; i < mItems.size(); i++) {
            TextNovelItem item = mItems.get(i);
            if (item.getViewType() == TextNovelItem.TYPE_PARAGRAPH || item.getViewType() == TextNovelItem.TYPE_TITLE) {
                notifyItemChanged(i);
            }
        }
    }

    // Lấy Item tại vị trí pos
    public TextNovelItem getItem(int position) {
        if (position >= 0 && position < mItems.size()) {
            return mItems.get(position);
        }
        return null;
    }

    public void removeLoadingItem() {
        if (!mItems.isEmpty()) {
            int lastIndex = mItems.size() - 1;
            if (mItems.get(lastIndex).getViewType() == TextNovelItem.TYPE_LOADING) {
                mItems.remove(lastIndex);
                notifyItemRemoved(lastIndex);
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        return mItems.get(position).getViewType();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch (viewType) {
            case TextNovelItem.TYPE_TITLE: {
                View view = inflater.inflate(R.layout.item_text_novel_title, parent, false);
                return new TitleViewHolder(view);
            }
            case TextNovelItem.TYPE_PARAGRAPH: {
                View view = inflater.inflate(R.layout.item_text_novel_paragraph, parent, false);
                return new ParagraphViewHolder(view);
            }
            case TextNovelItem.TYPE_DIVIDER: {
                View view = inflater.inflate(R.layout.item_text_novel_divider, parent, false);
                return new DividerViewHolder(view);
            }
            case TextNovelItem.TYPE_PAYWALL: {
                View view = inflater.inflate(R.layout.item_text_novel_paywall, parent, false);
                return new PaywallViewHolder(view);
            }
            case TextNovelItem.TYPE_LOADING: {
                View view = inflater.inflate(R.layout.item_text_novel_loading, parent, false);
                return new LoadingViewHolder(view);
            }
            default:
                throw new IllegalArgumentException("Invalid view type");
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        TextNovelItem item = mItems.get(position);

        if (holder instanceof TitleViewHolder && item instanceof TextNovelItem.TitleItem) {
            ((TitleViewHolder) holder).bind((TextNovelItem.TitleItem) item, mTextSizeSp, mTextColor);
        } else if (holder instanceof ParagraphViewHolder && item instanceof TextNovelItem.ParagraphItem) {
            ((ParagraphViewHolder) holder).bind((TextNovelItem.ParagraphItem) item, mTextSizeSp, mTextColor, mParagraphLongClickListener);
        } else if (holder instanceof PaywallViewHolder && item instanceof TextNovelItem.PaywallItem) {
            ((PaywallViewHolder) holder).bind((TextNovelItem.PaywallItem) item, mUnlockClickListener);
        }
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    // ViewHolders
    static class TitleViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvTitle;

        public TitleViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvChapterTitle);
        }

        public void bind(TextNovelItem.TitleItem item, float textSize, int textColor) {
            tvTitle.setText(item.getTitle());
            tvTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize + 8f); // Tên chương to hơn 8sp
            tvTitle.setTextColor(textColor);
        }
    }

    static class ParagraphViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvParagraph;

        public ParagraphViewHolder(@NonNull View itemView) {
            super(itemView);
            tvParagraph = itemView.findViewById(R.id.tvParagraph);
        }

        public void bind(TextNovelItem.ParagraphItem item, float textSize, int textColor, OnParagraphLongClickListener listener) {
            // Hiển thị dạng HTML để lỡ nội dung S3 có thẻ <b>, <i>, <br>
            tvParagraph.setText(Html.fromHtml(item.getContent(), Html.FROM_HTML_MODE_COMPACT));
            tvParagraph.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);
            tvParagraph.setTextColor(textColor);
            
            tvParagraph.setOnLongClickListener(v -> {
                if (listener != null) {
                    listener.onLongClick(item.getChapterId());
                    return true;
                }
                return false;
            });
        }
    }

    static class DividerViewHolder extends RecyclerView.ViewHolder {
        public DividerViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    static class PaywallViewHolder extends RecyclerView.ViewHolder {
        private final Button btnUnlock;
        private int currentChapterId;

        public PaywallViewHolder(@NonNull View itemView) {
            super(itemView);
            btnUnlock = itemView.findViewById(R.id.btnUnlock);
        }

        public void bind(TextNovelItem.PaywallItem item, OnPaywallUnlockClickListener listener) {
            currentChapterId = item.getChapterId();
            btnUnlock.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onUnlockClicked(currentChapterId);
                }
            });
        }
    }

    static class LoadingViewHolder extends RecyclerView.ViewHolder {
        public LoadingViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
