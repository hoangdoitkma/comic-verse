package com.example.comicversev1.presentation.detail;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.comicversev1.R;
import com.example.comicversev1.databinding.FragmentComicDetailBinding;
import com.example.comicversev1.domain.entity.ChapterItem;
import com.example.comicversev1.domain.entity.ComicDetailEntity;
import com.example.comicversev1.presentation.home.ShelfAdapter;
import com.example.comicversev1.presentation.shared.adapter.ShelfSectionAdapter;

import java.util.Collections;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

import android.view.inputmethod.InputMethodManager;
import android.content.Context;
import com.example.comicversev1.data.model.CommentDTO;
import com.example.comicversev1.presentation.comments.CommentAdapter;
import com.example.comicversev1.presentation.comments.CommentViewModel;

@AndroidEntryPoint
public class ComicDetailFragment extends Fragment implements CommentAdapter.OnCommentInteractionListener {

    private FragmentComicDetailBinding binding;
    private ComicDetailViewModel viewModel;
    
    private ComicDetailFragmentArgs args;
    private int savedChapterId = -1;

    private CommentViewModel commentViewModel;
    private CommentAdapter commentAdapter;
    private Integer replyingToCommentId = null;
    private boolean isCommentsLoaded = false;

    private ShelfAdapter similarAdapter;
    private boolean isSynopsisExpanded = false;

    public ComicDetailFragment() {
        // Required empty constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentComicDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        args = ComicDetailFragmentArgs.fromBundle(requireArguments());
        viewModel = new ViewModelProvider(this).get(ComicDetailViewModel.class);
        commentViewModel = new ViewModelProvider(this).get(CommentViewModel.class);
        
        setupViews();
        setupSimilarNovels();
        setupComments();
        observeState();
        
        // Cần tải data nếu chưa có
        if (args.getSlug() != null && !args.getSlug().isEmpty()) {
            // Tải chi tiết truyện theo slug. Điều này có thể đã được thực hiện ở onResume hoặc tự động bởi ViewModel.
            // ViewModel của chúng ta hiện tại tự động load theo savedStateHandle
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        ComicDetailUiState state = viewModel.uiState().getValue();
        if (state != null && state.getComic() != null) {
            viewModel.loadSavedProgress(state.getComic().getId());
        }
    }

    private void setupViews() {
        binding.toolbarDetail.setNavigationOnClickListener(v -> {
            NavHostFragment.findNavController(this).navigateUp();
        });

        binding.btnSubscribe.setOnClickListener(v -> {
            ComicDetailUiState state = viewModel.uiState().getValue();
            if (state != null && state.getComic() != null) {
                // Determine type (COMIC if not containing Novel in ViewModel logic or path)
                // ComicDetailFragment is typically for COMIC
                viewModel.toggleFavorite(state.getComic().getTitle(), state.getComic().getCoverImage(), "COMIC");
            }
        });


        binding.btnReport.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Đã mở form báo cáo", Toast.LENGTH_SHORT).show();
        });

        // Di chuyển tới khung bình luận khi người dùng muốn bình luận ngay
        binding.icComments.setOnClickListener(v -> {
            binding.scrollViewMain.smoothScrollTo(0, binding.layoutCommentInput.getTop());
            binding.etComicComment.requestFocus();
        });

        binding.btnExpandSynopsis.setOnClickListener(v -> {
            isSynopsisExpanded = !isSynopsisExpanded;
            if (isSynopsisExpanded) {
                binding.txtSynopsis.setMaxLines(Integer.MAX_VALUE);
                binding.icSynopsisExpand.setImageResource(android.R.drawable.arrow_up_float);
            } else {
                binding.txtSynopsis.setMaxLines(3);
                binding.icSynopsisExpand.setImageResource(android.R.drawable.arrow_down_float);
            }
        });

        binding.btnStartReading.setOnClickListener(v -> {
            ComicDetailUiState state = viewModel.uiState().getValue();
            if (state != null && state.getChapters() != null && !state.getChapters().isEmpty()) {
                // Determine target chapter
                int targetChapterId = (savedChapterId > 0) ? savedChapterId : state.getChapters().get(0).getId();
                int actualComicId = state.getComic() != null ? state.getComic().getId() : args.getComicId();
                
                // Route to ReaderFragment (Image Reader) instead of TextNovelReader
                com.example.comicversev1.NavGraphDirections.ActionGlobalReader action =
                        com.example.comicversev1.NavGraphDirections.actionGlobalReader(targetChapterId, actualComicId);
                NavHostFragment.findNavController(this).navigate(action);
            } else {
                Toast.makeText(requireContext(), "Chưa có chương nào để đọc!", Toast.LENGTH_SHORT).show();
            }
        });
        
        binding.btnViewAllChapters.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Xem tất cả chương", Toast.LENGTH_SHORT).show();
        });
    }

    private void setupComments() {
        Context darkContext = new androidx.appcompat.view.ContextThemeWrapper(requireContext(), androidx.appcompat.R.style.ThemeOverlay_AppCompat_Dark);
        commentAdapter = new CommentAdapter(darkContext, this);
        binding.rvComicComments.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvComicComments.setAdapter(commentAdapter);
        binding.rvComicComments.setNestedScrollingEnabled(false);

        binding.btnSendComicComment.setOnClickListener(v -> {
            String content = binding.etComicComment.getText().toString().trim();
            ComicDetailUiState state = viewModel.uiState().getValue();
            if (!content.isEmpty() && state != null && state.getComic() != null) {
                int actualComicId = state.getComic().getId();
                commentViewModel.postComicComment(actualComicId, content, replyingToCommentId);
                binding.etComicComment.setText("");
                binding.etComicComment.setHint("Viết bình luận...");
                replyingToCommentId = null;
                // Hide keyboard
                InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
            } else if (state == null || state.getComic() == null) {
                Toast.makeText(requireContext(), "Chưa tải xong dữ liệu truyện", Toast.LENGTH_SHORT).show();
            }
        });

        binding.btnPrevPage.setOnClickListener(v -> {
            Integer current = commentViewModel.getCurrentPage().getValue();
            if (current != null && current > 1) {
                commentViewModel.loadPage(current - 2); // 0-indexed API, current is 1-indexed
            }
        });

        binding.btnNextPage.setOnClickListener(v -> {
            Integer current = commentViewModel.getCurrentPage().getValue();
            Integer total = commentViewModel.getTotalPages().getValue();
            if (current != null && total != null && current < total) {
                commentViewModel.loadPage(current); // 0-indexed API, current is equivalent to next page index
            }
        });
    }

    private void setupSimilarNovels() {
        similarAdapter = new ShelfAdapter(item -> {
            // Re-navigate to ComicDetailFragment for the clicked item (same fragment class)
            NavHostFragment.findNavController(this).navigate(
                com.example.comicversev1.NavGraphDirections.actionGlobalComicDetail(item.getSlug(), 0)
            );
        });
        
        binding.recyclerSimilar.setLayoutManager(new LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false));
        binding.recyclerSimilar.setAdapter(similarAdapter);
    }

    private void observeState() {
        viewModel.uiState().observe(getViewLifecycleOwner(), state -> {
            if (state.getComic() != null) {
                bindComicData(state.getComic());
                if (!isCommentsLoaded) {
                    commentViewModel.loadComicComments(state.getComic().getId(), 0, 5);
                    isCommentsLoaded = true;
                }
            }
            if (state.getChapters() != null && !state.getChapters().isEmpty()) {
                String title = state.getComic() != null ? state.getComic().getTitle() : null;
                bindChapters(state.getChapters(), state.getComic() != null ? state.getComic().getId() : args.getComicId(), title);
            }

            if (state.getError() != null) {
                if ("COMIC_DELETED".equals(state.getError())) {
                    Toast.makeText(requireContext(), "Truyện này đã ngừng phát hành hoặc bị xóa", Toast.LENGTH_LONG).show();
                    NavHostFragment.findNavController(this).navigateUp();
                } else {
                    Toast.makeText(requireContext(), state.getError(), Toast.LENGTH_SHORT).show();
                }
            }
        });

        viewModel.isFavorite().observe(getViewLifecycleOwner(), isFav -> {
            if (isFav) {
                binding.btnSubscribe.setText("Đã lưu");
                binding.btnSubscribe.setStrokeColorResource(R.color.icon_yellow);
                binding.btnSubscribe.setTextColor(requireContext().getColor(R.color.icon_yellow));
            } else {
                binding.btnSubscribe.setText("Yêu thích");
                binding.btnSubscribe.setStrokeColorResource(R.color.text_primary);
                binding.btnSubscribe.setTextColor(requireContext().getColor(R.color.text_primary));
            }
        });

        viewModel.savedProgress().observe(getViewLifecycleOwner(), history -> {
            if (history != null && history.chapterId > 0) {
                savedChapterId = history.chapterId;
                
                String chapterTitle = "Chương ?";
                ComicDetailUiState state = viewModel.uiState().getValue();
                if (state != null && state.getChapters() != null) {
                    for (ChapterItem ch : state.getChapters()) {
                        if (ch.getId() == history.chapterId) {
                            if (!ch.getTitle().toLowerCase().contains("chương") && !ch.getTitle().toLowerCase().contains("chap")) {
                                chapterTitle = "Chương " + ch.getTitle();
                            } else {
                                chapterTitle = ch.getTitle();
                            }
                            break;
                        }
                    }
                }
                
                binding.txtStartReadingTitle.setText("Tiếp tục đọc");
                binding.txtStartReadingSubtitle.setText(chapterTitle + ": " + history.percent + " %");
            } else {
                binding.txtStartReadingTitle.setText("Bắt đầu đọc");
                binding.txtStartReadingSubtitle.setText("Chương 1");
            }
        });

        commentViewModel.getComments().observe(getViewLifecycleOwner(), comments -> {
            commentAdapter.setComments(comments);
            if (comments.isEmpty()) {
                binding.tvEmptyComicComments.setVisibility(View.VISIBLE);
                binding.layoutCommentPagination.setVisibility(View.GONE);
            } else {
                binding.tvEmptyComicComments.setVisibility(View.GONE);
                binding.layoutCommentPagination.setVisibility(View.VISIBLE);
            }
        });

        commentViewModel.getCurrentPage().observe(getViewLifecycleOwner(), page -> {
            Integer total = commentViewModel.getTotalPages().getValue();
            updatePaginationUI(page, total == null ? 1 : total);
        });

        commentViewModel.getTotalPages().observe(getViewLifecycleOwner(), total -> {
            Integer current = commentViewModel.getCurrentPage().getValue();
            updatePaginationUI(current == null ? 1 : current, total);
        });

        commentViewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
            }
        });

        commentViewModel.getCommentPosted().observe(getViewLifecycleOwner(), comment -> {
            if (comment != null) {
                commentAdapter.addComment(comment);
                binding.tvEmptyComicComments.setVisibility(View.GONE);
                Toast.makeText(requireContext(), "Đã đăng bình luận", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void bindComicData(ComicDetailEntity comic) {
        Glide.with(this)
             .load(comic.getCoverImage())
             .placeholder(R.drawable.bg_icon_rounded)
             .into(binding.imgCover);
             
        binding.txtTitle.setText(comic.getTitle());
        binding.txtAuthor.setText((comic.getAuthorName() != null && !comic.getAuthorName().isEmpty()) ? comic.getAuthorName() : "Khuyết Danh");
        binding.txtUpdateDate.setText((comic.getUpdatedAt() != null) ? comic.getUpdatedAt() : "Không rõ");
        binding.txtSource.setText("ComicVerse"); // Default for DB comic
        
        String statusLabel = "Đang ra";
        if ("COMPLETED".equalsIgnoreCase(comic.getStatus())) {
            statusLabel = "Đã hoàn thành";
        }
        binding.txtStatus.setText(statusLabel);
        
        // Tags
        binding.layoutTags.removeAllViews();
        if (comic.getGenres() != null && !comic.getGenres().isEmpty()) {
            binding.layoutTags.setVisibility(View.VISIBLE);
            for (String genre : comic.getGenres()) {
                addTag(genre);
            }
        } else {
            binding.layoutTags.setVisibility(View.GONE);
        }
        
        // Synopsis
        String contentText = (comic.getSynopsis() != null && !comic.getSynopsis().isEmpty()) 
                ? comic.getSynopsis() : comic.getAiSummary();
        binding.txtSynopsis.setText(contentText != null ? contentText : "Đang cập nhật nội dung...");
    }

    private void updatePaginationUI(int current, int total) {
        binding.tvCurrentPage.setText("Trang " + current + " / " + total);
        binding.btnPrevPage.setEnabled(current > 1);
        binding.btnPrevPage.setAlpha(current > 1 ? 1.0f : 0.5f);
        binding.btnNextPage.setEnabled(current < total);
        binding.btnNextPage.setAlpha(current < total ? 1.0f : 0.5f);
    }

    private void addTag(String text) {
        com.google.android.material.chip.Chip chip = new com.google.android.material.chip.Chip(requireContext());
        chip.setText(text.startsWith("#") ? text : "#" + text);
        chip.setTextColor(Color.parseColor("#E0E0E0"));
        chip.setTextSize(14f);
        
        chip.setCheckable(false);
        chip.setClickable(false);
        
        chip.setChipBackgroundColor(ColorStateList.valueOf(Color.parseColor("#1C1E26")));
        chip.setChipStrokeWidth(0f);
        chip.setShapeAppearanceModel(chip.getShapeAppearanceModel().withCornerSize(16f)); 
        
        binding.layoutTags.addView(chip);
    }

    private void bindChapters(List<ChapterItem> chapters, int comicId, String comicTitle) {
        binding.layoutChapters.removeAllViews();
        
        // Default chapters order is ASC (1, 2, 3 ...), we want latest 3 so we reverse or iterate from back
        int total = chapters.size();
        int maxShows = Math.min(3, total);
        
        for (int i = 0; i < maxShows; i++) {
            ChapterItem chapter = chapters.get(total - 1 - i); // Latest first
            
            View row = LayoutInflater.from(requireContext()).inflate(R.layout.item_detail_chapter_row, binding.layoutChapters, false);
            TextView title = row.findViewById(R.id.txtChapterTitle);
            TextView dateStr = row.findViewById(R.id.txtChapterDate);
            
            String chapTitle = chapter.getTitle();
            if (!chapTitle.toLowerCase().contains("chương") && !chapTitle.toLowerCase().contains("chap")) {
                chapTitle = "Chương " + chapTitle;
            }
            title.setText(chapTitle);
            dateStr.setText("Vừa cập nhật"); // Placeholder
            
            row.setOnClickListener(v -> {
                com.example.comicversev1.NavGraphDirections.ActionGlobalReader action =
                        com.example.comicversev1.NavGraphDirections.actionGlobalReader(chapter.getId(), comicId);
                NavHostFragment.findNavController(this).navigate(action);
            });
            
            binding.layoutChapters.addView(row);
        }
    }

    @Override
    public void onReplyClick(CommentDTO comment) {
        replyingToCommentId = comment.getId();
        binding.etComicComment.setHint("Trả lời " + comment.getUserDisplayName() + "...");
        binding.etComicComment.requestFocus();
        binding.scrollViewMain.smoothScrollTo(0, binding.layoutCommentInput.getBottom());
        InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) imm.showSoftInput(binding.etComicComment, InputMethodManager.SHOW_IMPLICIT);
    }

    @Override
    public void onLoadRepliesClick(CommentDTO comment, int position) {
        commentViewModel.loadReplies(comment.getId(), 0, 50, new CommentViewModel.ReplyCallback() {
            @Override
            public void onRepliesLoaded(List<CommentDTO> replies) {
                commentAdapter.updateReplies(position, replies);
            }

            @Override
            public void onError(String message) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
