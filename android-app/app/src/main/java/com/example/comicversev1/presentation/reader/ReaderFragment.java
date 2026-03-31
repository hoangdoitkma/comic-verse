package com.example.comicversev1.presentation.reader;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.comicversev1.databinding.FragmentReaderBinding;
import com.example.comicversev1.domain.entity.ChapterEntity;
import com.example.comicversev1.domain.entity.ChapterItem;
import com.example.comicversev1.presentation.novel.reader.BottomSheetChapterAdapter;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import java.util.ArrayList;

import com.example.comicversev1.R;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ReaderFragment extends Fragment {

    private FragmentReaderBinding binding;
    private ReaderViewModel viewModel;
    private ReaderPagesAdapter adapter;
    private LinearLayoutManager layoutManager;

    // Track current visible chapter for progress saving
    private int currentVisibleChapterId = -1;
    private String currentVisibleChapterTitle = "";

    private ViewTrackingTimer viewTrackingTimer;

    private BottomSheetDialog chapterListSheet;
    private BottomSheetChapterAdapter chapterListAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentReaderBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(ReaderViewModel.class);
        adapter = new ReaderPagesAdapter();
        layoutManager = new LinearLayoutManager(requireContext());
        viewTrackingTimer = new ViewTrackingTimer();

        binding.recyclerView.setLayoutManager(layoutManager);
        binding.recyclerView.setAdapter(adapter);

        setupToolbar();
        setupBackButton();
        setupCommentsButton();
        setupNavigationButtons();
        setupScrollListener();
        setupTouchZones();
        observeState();
    }

    private void setupToolbar() {
        ReaderFragmentArgs args = ReaderFragmentArgs.fromBundle(requireArguments());
        if (args.getComicTitle() != null) {
            binding.txtNovelTitle.setText(args.getComicTitle());
        } else {
            binding.txtNovelTitle.setText("Đang tải...");
        }
    }

    private void setupNavigationButtons() {
        binding.btnChapterList.setOnClickListener(v -> {
            showChapterListBottomSheet();
            viewModel.fetchChapterList();
        });
        
        binding.btnPreviousChapter.setOnClickListener(v -> {
            Integer prevId = viewModel.getActivePrevChapterId();
            if (prevId != null && prevId > 0) {
                viewModel.loadSpecificChapter(prevId);
            } else {
                android.widget.Toast.makeText(requireContext(), "Đây là chương đầu tiên!", android.widget.Toast.LENGTH_SHORT).show();
            }
        });
        
        binding.btnNextChapter.setOnClickListener(v -> {
            Integer nextId = viewModel.getPendingNextChapterId();
            if (nextId != null && nextId > 0) {
                viewModel.loadSpecificChapter(nextId);
            } else {
                android.widget.Toast.makeText(requireContext(), "Đây là chương mới nhất!", android.widget.Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showChapterListBottomSheet() {
        if (chapterListSheet == null) {
            chapterListSheet = new BottomSheetDialog(requireContext());
            View sheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_chapter_list, null);
            chapterListSheet.setContentView(sheetView);

            chapterListSheet.getBehavior().setPeekHeight(getResources().getDisplayMetrics().heightPixels);

            RecyclerView rvChapters = sheetView.findViewById(R.id.rv_chapter_list);
            rvChapters.setLayoutManager(new LinearLayoutManager(requireContext()));
            
            chapterListAdapter = new BottomSheetChapterAdapter(new ArrayList<>(), chapterId -> {
                chapterListSheet.dismiss();
                viewModel.loadSpecificChapter(chapterId);
            });
            rvChapters.setAdapter(chapterListAdapter);

            View btnClose = sheetView.findViewById(R.id.btn_close_sheet);
            if (btnClose != null) {
                btnClose.setOnClickListener(btnV -> chapterListSheet.dismiss());
            }

            EditText etSearch = sheetView.findViewById(R.id.et_search_chapter);
            if (etSearch != null) {
                etSearch.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        if (chapterListAdapter != null) {
                            chapterListAdapter.getFilter().filter(s);
                        }
                    }
                    @Override
                    public void afterTextChanged(Editable s) {}
                });
            }
        }
        chapterListSheet.show();
    }

    private void setupCommentsButton() {
        binding.btnComments.setOnClickListener(v -> {
            if (currentVisibleChapterId > 0) {
                com.example.comicversev1.presentation.comments.CommentsBottomSheetDialogFragment.newInstance(currentVisibleChapterId)
                        .show(getChildFragmentManager(), "CommentsBottomSheet");
            } else {
                android.widget.Toast.makeText(requireContext(), "Đang tải chương...", android.widget.Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupTouchZones() {
        android.view.GestureDetector gestureDetector = new android.view.GestureDetector(requireContext(), new android.view.GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapConfirmed(android.view.MotionEvent e) {
                int screenHeight = binding.recyclerView.getHeight();
                float y = e.getY();
                if (y < screenHeight / 3.0f) {
                    // Top zone: scroll up 1 page
                    binding.recyclerView.smoothScrollBy(0, -screenHeight);
                } else if (y > screenHeight * 2.0f / 3.0f) {
                    // Bottom zone: scroll down 1 page
                    binding.recyclerView.smoothScrollBy(0, screenHeight);
                } else {
                    // Middle zone: toggle topBar and bottomBar visibility
                    boolean isVisible = binding.appBarLayout.getVisibility() == View.VISIBLE;
                    binding.appBarLayout.setVisibility(isVisible ? View.GONE : View.VISIBLE);
                    binding.bottomBar.setVisibility(isVisible ? View.GONE : View.VISIBLE);
                }
                return true;
            }
        });

        binding.recyclerView.addOnItemTouchListener(new RecyclerView.SimpleOnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull android.view.MotionEvent e) {
                gestureDetector.onTouchEvent(e);
                // Return false allows child views (like PhotoView) to receive touch events (for zooming, paging)
                return false; 
            }
        });
    }

    private void setupBackButton() {
        binding.btnBack.setOnClickListener(v -> {
            // Lưu tiến trình bị bất đồng bộ ngay trước khi thoát reader
            if (layoutManager != null && viewModel != null && adapter != null) {
                int firstVisible = layoutManager.findFirstVisibleItemPosition();
                if (firstVisible >= 0) {
                    int chapterId = adapter.getChapterIdAtPosition(firstVisible);
                    int relativePage = adapter.getRelativePageIndex(firstVisible);
                    if (chapterId > 0) {
                        viewModel.saveProgressImmediately(chapterId, relativePage);
                    }
                }
            }
            NavHostFragment.findNavController(this).navigateUp();
        });
    }

    /**
     * Detect when user scrolls near the bottom → load next chapter
     * Also update the header with current chapter info
     * Save reading progress when scroll FULLY STOPS (SCROLL_STATE_IDLE)
     */
    private void setupScrollListener() {
        binding.recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                // Lưu progress khi scroll dừng hẳn (bao gồm cả sau fling)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    Log.d("ReaderFragment", ">>> SCROLL_STATE_IDLE detected. Triggering saveCurrentProgress()");
                    saveCurrentProgress();
                }
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                int totalItemCount = layoutManager.getItemCount();
                int lastVisiblePosition = layoutManager.findLastVisibleItemPosition();
                int firstVisiblePosition = layoutManager.findFirstVisibleItemPosition();

                // Update chapter title based on first visible item
                if (firstVisiblePosition >= 0) {
                    int chapterId = adapter.getChapterIdAtPosition(firstVisiblePosition);
                    if (chapterId > 0 && chapterId != currentVisibleChapterId) {
                        currentVisibleChapterId = chapterId;
                        ChapterEntity ch = viewModel.getChapterById(chapterId);
                        if (ch != null) {
                            currentVisibleChapterTitle = ch.getTitle();
                            binding.txtChapterTitle.post(() -> binding.txtChapterTitle.setText(ch.getTitle()));
                        }
                        
                        // Start/Restart view tracking timer when scrolled to a new chapter
                        if (viewTrackingTimer != null) {
                            viewTrackingTimer.startTimer(viewModel.getComicId(), chapterId, (cId, chId) -> {
                                if (viewModel != null) {
                                    viewModel.trackChapterView(chId);
                                }
                            });
                        }
                    }

                    // Update page progress (post to run queue to prevent requestLayout() loop during scroll)
                    binding.txtPageProgress.post(() -> 
                        binding.txtPageProgress.setText(String.format("Trang %d / %d", firstVisiblePosition + 1, totalItemCount))
                    );
                }

                // Trigger load next chapter when near the end (5 items from bottom)
                if (lastVisiblePosition >= totalItemCount - 5 && totalItemCount > 0) {
                    viewModel.loadNextChapterIfNeeded();
                }
            }
        });
    }

    /**
     * Lưu vị trí đọc hiện tại vào Room DB
     */
    private void saveCurrentProgress() {
        if (layoutManager == null || viewModel == null || adapter == null) return;
        int firstVisible = layoutManager.findFirstVisibleItemPosition();
        if (firstVisible >= 0) {
            int chapterId = adapter.getChapterIdAtPosition(firstVisible);
            int relativePage = adapter.getRelativePageIndex(firstVisible);
            if (chapterId > 0) {
                viewModel.saveReadingProgress(chapterId, relativePage);
            }
        }
    }

    private void observeState() {
        // Initial chapter load
        viewModel.uiState().observe(getViewLifecycleOwner(), state -> {
            binding.progressBar.setVisibility(state.isLoading() ? View.VISIBLE : View.GONE);
            binding.progressBarMore.setVisibility(state.isLoadingMore() ? View.VISIBLE : View.GONE);

            if (state.getChapter() != null && adapter.getItemCount() == 0) {
                ChapterEntity ch = state.getChapter();
                adapter.submitChapter(ch.getId(), ch.getTitle(), ch.getImages());
                currentVisibleChapterId = ch.getId();
                currentVisibleChapterTitle = ch.getTitle();
                binding.txtChapterTitle.setText(ch.getTitle());
                binding.txtPageProgress.setText(
                        String.format("Trang 1 / %d", ch.getImages() != null ? ch.getImages().size() : 0)
                );
                
                // Track view for initial chapter load
                if (viewTrackingTimer != null) {
                    viewTrackingTimer.startTimer(viewModel.getComicId(), ch.getId(), (cId, chId) -> {
                        if (viewModel != null) {
                            viewModel.trackChapterView(chId);
                        }
                    });
                }
                
                Log.d("ReaderFragment", ">>> Initial chapter loaded. ItemCount is now: " + adapter.getItemCount() + ". Scheduling scroll check.");
                // Móc lệnh cuộn ngay trước khi giao diện bắt đầu render pixel đầu tiên
                scheduleScrollToSavedPosition();
            }

            if (state.getError() != null && adapter.getItemCount() == 0) {
                binding.txtChapterTitle.setText("Lỗi: " + state.getError());
            }
        });

        // Append new chapters as they load
        viewModel.appendChapterEvent().observe(getViewLifecycleOwner(), chapter -> {
            if (chapter != null) {
                adapter.appendChapter(chapter.getId(), chapter.getTitle(), chapter.getImages());
            }
        });

        viewModel.clearItemsEvent().observe(getViewLifecycleOwner(), clear -> {
            if (Boolean.TRUE.equals(clear)) {
                adapter.clearItems();
            }
        });

        viewModel.chapterListEvent().observe(getViewLifecycleOwner(), chapters -> {
            if (chapters != null && chapterListSheet != null) {
                chapterListAdapter = new BottomSheetChapterAdapter(chapters, chapterId -> {
                    chapterListSheet.dismiss();
                    viewModel.loadSpecificChapter(chapterId);
                });
                RecyclerView rv = chapterListSheet.findViewById(R.id.rv_chapter_list);
                if (rv != null) {
                    rv.setAdapter(chapterListAdapter);
                }
            }
        });
    }

    private void scheduleScrollToSavedPosition() {
        binding.recyclerView.getViewTreeObserver().addOnPreDrawListener(new android.view.ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                // Hủy đăng ký ngay lập tức để chỉ chạy 1 lần
                binding.recyclerView.getViewTreeObserver().removeOnPreDrawListener(this);
                
                Integer pendingRelativePage = viewModel.consumePendingScrollPosition();
                Log.d("ReaderFragment", ">>> OnPreDraw fired. pendingRelativePage from ViewModel: " + pendingRelativePage);
                if (pendingRelativePage != null && pendingRelativePage >= 0) {
                    int restoredChapterId = viewModel.getRestoredChapterId();
                    Log.d("ReaderFragment", ">>> Restored Chapter Id: " + restoredChapterId);
                    int absolutePos = adapter.getAbsolutePosition(restoredChapterId, pendingRelativePage);
                    Log.d("ReaderFragment", ">>> Converted to Absolute Pos: " + absolutePos);

                    if (absolutePos >= 0 && absolutePos < adapter.getItemCount()) {
                        Log.d("ReaderFragment", ">>> EXECUTING layoutManager.scrollToPositionWithOffset(" + absolutePos + ", 0)");
                        // Chốt cứng Layout Manager
                        layoutManager.scrollToPositionWithOffset(absolutePos, 0);

                        // Cập nhật text Header ngay
                        int chapterId = adapter.getChapterIdAtPosition(absolutePos);
                        if (chapterId > 0) {
                            currentVisibleChapterId = chapterId;
                            ChapterEntity ch = viewModel.getChapterById(chapterId);
                            if (ch != null) {
                                currentVisibleChapterTitle = ch.getTitle();
                                binding.txtChapterTitle.setText(ch.getTitle());
                            }
                        }
                        binding.txtPageProgress.setText(
                                String.format("Trang %d / %d", absolutePos + 1, adapter.getItemCount())
                        );
                    }
                }
                return true; // Cho phép vẽ tiếp
            }
        });
    }

    @Override
    public void onStop() {
        super.onStop();
        // Bảo vệ tiến trình đọc nếu người dùng bấm nút Home (chạy ngầm) hoặc vuốt tắt ứng dụng
        // OS sẽ gọi onStop thay vì onDestroyView.
        if (layoutManager != null && viewModel != null && adapter != null) {
            int firstVisible = layoutManager.findFirstVisibleItemPosition();
            if (firstVisible >= 0) {
                int chapterId = adapter.getChapterIdAtPosition(firstVisible);
                int relativePage = adapter.getRelativePageIndex(firstVisible);
                if (chapterId > 0) {
                    viewModel.saveProgressImmediately(chapterId, relativePage);
                }
            }
        }
        
        if (viewTrackingTimer != null) {
            viewTrackingTimer.cancelTimer();
        }
    }

    @Override
    public void onDestroyView() {
        // Đảm bảo lưu progress bằng tiến trình chạy ngầm KHÔNG chặn luồng trước khi ViewModel bị destroy
        // (tránh race condition với disposables.clear() trong onCleared)
        if (layoutManager != null && viewModel != null && adapter != null) {
            int firstVisible = layoutManager.findFirstVisibleItemPosition();
            if (firstVisible >= 0) {
                int chapterId = adapter.getChapterIdAtPosition(firstVisible);
                int relativePage = adapter.getRelativePageIndex(firstVisible);
                if (chapterId > 0) {
                    viewModel.saveProgressImmediately(chapterId, relativePage);
                }
            }
        }
        if (viewTrackingTimer != null) {
            viewTrackingTimer.cancelTimer();
        }
        super.onDestroyView();
        binding = null;
    }
}
