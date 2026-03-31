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
import com.example.comicversev1.databinding.FragmentNovelDetailBinding;
import com.example.comicversev1.domain.entity.ChapterItem;
import com.example.comicversev1.domain.entity.ComicDetailEntity;
import com.example.comicversev1.presentation.home.ShelfAdapter;
import com.example.comicversev1.presentation.shared.adapter.ShelfSectionAdapter;

import java.util.Collections;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class NovelDetailFragment extends Fragment {

    private FragmentNovelDetailBinding binding;
    private ComicDetailViewModel viewModel;
    
    private NovelDetailFragmentArgs args;
    private int savedChapterId = -1;

    private ShelfAdapter similarAdapter;
    private boolean isSynopsisExpanded = false;

    public NovelDetailFragment() {
        // Required empty constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentNovelDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        args = NovelDetailFragmentArgs.fromBundle(requireArguments());
        viewModel = new ViewModelProvider(this).get(ComicDetailViewModel.class);
        
        setupViews();
        setupSimilarNovels();
        observeState();
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
            Toast.makeText(requireContext(), "Đã đăng ký truyện!", Toast.LENGTH_SHORT).show();
        });

        binding.btnDownload.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Tính năng tải xuống đang phát triển", Toast.LENGTH_SHORT).show();
        });

        binding.btnReport.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Đã mở form báo cáo", Toast.LENGTH_SHORT).show();
        });

        binding.btnComment.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Tính năng bình luận đang phát triển", Toast.LENGTH_SHORT).show();
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
                int targetChapterId = (savedChapterId > 0) ? savedChapterId : state.getChapters().get(0).getId();
                int actualComicId = state.getComic() != null ? state.getComic().getId() : args.getComicId();
                
                com.example.comicversev1.NavGraphDirections.ActionGlobalTextNovelReader action =
                        com.example.comicversev1.NavGraphDirections.actionGlobalTextNovelReader(targetChapterId, actualComicId);
                NavHostFragment.findNavController(this).navigate(action);
            } else {
                Toast.makeText(requireContext(), "Chưa có chương nào để đọc!", Toast.LENGTH_SHORT).show();
            }
        });
        
        binding.btnViewAllChapters.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Xem tất cả chương", Toast.LENGTH_SHORT).show();
        });
    }

    private void setupSimilarNovels() {
        similarAdapter = new ShelfAdapter(item -> {
            // Re-navigate to NovelDetailFragment for the clicked item
            NavHostFragment.findNavController(this).navigate(
                com.example.comicversev1.NavGraphDirections.actionGlobalNovelDetail(0, item.getSlug())
            );
        });
        
        binding.recyclerSimilar.setLayoutManager(new LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false));
        binding.recyclerSimilar.setAdapter(similarAdapter);
    }

    private void observeState() {
        viewModel.uiState().observe(getViewLifecycleOwner(), state -> {
            if (state.getComic() != null) {
                bindComicData(state.getComic());
            }
            if (state.getChapters() != null && !state.getChapters().isEmpty()) {
                bindChapters(state.getChapters(), state.getComic() != null ? state.getComic().getId() : args.getComicId());
            }

            if (state.getError() != null) {
                Toast.makeText(requireContext(), state.getError(), Toast.LENGTH_SHORT).show();
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
    }

    private void bindComicData(ComicDetailEntity comic) {
        Glide.with(this)
             .load(comic.getCoverImage())
             .placeholder(R.drawable.bg_icon_rounded)
             .into(binding.imgCover);
             
        binding.txtTitle.setText(comic.getTitle());
        binding.txtAuthor.setText((comic.getAuthorName() != null && !comic.getAuthorName().isEmpty()) ? comic.getAuthorName() : "Khuyết Danh");
        binding.txtUpdateDate.setText((comic.getUpdatedAt() != null) ? comic.getUpdatedAt() : "Không rõ");
        binding.txtSource.setText("NovelVN"); // Default
        
        String statusLabel = "Đang ra";
        if ("COMPLETED".equalsIgnoreCase(comic.getStatus())) {
            statusLabel = "Đã hoàn thành";
        }
        binding.txtStatus.setText(statusLabel);
        
        // Tags
        binding.layoutTags.removeAllViews();
        if (comic.getGenres() != null && !comic.getGenres().isEmpty()) {
            for (String genre : comic.getGenres()) {
                addTag(genre);
            }
        } else {
            addTag("#Tiên Hiệp"); // Dummy if empty
            addTag("#Kiếm Hiệp");
        }
        
        // Synopsis
        String contentText = (comic.getSynopsis() != null && !comic.getSynopsis().isEmpty()) 
                ? comic.getSynopsis() : comic.getAiSummary();
        binding.txtSynopsis.setText(contentText != null ? contentText : "Đang cập nhật nội dung...");
    }

    private void addTag(String text) {
        TextView tv = new TextView(requireContext());
        tv.setText(text.startsWith("#") ? text : "#" + text);
        tv.setTextColor(Color.parseColor("#E0E0E0"));
        tv.setTextSize(14f);
        tv.setBackgroundResource(R.drawable.bg_icon_rounded); // Reuse a rounded background
        tv.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#1C1E26")));
        
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 16, 0); // 16px right margin
        tv.setLayoutParams(params);
        tv.setPadding(32, 16, 32, 16);
        
        binding.layoutTags.addView(tv);
    }

    private void bindChapters(List<ChapterItem> chapters, int comicId) {
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
            if (!chapTitle.toLowerCase().contains("chương")) {
                chapTitle = "Chương " + chapTitle;
            }
            title.setText(chapTitle);
            dateStr.setText("Vừa cập nhật"); // Placeholder
            
            row.setOnClickListener(v -> {
                com.example.comicversev1.NavGraphDirections.ActionGlobalTextNovelReader action =
                        com.example.comicversev1.NavGraphDirections.actionGlobalTextNovelReader(chapter.getId(), comicId);
                NavHostFragment.findNavController(this).navigate(action);
            });
            
            binding.layoutChapters.addView(row);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
