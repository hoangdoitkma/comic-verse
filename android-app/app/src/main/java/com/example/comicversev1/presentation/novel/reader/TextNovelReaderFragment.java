package com.example.comicversev1.presentation.novel.reader;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.comicversev1.R;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import dagger.hilt.android.AndroidEntryPoint;
import kotlin.Unit;

import com.example.comicversev1.presentation.reader.ViewTrackingTimer; // Kế thừa timer có sẵn

@AndroidEntryPoint
public class TextNovelReaderFragment extends Fragment {

    private TextNovelViewModel viewModel;
    private RecyclerView recyclerView;
    private TextNovelAdapter adapter;

    // View components
    private AppBarLayout appBarLayout;
    private View bottomBar;
    private FrameLayout loadingOverlay;
    private BottomSheetDialog settingsBottomSheet;
    private BottomSheetDialog chapterListSheet;
    private BottomSheetChapterAdapter chapterListAdapter;
    private boolean isUiVisible = false; // Toggle bars

    // Settings memory
    private SharedPreferences prefs;
    private float currentTextSize = 18f;
    private int currentTheme = 0; // 0: Light, 1: Sepia, 2: Dark
    private int currentTextColor = Color.BLACK;

    private ViewTrackingTimer viewTrackingTimer;
    private int currentlyTrackedChapterId = -1;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_text_novel_reader, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        prefs = requireActivity().getSharedPreferences("NovelReaderPrefs", Context.MODE_PRIVATE);
        currentTextSize = prefs.getFloat("text_size", 18f);
        currentTheme = prefs.getInt("theme", 0);

        viewModel = new ViewModelProvider(this).get(TextNovelViewModel.class);

        appBarLayout = view.findViewById(R.id.appBarLayout);
        bottomBar = view.findViewById(R.id.bottomBar);
        loadingOverlay = view.findViewById(R.id.loadingOverlay);

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        // toolbar.setTitle("Đọc truyện"); // Use custom titles
        toolbar.setNavigationIcon(R.drawable.baseline_arrow_back_24);
        toolbar.setNavigationOnClickListener(v -> requireActivity().onBackPressed());

        TextView tvTitle = view.findViewById(R.id.tv_novel_title);
        TextView tvChapter = view.findViewById(R.id.tv_chapter_title);
        tvTitle.setText("Đang tải...");
        tvChapter.setText("");

        LinearLayout btnSettings = view.findViewById(R.id.btnSettings);
        btnSettings.setOnClickListener(v -> showSettingsBottomSheet());
        
        // Navigation buttons
        View btnPrev = view.findViewById(R.id.btn_previous_chapter);
        View btnList = view.findViewById(R.id.btn_chapter_list);
        View btnNext = view.findViewById(R.id.btn_next_chapter);
        
        btnPrev.setOnClickListener(v -> {
            Integer prevId = viewModel.getActivePrevChapterId();
            if (prevId != null && prevId > 0) {
                viewModel.loadSpecificChapter(prevId);
            } else {
                Toast.makeText(requireContext(), "Đây là chương đầu tiên!", Toast.LENGTH_SHORT).show();
            }
        });
        
        btnList.setOnClickListener(v -> {
            showChapterListBottomSheet();
            viewModel.fetchChapterList();
        });
        
        btnNext.setOnClickListener(v -> {
            Integer nextId = viewModel.getPendingNextChapterId();
            if (nextId != null && nextId > 0) {
                viewModel.loadSpecificChapter(nextId);
            } else {
                Toast.makeText(requireContext(), "Đây là chương mới nhất!", Toast.LENGTH_SHORT).show();
            }
        });

        recyclerView = view.findViewById(R.id.recyclerViewNovel);
        adapter = new TextNovelAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        applyTheme(); // Áp dụng theme ban đầu (Background của FrameLayout)

        // Setup listener
        adapter.setOnPaywallUnlockClickListener(chapterId -> {
            Toast.makeText(requireContext(), "Cần tích hợp IAP hoặc coin để mở khóa!", Toast.LENGTH_SHORT).show();
        });

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (layoutManager == null) return;

                // Tự động ẩn UI khi cuộn xuống
                if (dy > 10 && isUiVisible) {
                    toggleSystemUI();
                }

                int lastVisible = layoutManager.findLastVisibleItemPosition();
                int totalItems = layoutManager.getItemCount();

                // 1. Phân trang: Còn cách đáy 15 paragraph thì gọi LoadNext
                if (totalItems > 0 && lastVisible >= totalItems - 15) {
                    viewModel.loadNextChapterIfNeeded();
                }

                // 2. Lưu trạng thái: Update Room debounce
                int firstVisible = layoutManager.findFirstVisibleItemPosition();
                if (firstVisible != RecyclerView.NO_POSITION) {
                    TextNovelItem visibleItem = adapter.getItem(firstVisible);
                    if (visibleItem instanceof TextNovelItem.ParagraphItem) {
                        TextNovelItem.ParagraphItem pItem = (TextNovelItem.ParagraphItem) visibleItem;
                        viewModel.saveReadingProgress(pItem.getChapterId(), pItem.getParagraphIndex());
                        trackViewForChapter(pItem.getChapterId());
                    }
                }
            }
        });

        // Toggle UI Hide/Show bằng GestureDetector
        android.view.GestureDetector gestureDetector = new android.view.GestureDetector(requireContext(), new android.view.GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapConfirmed(android.view.MotionEvent e) {
                toggleSystemUI();
                return true;
            }
        });

        recyclerView.setOnTouchListener((v, event) -> {
            gestureDetector.onTouchEvent(event);
            return false;
        });

        observeViewModel();
        
        viewTrackingTimer = new ViewTrackingTimer();
    }

    private void trackViewForChapter(int chapterId) {
        if (chapterId != currentlyTrackedChapterId) {
            currentlyTrackedChapterId = chapterId;
            viewTrackingTimer.startTimer(viewModel.getComicId(), chapterId, new ViewTrackingTimer.OnTimerCompletedListener() {
                @Override
                public void onTimerCompleted(int cId, int chId) {
                    viewModel.trackChapterView(chId);
                }
            });
        }
    }

    private void observeViewModel() {
        viewModel.isLoading().observe(getViewLifecycleOwner(), isLoading -> {
            loadingOverlay.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });

        viewModel.errorEvent().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null && !msg.isEmpty()) {
                Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.appendItemsEvent().observe(getViewLifecycleOwner(), items -> {
            if (items != null && !items.isEmpty()) {
                recyclerView.post(() -> {
                    adapter.removeLoadingItem();
                    adapter.appendItems(items);
                });
                
                // Tiêu thụ lệnh cuộn sau khi load thành công
                Integer pendingScroll = viewModel.consumePendingScrollPosition();
                if (pendingScroll != null) {
                    recyclerView.post(() -> {
                         // Find item theo paragraphIndex (sẽ rườm rà nếu chưa map đủ, ta tạm cuộn tới item thứ `pendingScroll` trong RV)
                         // Thường `pendingScroll` là index trong Array S3. Nằm ngẫu nhiên ở cuối list.
                         // Giản lược: cuộn tương đối nếu chỉ load 1 mảng ban đầu.
                         recyclerView.scrollToPosition(pendingScroll + 1); // +1 do Title
                    });
                }
            }
        });

        viewModel.clearItemsEvent().observe(getViewLifecycleOwner(), clear -> {
            if (Boolean.TRUE.equals(clear)) {
                adapter.clearItems();
            }
        });

        viewModel.currentChapterEvent().observe(getViewLifecycleOwner(), chapter -> {
            if (chapter != null) {
                View root = getView();
                if (root != null) {
                    TextView tvTitle = root.findViewById(R.id.tv_novel_title);
                    TextView tvChapter = root.findViewById(R.id.tv_chapter_title);
                    
                    if (tvTitle != null) tvTitle.setText("Đọc truyện");
                    if (tvChapter != null) {
                        String chapTitle = chapter.getTitle();
                        if (chapTitle != null && !chapTitle.toLowerCase().contains("chương") && !chapTitle.toLowerCase().contains("chap")) {
                            chapTitle = "Chương " + chapTitle;
                        }
                        tvChapter.setText(chapTitle);
                    }
                }
            }
        });

        viewModel.chapterListEvent().observe(getViewLifecycleOwner(), chapters -> {
            if (chapters != null && chapterListSheet != null) {
                chapterListAdapter = new BottomSheetChapterAdapter(chapters, chapterId -> {
                    chapterListSheet.dismiss();
                    viewModel.loadSpecificChapter(chapterId); // Load the chosen chapter
                });
                RecyclerView rv = chapterListSheet.findViewById(R.id.rv_chapter_list);
                if (rv != null) {
                    rv.setAdapter(chapterListAdapter);
                }
            }
        });
    }

    private void showChapterListBottomSheet() {
        if (chapterListSheet == null) {
            chapterListSheet = new BottomSheetDialog(requireContext());
            View sheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_chapter_list, null);
            chapterListSheet.setContentView(sheetView);

            // Bắt buộc sheet mở to
            chapterListSheet.getBehavior().setPeekHeight(getResources().getDisplayMetrics().heightPixels);

            RecyclerView rvChapters = sheetView.findViewById(R.id.rv_chapter_list);
            rvChapters.setLayoutManager(new LinearLayoutManager(requireContext()));
            
            chapterListAdapter = new BottomSheetChapterAdapter(new java.util.ArrayList<>(), chapterId -> {
                chapterListSheet.dismiss();
                viewModel.loadSpecificChapter(chapterId);
            });
            rvChapters.setAdapter(chapterListAdapter);

            View btnClose = sheetView.findViewById(R.id.btn_close_sheet);
            if (btnClose != null) {
                btnClose.setOnClickListener(v -> chapterListSheet.dismiss());
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

    private void showSettingsBottomSheet() {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        View view = getLayoutInflater().inflate(R.layout.bottom_sheet_novel_settings, null);
        dialog.setContentView(view);

        TextView tvSize = view.findViewById(R.id.tvCurrentSize);
        tvSize.setText(String.valueOf((int) currentTextSize));

        Button btnDec = view.findViewById(R.id.btnSizeDecrease);
        Button btnInc = view.findViewById(R.id.btnSizeIncrease);
        btnDec.setOnClickListener(v -> {
            if (currentTextSize > 12) {
                currentTextSize -= 2;
                tvSize.setText(String.valueOf((int) currentTextSize));
                saveSettings();
            }
        });
        btnInc.setOnClickListener(v -> {
            if (currentTextSize < 32) {
                currentTextSize += 2;
                tvSize.setText(String.valueOf((int) currentTextSize));
                saveSettings();
            }
        });

        Button btnLight = view.findViewById(R.id.btnThemeLight);
        Button btnSepia = view.findViewById(R.id.btnThemeSepia);
        Button btnDark = view.findViewById(R.id.btnThemeDark);

        btnLight.setOnClickListener(v -> { currentTheme = 0; saveSettings(); });
        btnSepia.setOnClickListener(v -> { currentTheme = 1; saveSettings(); });
        btnDark.setOnClickListener(v -> { currentTheme = 2; saveSettings(); });

        dialog.show();
    }

    private void saveSettings() {
        prefs.edit()
            .putFloat("text_size", currentTextSize)
            .putInt("theme", currentTheme)
            .apply();
        applyTheme();
    }

    private void applyTheme() {
        View root = getView();
        if (root == null) return;
        
        switch (currentTheme) {
            case 0: // Light
                root.setBackgroundColor(Color.parseColor("#FFFFFF"));
                currentTextColor = Color.parseColor("#333333");
                break;
            case 1: // Sepia
                root.setBackgroundColor(Color.parseColor("#F4ECD8"));
                currentTextColor = Color.parseColor("#5B4636");
                break;
            case 2: // Dark
                root.setBackgroundColor(Color.parseColor("#222222"));
                currentTextColor = Color.parseColor("#E0E0E0");
                break;
        }

        adapter.updateSettings(currentTextSize, currentTextColor);
    }
    
    private void toggleSystemUI() {
        isUiVisible = !isUiVisible;
        if (isUiVisible) {
            appBarLayout.animate().translationY(0f).alpha(1f).setDuration(250).start();
            bottomBar.animate().translationY(0f).alpha(1f).setDuration(250).start();
        } else {
            appBarLayout.animate().translationY(-appBarLayout.getHeight()).alpha(0f).setDuration(250).start();
            bottomBar.animate().translationY(bottomBar.getHeight()).alpha(0f).setDuration(250).start();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (viewTrackingTimer != null) {
            viewTrackingTimer.cancelTimer();
        }
    }
}
