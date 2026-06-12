package com.example.comicversev1.presentation.comments;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.comicversev1.R;
import com.example.comicversev1.data.model.CommentDTO;

import java.util.ArrayList;
import java.util.List;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {

    private final Context context;
    private final List<CommentDTO> comments;
    private final OnCommentInteractionListener listener;
    public interface OnCommentInteractionListener {
        void onReplyClick(CommentDTO comment);
        void onLoadRepliesClick(CommentDTO comment, int position);
    }

    public CommentAdapter(Context context, OnCommentInteractionListener listener) {
        this.context = context;
        this.comments = new ArrayList<>();
        this.listener = listener;
    }

    public void setComments(List<CommentDTO> newComments) {
        this.comments.clear();
        if (newComments != null) {
            this.comments.addAll(newComments);
        }
        notifyDataSetChanged();
    }

    public void addComment(CommentDTO comment) {
        this.comments.add(0, comment);
        notifyItemInserted(0);
    }

    public void updateReplies(int position, List<CommentDTO> replies) {
        if (position >= 0 && position < comments.size()) {
            CommentDTO comment = comments.get(position);
            comment.setReplies(replies);
            notifyItemChanged(position);
        }
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_comment, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        CommentDTO comment = comments.get(position);

        holder.tvUsername.setText(safeUserName(comment.getUserDisplayName()));
        holder.tvContent.setText(safeContent(comment.getContent()));
        holder.tvTime.setText(CommentTimeFormatter.formatTimeAgo(comment.getCreatedAt()));

        if (comment.getUserAvatarUrl() != null && !comment.getUserAvatarUrl().isEmpty()) {
            Glide.with(context)
                    .load(comment.getUserAvatarUrl())
                    .placeholder(R.drawable.ic_auth_user)
                    .error(R.drawable.ic_auth_user)
                    .into(holder.ivAvatar);
        } else {
            holder.ivAvatar.setImageResource(R.drawable.ic_auth_user);
        }

        holder.tvReply.setOnClickListener(v -> {
            if (listener != null) listener.onReplyClick(comment);
        });

        // Handle replies container visibility
        if (comment.getReplyCount() != null && comment.getReplyCount() > 0) {
            holder.llRepliesContainer.setVisibility(View.VISIBLE);
            if (comment.isRepliesLoaded() && comment.getReplies() != null) {
                holder.tvViewReplies.setVisibility(View.GONE);
                holder.rvReplies.setVisibility(View.VISIBLE);
                
                // Add margins so replies look nested visually using another adapter
                ReplyAdapter replyAdapter = new ReplyAdapter(context, comment.getReplies(), listener, comment.getId());
                holder.rvReplies.setLayoutManager(new LinearLayoutManager(context));
                holder.rvReplies.setAdapter(replyAdapter);
            } else {
                holder.tvViewReplies.setVisibility(View.VISIBLE);
                holder.rvReplies.setVisibility(View.GONE);
                holder.tvViewReplies.setText("Xem tất cả " + comment.getReplyCount() + " phản hồi");
                holder.tvViewReplies.setOnClickListener(v -> {
                    if (listener != null) listener.onLoadRepliesClick(comment, position);
                });
            }
        } else {
            holder.llRepliesContainer.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

    static class CommentViewHolder extends RecyclerView.ViewHolder {
        ImageView ivAvatar;
        TextView tvUsername, tvTime, tvContent, tvReply, tvViewReplies;
        LinearLayout llRepliesContainer;
        RecyclerView rvReplies;

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.ivAvatar);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvContent = itemView.findViewById(R.id.tvContent);
            tvReply = itemView.findViewById(R.id.tvReply);
            tvViewReplies = itemView.findViewById(R.id.tvViewReplies);
            llRepliesContainer = itemView.findViewById(R.id.llRepliesContainer);
            rvReplies = itemView.findViewById(R.id.rvReplies);
        }
    }

    // A simple adapter for replies (same visual but no view replies button)
    static class ReplyAdapter extends RecyclerView.Adapter<CommentViewHolder> {
        private final Context context;
        private final List<CommentDTO> replies;
        private final OnCommentInteractionListener listener;
        private final Integer parentCommentId;
        public ReplyAdapter(Context context, List<CommentDTO> replies, OnCommentInteractionListener listener, Integer parentCommentId) {
            this.context = context;
            this.replies = replies;
            this.listener = listener;
            this.parentCommentId = parentCommentId;
        }

        @NonNull
        @Override
        public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_comment, parent, false);
            return new CommentViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
            CommentDTO comment = replies.get(position);
            holder.tvUsername.setText(safeUserName(comment.getUserDisplayName()));
            holder.tvContent.setText(safeContent(comment.getContent()));
            holder.tvTime.setText(CommentTimeFormatter.formatTimeAgo(comment.getCreatedAt()));

            if (comment.getUserAvatarUrl() != null && !comment.getUserAvatarUrl().isEmpty()) {
                Glide.with(context).load(comment.getUserAvatarUrl())
                        .placeholder(R.drawable.ic_auth_user)
                        .error(R.drawable.ic_auth_user)
                        .into(holder.ivAvatar);
            } else {
                holder.ivAvatar.setImageResource(R.drawable.ic_auth_user);
            }

            holder.llRepliesContainer.setVisibility(View.GONE); // No nested replies
            
            // To reply to a reply, effectively reply to the parent
            holder.tvReply.setOnClickListener(v -> {
                CommentDTO fakedParent = new CommentDTO();
                fakedParent.setId(parentCommentId);
                fakedParent.setUserDisplayName(safeUserName(comment.getUserDisplayName())); 
                if (listener != null) listener.onReplyClick(fakedParent);
            });
        }

        @Override
        public int getItemCount() {
            return replies.size();
        }
    }

    private static String safeUserName(String value) {
        return value == null || value.trim().isEmpty() ? "Người đọc" : value.trim();
    }

    private static String safeContent(String value) {
        return value == null ? "" : value;
    }
}
