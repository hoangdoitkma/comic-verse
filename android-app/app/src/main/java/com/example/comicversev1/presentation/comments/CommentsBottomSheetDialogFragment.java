package com.example.comicversev1.presentation.comments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.comicversev1.R;
import com.example.comicversev1.data.model.CommentDTO;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class CommentsBottomSheetDialogFragment extends BottomSheetDialogFragment implements CommentAdapter.OnCommentInteractionListener {

    public static final String REQUEST_COMMENTS_CHANGED = "comments_changed";

    private static final String ARG_CHAPTER_ID = "chapter_id";
    private static final String ARG_TARGET_TYPE = "target_type";
    private static final String ARG_TARGET_ID = "target_id";
    private static final String ARG_TITLE = "title";

    private CommentViewModel viewModel;
    private CommentAdapter adapter;
    private int targetId;
    private CommentTargetType targetType = CommentTargetType.CHAPTER;
    private String title = "Bình luận";

    private RecyclerView rvComments;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private EditText etComment;
    private ImageButton btnSend;
    private ImageButton btnClose;
    private TextView tvCommentCount;

    // Track replying state
    private Integer replyingToCommentId = null;
    private View btnPrevPage, btnNextPage, layoutCommentPagination;
    private TextView tvCurrentPage;

    public static CommentsBottomSheetDialogFragment newInstance(int chapterId) {
        return newInstance(CommentTargetType.CHAPTER, chapterId, "Bình luận chương");
    }

    public static CommentsBottomSheetDialogFragment newInstance(CommentTargetType targetType, int targetId, String title) {
        CommentsBottomSheetDialogFragment fragment = new CommentsBottomSheetDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TARGET_TYPE, targetType.name());
        args.putInt(ARG_TARGET_ID, targetId);
        args.putString(ARG_TITLE, title);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            String rawTargetType = getArguments().getString(ARG_TARGET_TYPE);
            if (rawTargetType != null) {
                try {
                    targetType = CommentTargetType.valueOf(rawTargetType);
                } catch (IllegalArgumentException ignored) {
                    targetType = CommentTargetType.CHAPTER;
                }
                targetId = getArguments().getInt(ARG_TARGET_ID, getArguments().getInt(ARG_CHAPTER_ID));
                title = getArguments().getString(ARG_TITLE, title);
            } else {
                targetId = getArguments().getInt(ARG_CHAPTER_ID);
                targetType = CommentTargetType.CHAPTER;
                title = "Bình luận chương";
            }
        }
        viewModel = new ViewModelProvider(this).get(CommentViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_comments_bottom_sheet, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        rvComments = view.findViewById(R.id.rvComments);
        progressBar = view.findViewById(R.id.progressBar);
        tvEmpty = view.findViewById(R.id.tvEmpty);
        etComment = view.findViewById(R.id.etComment);
        btnSend = view.findViewById(R.id.btnSend);
        btnClose = view.findViewById(R.id.btnClose);
        tvCommentCount = view.findViewById(R.id.tvCommentCount);
        btnPrevPage = view.findViewById(R.id.btnPrevPage);
        btnNextPage = view.findViewById(R.id.btnNextPage);
        tvCurrentPage = view.findViewById(R.id.tvCurrentPage);
        layoutCommentPagination = view.findViewById(R.id.layoutCommentPagination);

        tvCommentCount.setText(title);

        adapter = new CommentAdapter(requireContext(), this);
        rvComments.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvComments.setAdapter(adapter);

        btnClose.setOnClickListener(v -> dismiss());

        btnSend.setOnClickListener(v -> {
            String content = etComment.getText().toString().trim();
            if (!content.isEmpty()) {
                viewModel.postComment(content, replyingToCommentId);
            }
        });

        observeViewModel();
        
        // Initial load
        viewModel.loadComments(targetType, targetId, 0, 5);

        btnPrevPage.setOnClickListener(v -> {
            Integer current = viewModel.getCurrentPage().getValue();
            if (current != null && current > 1) {
                viewModel.loadPage(current - 2);
            }
        });

        btnNextPage.setOnClickListener(v -> {
            Integer current = viewModel.getCurrentPage().getValue();
            Integer total = viewModel.getTotalPages().getValue();
            if (current != null && total != null && current < total) {
                viewModel.loadPage(current);
            }
        });
    }

    private void observeViewModel() {
        viewModel.getComments().observe(getViewLifecycleOwner(), comments -> {
            adapter.setComments(comments);
            if (comments == null || comments.isEmpty()) {
                tvEmpty.setVisibility(View.VISIBLE);
                layoutCommentPagination.setVisibility(View.GONE);
            } else {
                tvEmpty.setVisibility(View.GONE);
                layoutCommentPagination.setVisibility(View.VISIBLE);
            }
        });

        viewModel.getCurrentPage().observe(getViewLifecycleOwner(), page -> {
            Integer total = viewModel.getTotalPages().getValue();
            updatePaginationUI(page, total == null ? 1 : total);
        });

        viewModel.getTotalPages().observe(getViewLifecycleOwner(), total -> {
            Integer current = viewModel.getCurrentPage().getValue();
            updatePaginationUI(current == null ? 1 : current, total);
        });

        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });

        viewModel.getIsPosting().observe(getViewLifecycleOwner(), isPosting -> {
            btnSend.setEnabled(!isPosting);
            btnSend.setAlpha(isPosting ? 0.5f : 1.0f);
            etComment.setEnabled(!isPosting);
        });

        viewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getCommentPosted().observe(getViewLifecycleOwner(), comment -> {
            if (comment != null) {
                boolean wasReply = replyingToCommentId != null;
                etComment.setText("");
                etComment.setHint("Viết bình luận...");
                replyingToCommentId = null;
                Toast.makeText(requireContext(), wasReply ? "Đã đăng phản hồi" : "Đã đăng bình luận", Toast.LENGTH_SHORT).show();
                getParentFragmentManager().setFragmentResult(REQUEST_COMMENTS_CHANGED, new Bundle());
                if (!wasReply) {
                    rvComments.smoothScrollToPosition(0);
                }
                InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) imm.hideSoftInputFromWindow(etComment.getWindowToken(), 0);
            }
        });
    }

    private void updatePaginationUI(int current, int total) {
        if (tvCurrentPage != null) tvCurrentPage.setText("Trang " + current + " / " + total);
        if (btnPrevPage != null) {
            btnPrevPage.setEnabled(current > 1);
            btnPrevPage.setAlpha(current > 1 ? 1.0f : 0.5f);
        }
        if (btnNextPage != null) {
            btnNextPage.setEnabled(current < total);
            btnNextPage.setAlpha(current < total ? 1.0f : 0.5f);
        }
    }

    @Override
    public void onReplyClick(CommentDTO comment) {
        replyingToCommentId = comment.getId();
        etComment.setHint("Trả lời " + comment.getUserDisplayName() + "...");
        etComment.requestFocus();
        InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) imm.showSoftInput(etComment, InputMethodManager.SHOW_IMPLICIT);
    }

    @Override
    public void onLoadRepliesClick(CommentDTO comment, int position) {
        viewModel.loadReplies(comment.getId(), 0, 50, new CommentViewModel.ReplyCallback() {
            @Override
            public void onRepliesLoaded(List<CommentDTO> replies) {
                adapter.updateReplies(position, replies);
            }

            @Override
            public void onError(String message) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
