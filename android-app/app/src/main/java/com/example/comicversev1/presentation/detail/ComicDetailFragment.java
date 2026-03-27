package com.example.comicversev1.presentation.detail;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.ConcatAdapter;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.comicversev1.data.local.entity.ReadingHistoryEntity;
import com.example.comicversev1.databinding.FragmentComicDetailBinding;
import com.example.comicversev1.domain.entity.ChapterItem;

import com.example.comicversev1.domain.entity.HomeContent;
import com.example.comicversev1.presentation.home.ShelfAdapter;
import com.example.comicversev1.presentation.shared.adapter.ShelfSectionAdapter;

import java.util.Collections;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ComicDetailFragment extends Fragment implements ChapterListAdapter.OnChapterClickListener {

    private FragmentComicDetailBinding binding;
    private ComicDetailViewModel viewModel;
    
    private DetailHeaderAdapter headerAdapter;
    private DetailChapterHeaderAdapter chapterHeaderAdapter;
    private ChapterListAdapter chapterAdapter;
    private DetailCommentsAdapter commentsAdapter;
    private ShelfAdapter relatedAdapter;
    private ShelfSectionAdapter relatedSectionAdapter;

    private ComicDetailFragmentArgs args;

    // Saved reading progress (chapter ID to resume from)
    private int savedChapterId = -1;

    public ComicDetailFragment() {
        args = null;
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
        
        setupViews();
        setupRecyclerView();
        observeState();
    }

    private void setupViews() {
        // App Bar Controls
        binding.toolbarDetail.setNavigationOnClickListener(v -> {
            NavHostFragment.findNavController(this).navigateUp();
        });
        
        binding.toolbarDetail.setOnMenuItemClickListener(item -> {
            return false;
        });

        // Bottom Bar Control - Navigate to Reader
        binding.btnStartReading.setOnClickListener(v -> {
            ComicDetailUiState state = viewModel.uiState().getValue();
            if (state != null && state.getChapters() != null && !state.getChapters().isEmpty()) {
                int targetChapterId;

                if (savedChapterId > 0) {
                    // Resume from saved chapter
                    targetChapterId = savedChapterId;
                } else {
                    // Start from first chapter (chapters sorted DESC, last item = chapter 1)
                    targetChapterId = state.getChapters().get(state.getChapters().size() - 1).getId();
                }

                // Use actual comicId from API response, not from nav args (which can be 0)
                int actualComicId = state.getComic() != null ? state.getComic().getId() : args.getComicId();

                ComicDetailFragmentDirections.ActionDetailToReader action =
                        ComicDetailFragmentDirections.actionDetailToReader(targetChapterId, actualComicId);
                NavHostFragment.findNavController(this).navigate(action);
            } else {
                Toast.makeText(requireContext(), "Chưa có chương nào để đọc!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupRecyclerView() {
        headerAdapter = new DetailHeaderAdapter();
        chapterHeaderAdapter = new DetailChapterHeaderAdapter();
        chapterAdapter = new ChapterListAdapter(this);
        commentsAdapter = new DetailCommentsAdapter();
        
        relatedAdapter = new ShelfAdapter(item -> {});
        relatedSectionAdapter = new ShelfSectionAdapter(
            "Cùng thể loại:", 
            relatedAdapter, 
            new LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false),
            true // showAlways
        );

        ConcatAdapter concatAdapter = new ConcatAdapter(
                headerAdapter,
                chapterHeaderAdapter,
                chapterAdapter,
                commentsAdapter,
                relatedSectionAdapter
        );

        binding.recyclerMain.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerMain.setAdapter(concatAdapter);
    }

    private void observeState() {
        viewModel.uiState().observe(getViewLifecycleOwner(), state -> {
            if (state.getComic() != null) {
                headerAdapter.setComic(state.getComic());
            }
            if (state.getChapters() != null && !state.getChapters().isEmpty()) {
                chapterAdapter.submitList(state.getChapters());
            } else {
                chapterAdapter.submitList(Collections.emptyList());
            }

            if (state.getError() != null) {
                Toast.makeText(requireContext(), state.getError(), Toast.LENGTH_SHORT).show();
            }
        });

        // Observe saved reading progress → update button text and saved chapter ID
        viewModel.savedProgress().observe(getViewLifecycleOwner(), history -> {
            if (history != null && history.chapterId > 0) {
                savedChapterId = history.chapterId;
                binding.txtStartReadingTitle.setText("Đọc tiếp ▶");
                binding.txtStartReadingSubtitle.setText("Tiếp tục từ lần trước");
            }
        });
    }

    @Override
    public void onChapterClick(ChapterItem item) {
        // Lấy comicId thật từ API response (không dùng args.getComicId() vì có thể = 0)
        ComicDetailUiState state = viewModel.uiState().getValue();
        int actualComicId = (state != null && state.getComic() != null)
                ? state.getComic().getId() : args.getComicId();
        ComicDetailFragmentDirections.ActionDetailToReader action =
                ComicDetailFragmentDirections.actionDetailToReader(item.getId(), actualComicId);
        NavHostFragment.findNavController(this).navigate(action);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
