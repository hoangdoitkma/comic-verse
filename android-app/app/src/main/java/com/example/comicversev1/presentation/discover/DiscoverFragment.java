package com.example.comicversev1.presentation.discover;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.ConcatAdapter;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.comicversev1.R;
import com.example.comicversev1.data.model.GenreDTO;
import com.example.comicversev1.data.model.HotSearchDTO;
import com.example.comicversev1.data.model.SearchHistoryItemDTO;
import com.example.comicversev1.databinding.FragmentDiscoverBinding;
import com.example.comicversev1.domain.entity.ComicEntity;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.chip.Chip;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class DiscoverFragment extends Fragment {

    private FragmentDiscoverBinding binding;
    private DiscoverViewModel viewModel;
    private SearchResultAdapter comicResultAdapter;
    private SearchResultAdapter novelResultAdapter;
    private final Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable pendingSearchRunnable;
    private String contentType;
    private boolean hasRecentSearches;
    private boolean hasHotSearches;
    private boolean isLoading;

    private List<FilterOption> countryOptions = new ArrayList<>();
    private List<FilterOption> genreOptions = new ArrayList<>();
    private List<FilterOption> statusOptions = new ArrayList<>();
    private FilterOption selectedCountry;
    private FilterOption selectedGenre;
    private FilterOption selectedStatus;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentDiscoverBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(DiscoverViewModel.class);
        setupList();
        setupPullToRefresh();
        setupSearch();
        setupFilters();
        setupSuggestionActions();
        observeResults();
        observeSearchSuggestions();
        readInitialArgs();
        viewModel.loadSearchSuggestions(null);
        viewModel.loadGenres();
        updateSuggestionsVisibility();
    }

    private void readInitialArgs() {
        Bundle args = getArguments();
        contentType = null;
        if (args == null) return;

        String initialQuery = args.getString("initialQuery");
        if (initialQuery != null && !initialQuery.trim().isEmpty()) {
            binding.inputSearch.setText(initialQuery);
            binding.inputSearch.setSelection(binding.inputSearch.getText() != null ? binding.inputSearch.getText().length() : 0);
            if (pendingSearchRunnable != null) {
                searchHandler.removeCallbacks(pendingSearchRunnable);
            }
            viewModel.submitSearch(initialQuery, null);
        }
    }

    private void setupList() {
        comicResultAdapter = new SearchResultAdapter(this::openComicDetail);
        novelResultAdapter = new SearchResultAdapter(this::openNovelDetail);

        ConcatAdapter concatAdapter = new ConcatAdapter(
                new SearchResultSectionAdapter(
                        "Kết quả Truyện tranh",
                        comicResultAdapter,
                        new LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false),
                        true),
                new SearchResultSectionAdapter(
                        "Kết quả Truyện chữ",
                        novelResultAdapter,
                        new LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false),
                        true)
        );

        binding.recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerView.setAdapter(concatAdapter);
    }

    private void setupPullToRefresh() {
        binding.swipeRefresh.setColorSchemeResources(R.color.brand_primary, R.color.brand_secondary);
        binding.swipeRefresh.setProgressBackgroundColorSchemeResource(R.color.bg_dark_surface_elevated);
        binding.swipeRefresh.setOnRefreshListener(() -> viewModel.refreshResults());
    }

    private void setupSearch() {
        binding.inputSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                runSearchNow();
                return true;
            }
            return false;
        });

        binding.inputSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (pendingSearchRunnable != null) {
                    searchHandler.removeCallbacks(pendingSearchRunnable);
                }
                pendingSearchRunnable = () -> viewModel.search(s != null ? s.toString() : null, null);
                searchHandler.postDelayed(pendingSearchRunnable, 350);
                updateSuggestionsVisibility();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void runSearchNow() {
        if (pendingSearchRunnable != null) {
            searchHandler.removeCallbacks(pendingSearchRunnable);
        }
        viewModel.submitSearch(binding.inputSearch.getText() != null ? binding.inputSearch.getText().toString() : null, null);
        updateSuggestionsVisibility();
    }

    private void setupSuggestionActions() {
        binding.btnClearRecentSearches.setOnClickListener(v -> viewModel.clearSearchHistory(contentType));
    }

    private void setupFilters() {
        countryOptions = new ArrayList<>(Arrays.asList(
                new FilterOption("Tất cả", null, null),
                new FilterOption("Nhật Bản", "JAPAN", null),
                new FilterOption("Hàn Quốc", "KOREA", null),
                new FilterOption("Trung Quốc", "CHINA", null),
                new FilterOption("Việt Nam", "VIETNAM", null),
                new FilterOption("Quốc tế", "GLOBAL", null)
        ));
        genreOptions = new ArrayList<>();
        genreOptions.add(new FilterOption("Tất cả", null, null));
        statusOptions = new ArrayList<>(Arrays.asList(
                new FilterOption("Tất cả", null, null),
                new FilterOption("Đang ra", "ONGOING", null),
                new FilterOption("Hoàn thành", "COMPLETED", null)
        ));

        selectedCountry = countryOptions.get(0);
        selectedGenre = genreOptions.get(0);
        selectedStatus = statusOptions.get(0);

        binding.chipCountryFilter.setOnClickListener(v ->
                showFilterSheet("Quốc gia", countryOptions, selectedCountry, false, option -> {
                    selectedCountry = option;
                    onFilterChanged();
                }));
        binding.chipGenreFilter.setOnClickListener(v ->
                showFilterSheet("Thể loại", genreOptions, selectedGenre, true, option -> {
                    selectedGenre = option;
                    onFilterChanged();
                }));
        binding.chipStatusFilter.setOnClickListener(v ->
                showFilterSheet("Trạng thái", statusOptions, selectedStatus, false, option -> {
                    selectedStatus = option;
                    onFilterChanged();
                }));

        binding.chipCountryFilter.setOnCloseIconClickListener(v -> {
            selectedCountry = countryOptions.get(0);
            onFilterChanged();
        });
        binding.chipGenreFilter.setOnCloseIconClickListener(v -> {
            selectedGenre = genreOptions.get(0);
            onFilterChanged();
        });
        binding.chipStatusFilter.setOnCloseIconClickListener(v -> {
            selectedStatus = statusOptions.get(0);
            onFilterChanged();
        });

        updateFilterChips();
        viewModel.genres().observe(getViewLifecycleOwner(), this::renderGenres);
    }

    private void observeResults() {
        viewModel.comicResults().observe(getViewLifecycleOwner(), results -> {
            comicResultAdapter.submitList(results);
            updateResultsState();
        });
        viewModel.novelResults().observe(getViewLifecycleOwner(), results -> {
            novelResultAdapter.submitList(results);
            updateResultsState();
        });
        viewModel.refreshing().observe(getViewLifecycleOwner(), refreshing -> {
            isLoading = Boolean.TRUE.equals(refreshing);
            binding.swipeRefresh.setRefreshing(isLoading);
            updateResultsState();
        });
    }

    private void observeSearchSuggestions() {
        viewModel.searchHistory().observe(getViewLifecycleOwner(), this::renderRecentSearches);
        viewModel.hotSearches().observe(getViewLifecycleOwner(), this::renderHotSearches);
    }

    private void renderRecentSearches(List<SearchHistoryItemDTO> items) {
        if (binding == null) return;
        binding.chipRecentSearches.removeAllViews();
        hasRecentSearches = false;
        if (items != null) {
            for (SearchHistoryItemDTO item : items) {
                if (item == null || item.keyword == null || item.keyword.trim().isEmpty()) {
                    continue;
                }
                hasRecentSearches = true;
                binding.chipRecentSearches.addView(createSearchChip(item.keyword.trim(), true));
            }
        }
        updateSuggestionsVisibility();
    }

    private void renderHotSearches(List<HotSearchDTO> items) {
        if (binding == null) return;
        binding.chipHotSearches.removeAllViews();
        hasHotSearches = false;
        if (items != null) {
            for (HotSearchDTO item : items) {
                if (item == null || item.keyword == null || item.keyword.trim().isEmpty()) {
                    continue;
                }
                hasHotSearches = true;
                binding.chipHotSearches.addView(createSearchChip(item.keyword.trim(), false));
            }
        }
        updateSuggestionsVisibility();
    }

    private Chip createSearchChip(String keyword, boolean allowDelete) {
        Chip chip = new Chip(requireContext());
        chip.setText(keyword);
        chip.setSingleLine(true);
        chip.setCheckable(false);
        chip.setTextColor(ColorStateList.valueOf(Color.parseColor("#F8FAFC")));
        chip.setChipBackgroundColor(ColorStateList.valueOf(Color.parseColor("#202633")));
        chip.setChipStrokeColor(ColorStateList.valueOf(Color.parseColor("#2E3948")));
        chip.setChipStrokeWidth(1f);
        chip.setCloseIconVisible(allowDelete);
        chip.setOnClickListener(v -> applySearchSuggestion(keyword));
        if (allowDelete) {
            chip.setOnCloseIconClickListener(v -> viewModel.deleteSearchKeyword(keyword, contentType));
        }
        return chip;
    }

    private void renderGenres(List<GenreDTO> genres) {
        if (binding == null) return;
        Integer selectedGenreId = selectedGenre != null ? selectedGenre.id : null;
        List<FilterOption> options = new ArrayList<>();
        options.add(new FilterOption("Tất cả", null, null));
        if (genres != null) {
            for (GenreDTO genre : genres) {
                if (genre == null || genre.name == null || genre.name.trim().isEmpty()) {
                    continue;
                }
                options.add(new FilterOption(genre.name.trim(), null, genre.id));
            }
        }
        genreOptions = options;
        selectedGenre = findOptionById(genreOptions, selectedGenreId);
        if (selectedGenre == null) {
            selectedGenre = genreOptions.get(0);
        }
        updateFilterChips();
    }

    private void onFilterChanged() {
        updateFilterChips();
        applyFilters();
    }

    private void applyFilters() {
        viewModel.updateFilters(
                selectedCountry != null ? selectedCountry.value : null,
                selectedGenre != null ? selectedGenre.id : null,
                selectedStatus != null ? selectedStatus.value : null
        );
    }

    private void updateFilterChips() {
        updateFilterChip(binding.chipCountryFilter, "Quốc gia", selectedCountry);
        updateFilterChip(binding.chipGenreFilter, "Thể loại", selectedGenre);
        updateFilterChip(binding.chipStatusFilter, "Trạng thái", selectedStatus);
    }

    private void updateFilterChip(Chip chip, String title, FilterOption option) {
        boolean active = isActive(option);
        chip.setText(title + ": " + (option != null ? option.label : "Tất cả"));
        chip.setCloseIconVisible(active);
        chip.setTextColor(ColorStateList.valueOf(Color.parseColor(active ? "#FFFFFF" : "#D6DDF0")));
        chip.setChipBackgroundColor(ColorStateList.valueOf(Color.parseColor(active ? "#1D2D3E" : "#202633")));
        chip.setChipStrokeColor(ColorStateList.valueOf(Color.parseColor(active ? "#4FC3F7" : "#2E3948")));
        chip.setChipStrokeWidth(1f);
        chip.setCloseIconTint(ColorStateList.valueOf(Color.parseColor("#D6DDF0")));
    }

    private boolean isActive(FilterOption option) {
        return option != null && (option.value != null || option.id != null);
    }

    private FilterOption findOptionById(List<FilterOption> options, Integer id) {
        if (id == null || options == null) {
            return null;
        }
        for (FilterOption option : options) {
            if (option != null && id.equals(option.id)) {
                return option;
            }
        }
        return null;
    }

    private void showFilterSheet(String title,
                                 List<FilterOption> options,
                                 FilterOption selected,
                                 boolean searchable,
                                 FilterSelectListener listener) {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        LinearLayout root = new LinearLayout(requireContext());
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(20), dp(16), dp(20), dp(20));
        root.setBackgroundColor(Color.parseColor("#171A22"));

        TextView titleView = new TextView(requireContext());
        titleView.setText(title);
        titleView.setTextColor(Color.parseColor("#FFFFFF"));
        titleView.setTextSize(18f);
        titleView.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        root.addView(titleView, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        FilterOptionAdapter adapter = new FilterOptionAdapter(option -> {
            listener.onSelected(option);
            dialog.dismiss();
        });
        adapter.submitOptions(options, selected);

        if (searchable) {
            EditText searchInput = new EditText(requireContext());
            searchInput.setSingleLine(true);
            searchInput.setHint("Tìm thể loại");
            searchInput.setTextColor(Color.parseColor("#F8FAFC"));
            searchInput.setHintTextColor(Color.parseColor("#8D95A6"));
            searchInput.setTextSize(14f);
            searchInput.setPadding(dp(14), 0, dp(14), 0);
            searchInput.setBackgroundResource(R.drawable.bg_detail_comment_input);
            LinearLayout.LayoutParams searchParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    dp(46));
            searchParams.topMargin = dp(14);
            root.addView(searchInput, searchParams);
            searchInput.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                    adapter.filter(s != null ? s.toString() : null);
                }
                @Override public void afterTextChanged(Editable s) {}
            });
        }

        RecyclerView recyclerView = new RecyclerView(requireContext());
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);
        recyclerView.setOverScrollMode(View.OVER_SCROLL_NEVER);
        LinearLayout.LayoutParams listParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(searchable ? 360 : 280));
        listParams.topMargin = dp(12);
        root.addView(recyclerView, listParams);

        dialog.setContentView(root);
        dialog.show();
        View bottomSheet = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
        if (bottomSheet != null) {
            bottomSheet.setBackgroundColor(Color.parseColor("#171A22"));
        }
    }

    private void applySearchSuggestion(String keyword) {
        if (binding == null) return;
        if (pendingSearchRunnable != null) {
            searchHandler.removeCallbacks(pendingSearchRunnable);
        }
        binding.inputSearch.setText(keyword);
        if (binding.inputSearch.getText() != null) {
            binding.inputSearch.setSelection(binding.inputSearch.getText().length());
        }
        runSearchNow();
    }

    private void updateSuggestionsVisibility() {
        if (binding == null) return;
        boolean hasQuery = binding.inputSearch.getText() != null
                && !binding.inputSearch.getText().toString().trim().isEmpty();
        boolean showSuggestions = !hasQuery && (hasRecentSearches || hasHotSearches);

        binding.searchSuggestionsContainer.setVisibility(showSuggestions ? View.VISIBLE : View.GONE);
        binding.recentSearchSection.setVisibility(showSuggestions && hasRecentSearches ? View.VISIBLE : View.GONE);
        binding.recentSearchScroll.setVisibility(showSuggestions && hasRecentSearches ? View.VISIBLE : View.GONE);
        binding.hotSearchTitle.setVisibility(showSuggestions && hasHotSearches ? View.VISIBLE : View.GONE);
        binding.hotSearchScroll.setVisibility(showSuggestions && hasHotSearches ? View.VISIBLE : View.GONE);
    }

    private void updateResultsState() {
        if (binding == null || comicResultAdapter == null || novelResultAdapter == null) return;
        boolean hasResults = comicResultAdapter.getItemCount() > 0 || novelResultAdapter.getItemCount() > 0;
        binding.progressBar.setVisibility(isLoading && !hasResults ? View.VISIBLE : View.GONE);
        binding.emptyResults.setVisibility(!isLoading && !hasResults ? View.VISIBLE : View.GONE);
        binding.recyclerView.setVisibility(!isLoading || hasResults ? View.VISIBLE : View.GONE);
    }

    private void openComicDetail(ComicEntity comic) {
        NavHostFragment.findNavController(this).navigate(
                com.example.comicversev1.NavGraphDirections.actionGlobalComicDetail(comic.getSlug(), comic.getId()));
    }

    private void openNovelDetail(ComicEntity novel) {
        NavHostFragment.findNavController(this).navigate(
                com.example.comicversev1.NavGraphDirections.actionGlobalNovelDetail(novel.getId(), novel.getSlug()));
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    private static class FilterOption {
        final String label;
        final String value;
        final Integer id;

        FilterOption(String label, String value, Integer id) {
            this.label = label;
            this.value = value;
            this.id = id;
        }
    }

    private interface FilterSelectListener {
        void onSelected(FilterOption option);
    }

    private class FilterOptionAdapter extends RecyclerView.Adapter<FilterOptionAdapter.ViewHolder> {
        private final FilterSelectListener listener;
        private final List<FilterOption> source = new ArrayList<>();
        private final List<FilterOption> visible = new ArrayList<>();
        private FilterOption selected;

        FilterOptionAdapter(FilterSelectListener listener) {
            this.listener = listener;
        }

        void submitOptions(List<FilterOption> options, FilterOption selected) {
            source.clear();
            if (options != null) {
                source.addAll(options);
            }
            this.selected = selected;
            filter(null);
        }

        void filter(String query) {
            String normalized = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
            visible.clear();
            for (FilterOption option : source) {
                if (normalized.isEmpty() || option.label.toLowerCase(Locale.ROOT).contains(normalized)) {
                    visible.add(option);
                }
            }
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            TextView textView = new TextView(parent.getContext());
            textView.setGravity(Gravity.CENTER_VERTICAL);
            textView.setMinHeight(dp(48));
            textView.setPadding(dp(14), 0, dp(14), 0);
            textView.setTextSize(15f);
            textView.setLayoutParams(new RecyclerView.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            return new ViewHolder(textView);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            FilterOption option = visible.get(position);
            boolean checked = sameOption(option, selected);
            holder.textView.setText(option.label);
            holder.textView.setTypeface(Typeface.DEFAULT, checked ? Typeface.BOLD : Typeface.NORMAL);
            holder.textView.setTextColor(Color.parseColor(checked ? "#4FC3F7" : "#F8FAFC"));
            holder.textView.setBackgroundColor(Color.parseColor(checked ? "#1D2D3E" : "#171A22"));
            holder.itemView.setOnClickListener(v -> listener.onSelected(option));
        }

        @Override
        public int getItemCount() {
            return visible.size();
        }

        private boolean sameOption(FilterOption left, FilterOption right) {
            if (left == right) return true;
            if (left == null || right == null) return false;
            if (left.id != null || right.id != null) {
                return left.id != null && left.id.equals(right.id);
            }
            if (left.value != null || right.value != null) {
                return left.value != null && left.value.equals(right.value);
            }
            return true;
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            final TextView textView;

            ViewHolder(@NonNull TextView itemView) {
                super(itemView);
                textView = itemView;
            }
        }
    }

    @Override
    public void onDestroyView() {
        if (pendingSearchRunnable != null) {
            searchHandler.removeCallbacks(pendingSearchRunnable);
        }
        binding = null;
        super.onDestroyView();
    }
}
