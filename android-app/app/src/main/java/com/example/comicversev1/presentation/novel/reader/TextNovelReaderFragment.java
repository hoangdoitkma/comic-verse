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
import android.widget.ImageButton;
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
import com.example.comicversev1.utils.AutoScrollManager; // Import AutoScrollManager

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
    private boolean isUiVisible = true; // Navbar & Bottombar hiển thị theo mặc định XML

    // Settings memory
    private SharedPreferences prefs;
    private float currentTextSize = 18f;
    private int currentTheme = 0; // 0: Light, 1: Sepia, 2: Dark
    private int currentTextColor = Color.BLACK;

    private ViewTrackingTimer viewTrackingTimer;
    private int currentlyTrackedChapterId = -1;
    
    // Auto Scroll
    private AutoScrollManager autoScrollManager;

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
        
        TextNovelReaderFragmentArgs args = TextNovelReaderFragmentArgs.fromBundle(requireArguments());
        String comicTitle = args.getComicTitle();
        tvTitle.setText(comicTitle != null ? comicTitle : "Đang tải...");
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
        
        ImageButton btnComments = view.findViewById(R.id.btn_comments);
        if (btnComments != null) {
            btnComments.setOnClickListener(v -> {
                int chapterId = currentlyTrackedChapterId;
                if (chapterId <= 0) {
                    com.example.comicversev1.domain.entity.ChapterEntity current = viewModel.currentChapterEvent().getValue();
                    if (current != null) chapterId = current.getId();
                }
                if (chapterId > 0) {
                    com.example.comicversev1.presentation.comments.CommentsBottomSheetDialogFragment.newInstance(chapterId)
                            .show(getChildFragmentManager(), "CommentsBottomSheet");
                } else {
                    Toast.makeText(requireContext(), "Đang tải chương...", Toast.LENGTH_SHORT).show();
                }
            });
        }

        
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

        // Init AutoScroll
        autoScrollManager = new AutoScrollManager(requireContext(), recyclerView);
        autoScrollManager.setListener(isScrolling -> {
            // Callback when auto-scroll state changes (e.g. paused by touch in Mode A)
        });

        applyTheme(); // Áp dụng theme ban đầu (Background của FrameLayout)

        // Setup listener
        adapter.setOnPaywallUnlockClickListener(chapterId -> {
            Toast.makeText(requireContext(), "Cần tích hợp IAP hoặc coin để mở khóa!", Toast.LENGTH_SHORT).show();
        });

        adapter.setOnParagraphLongClickListener(chapterId -> {
            com.example.comicversev1.presentation.shared.ReportChapterBottomSheet sheet = new com.example.comicversev1.presentation.shared.ReportChapterBottomSheet();
            sheet.setOnReportSubmitListener(request -> {
                viewModel.reportChapter(chapterId, request);
            });
            sheet.show(getChildFragmentManager(), "ReportChapterBottomSheet");
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
            public boolean onSingleTapUp(android.view.MotionEvent e) {
                int screenHeight = recyclerView.getHeight();
                int screenWidth = recyclerView.getWidth();
                float x = e.getX();
                float y = e.getY();
                
                if (y < screenHeight / 3.0f) {
                    // Top zone: scroll up 1 page
                    recyclerView.post(() -> recyclerView.smoothScrollBy(0, -(int)(screenHeight * 0.9)));
                } else if (y > screenHeight * 2.0f / 3.0f) {
                    // Bottom zone: scroll down 1 page
                    recyclerView.post(() -> recyclerView.smoothScrollBy(0, (int)(screenHeight * 0.9)));
                } else {
                    // Middle zone row
                    if (x < screenWidth / 3.0f) {
                        // Left zone: Previous Chapter
                        View btnPrev = getView() != null ? getView().findViewById(R.id.btn_previous_chapter) : null;
                        if (btnPrev != null) btnPrev.performClick();
                    } else if (x > screenWidth * 2.0f / 3.0f) {
                        // Right zone: Next Chapter
                        View btnNext = getView() != null ? getView().findViewById(R.id.btn_next_chapter) : null;
                        if (btnNext != null) btnNext.performClick();
                    } else {
                        // Center zone: toggle system UI
                        toggleSystemUI();
                    }
                }
                return true;
            }
        });

        recyclerView.addOnItemTouchListener(new RecyclerView.SimpleOnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull android.view.MotionEvent event) {
                gestureDetector.onTouchEvent(event);
                return false;
            }
        });

        observeViewModel();
        
        viewTrackingTimer = new ViewTrackingTimer();
    }

    private void trackViewForChapter(int chapterId) {
        if (chapterId != currentlyTrackedChapterId) {
            currentlyTrackedChapterId = chapterId;
            updateChapterTitleUi(chapterId);
            viewTrackingTimer.startTimer(viewModel.getComicId(), chapterId, new ViewTrackingTimer.OnTimerCompletedListener() {
                @Override
                public void onTimerCompleted(int cId, int chId) {
                    viewModel.trackChapterView(chId);
                }
            });
        }
    }

    private void updateChapterTitleUi(int chapterId) {
        String chapTitle = viewModel.getChapterTitleCache(chapterId);
        if (chapTitle != null) {
            View root = getView();
            if (root != null) {
                TextView tvChapter = root.findViewById(R.id.tv_chapter_title);
                if (tvChapter != null) {
                    if (!chapTitle.toLowerCase().contains("chương") && !chapTitle.toLowerCase().contains("chap")) {
                        chapTitle = "Chương " + chapTitle;
                    }
                    tvChapter.setText(chapTitle);
                }
            }
        }
    }

    private void observeViewModel() {
        viewModel.isLoading().observe(getViewLifecycleOwner(), isLoading -> {
            loadingOverlay.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            if (autoScrollManager != null) {
                if (isLoading) {
                    autoScrollManager.pauseForNetwork();
                } else {
                    autoScrollManager.resumeFromNetwork();
                }
            }
        });

        viewModel.errorEvent().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null && !msg.isEmpty()) {
                if (msg.contains("VIP_REQUIRED")) {
                    boolean isAnonymous = msg.contains("VIP_REQUIRED_ANONYMOUS");
                    com.google.android.material.bottomsheet.BottomSheetDialog bottomSheet = new com.google.android.material.bottomsheet.BottomSheetDialog(requireContext());
                    View sheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_vip_required, null);
                    bottomSheet.setContentView(sheetView);
                    
                    android.widget.TextView tvVipMessage = sheetView.findViewById(R.id.tvVipMessage);
                    android.widget.TextView tvPrimaryAction = sheetView.findViewById(R.id.tvPrimaryAction);
                    View btnPrimaryAction = sheetView.findViewById(R.id.btnPrimaryAction);
                    View btnCancel = sheetView.findViewById(R.id.btnCancel);

                    tvVipMessage.setText(isAnonymous ? "Chương này dành riêng cho tài khoản VIP. Bạn cần đăng nhập để tiếp tục." : "Chương này yêu cầu tài khoản VIP. Bạn có muốn nâng cấp tài khoản VIP ngay bây giờ không?");
                    tvPrimaryAction.setText(isAnonymous ? "Đăng nhập" : "Nâng cấp ngay");
                    
                    btnPrimaryAction.setOnClickListener(v -> {
                        bottomSheet.dismiss();
                        if (isAnonymous) {
                            androidx.navigation.fragment.NavHostFragment.findNavController(this).navigate(R.id.loginFragment);
                        } else {
                            androidx.navigation.fragment.NavHostFragment.findNavController(this).navigate(R.id.action_global_vipCenter);
                        }
                    });
                    
                    btnCancel.setOnClickListener(v -> {
                        bottomSheet.dismiss();
                        androidx.navigation.fragment.NavHostFragment.findNavController(this).navigateUp();
                    });

                    bottomSheet.setCancelable(false);
                    bottomSheet.show();
                } else {
                    Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
                }
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
                    
                    if (tvTitle != null) {
                        TextNovelReaderFragmentArgs args = TextNovelReaderFragmentArgs.fromBundle(requireArguments());
                        String cTitle = args.getComicTitle();
                        tvTitle.setText(cTitle != null ? cTitle : "Đọc truyện");
                    }
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

        viewModel.reportSuccessEvent().observe(getViewLifecycleOwner(), success -> {
            if (Boolean.TRUE.equals(success)) {
                Toast.makeText(requireContext(), "Báo cáo thành công! Cảm ơn bạn.", Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.errorEvent().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
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

        // --- Auto Scroll Settings ---
        androidx.appcompat.widget.SwitchCompat switchAutoScroll = view.findViewById(R.id.switchAutoScroll);
        View layoutAutoScrollSettings = view.findViewById(R.id.layoutAutoScrollSettings);
        android.widget.SeekBar seekBarSpeed = view.findViewById(R.id.seekBarSpeed);
        TextView tvScrollSpeedLbl = view.findViewById(R.id.tvScrollSpeedLbl);
        android.widget.RadioGroup rgConflictMode = view.findViewById(R.id.rgConflictMode);
        TextView tvAutoScrollHint = view.findViewById(R.id.tvAutoScrollHint);

        if (switchAutoScroll != null && autoScrollManager != null) {
            switchAutoScroll.setChecked(autoScrollManager.isAutoScrolling());
            layoutAutoScrollSettings.setVisibility(switchAutoScroll.isChecked() ? View.VISIBLE : View.GONE);
            
            int currentSpeed = prefs.getInt("scroll_speed", 2);
            seekBarSpeed.setProgress(currentSpeed);
            tvScrollSpeedLbl.setText("Tốc độ cuộn: " + currentSpeed);

            int conflictMode = prefs.getInt("scroll_conflict_mode", 1);
            if (conflictMode == 0) {
                rgConflictMode.check(R.id.rbStop);
                if (tvAutoScrollHint != null) tvAutoScrollHint.setText("Lưu ý: Bất kỳ thao tác chạm nào trên trang đọc cũng sẽ tắt hẳn chức năng tự động cuộn.");
            } else {
                rgConflictMode.check(R.id.rbPause);
                if (tvAutoScrollHint != null) tvAutoScrollHint.setText("Lưu ý: Tính năng cuộn sẽ dừng lại khi bạn giữ tay để đọc, và tự động cuộn tiếp sau 2 giây khi bạn thả tay.");
            }

            switchAutoScroll.setOnCheckedChangeListener((buttonView, isChecked) -> {
                layoutAutoScrollSettings.setVisibility(isChecked ? View.VISIBLE : View.GONE);
                autoScrollManager.toggle(isChecked);
                // Ẩn thanh công cụ và Settings Dialog để đọc luôn với Auto-scroll
                if (isChecked && isUiVisible) {
                    toggleSystemUI();
                    dialog.dismiss();
                }
            });

            seekBarSpeed.setOnSeekBarChangeListener(new android.widget.SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(android.widget.SeekBar seekBar, int progress, boolean fromUser) {
                    int speed = Math.max(1, progress); // Tốc độ tối thiểu là 1
                    tvScrollSpeedLbl.setText("Tốc độ cuộn: " + speed);
                    autoScrollManager.applySettings(speed, prefs.getInt("scroll_conflict_mode", 1));
                }
                @Override
                public void onStartTrackingTouch(android.widget.SeekBar seekBar) {}
                @Override
                public void onStopTrackingTouch(android.widget.SeekBar seekBar) {}
            });

            rgConflictMode.setOnCheckedChangeListener((group, checkedId) -> {
                int mode = checkedId == R.id.rbStop ? 0 : 1;
                autoScrollManager.applySettings(prefs.getInt("scroll_speed", 2), mode);
                if (tvAutoScrollHint != null) {
                    if (mode == 0) {
                        tvAutoScrollHint.setText("Lưu ý: Bất kỳ thao tác chạm nào trên trang đọc cũng sẽ tắt hẳn chức năng tự động cuộn.");
                    } else {
                        tvAutoScrollHint.setText("Lưu ý: Tính năng cuộn sẽ dừng lại khi bạn giữ tay để đọc, và tự động cuộn tiếp sau 2 giây khi bạn thả tay.");
                    }
                }
            });
        }

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
        if (autoScrollManager != null) {
            autoScrollManager.destroy();
            autoScrollManager = null;
        }
    }
}
