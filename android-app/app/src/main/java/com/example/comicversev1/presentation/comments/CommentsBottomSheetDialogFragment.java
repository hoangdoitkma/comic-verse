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

    private static final String ARG_CHAPTER_ID = "chapter_id";

    private CommentViewModel viewModel;
    private CommentAdapter adapter;
    private int chapterId;

    private RecyclerView rvComments;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private EditText etComment;
    private ImageButton btnSend;
    private ImageButton btnClose;

    // Track replying state
    private Integer replyingToCommentId = null;
    private View btnPrevPage, btnNextPage, layoutCommentPagination;
    private TextView tvCurrentPage;

    public static CommentsBottomSheetDialogFragment newInstance(int chapterId) {
        CommentsBottomSheetDialogFragment fragment = new CommentsBottomSheetDialogFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_CHAPTER_ID, chapterId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            chapterId = getArguments().getInt(ARG_CHAPTER_ID);
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

        adapter = new CommentAdapter(requireContext(), this);
        rvComments.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvComments.setAdapter(adapter);

        btnClose.setOnClickListener(v -> dismiss());

        btnSend.setOnClickListener(v -> {
            String content = etComment.getText().toString().trim();
            if (!content.isEmpty()) {
                viewModel.postComment(chapterId, content, replyingToCommentId);
                etComment.setText("");
                etComment.setHint("Write a comment...");
                replyingToCommentId = null;
                // Hide keyboard
                InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
            }
        });

        observeViewModel();
        
        // Initial load
        viewModel.loadComments(chapterId, 0, 5);

        btnPrevPage = view.findViewById(R.id.btnPrevPage);
        btnNextPage = view.findViewById(R.id.btnNextPage);
        tvCurrentPage = view.findViewById(R.id.tvCurrentPage);
        layoutCommentPagination = view.findViewById(R.id.layoutCommentPagination);

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
            if (comments.isEmpty()) {
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

        viewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getCommentPosted().observe(getViewLifecycleOwner(), comment -> {
            if (comment != null) {
                adapter.addComment(comment);
                tvEmpty.setVisibility(View.GONE);
                Toast.makeText(requireContext(), "Comment posted", Toast.LENGTH_SHORT).show();
                rvComments.smoothScrollToPosition(0);
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
        etComment.setHint("Replying to " + comment.getUserDisplayName() + "...");
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
