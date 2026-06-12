package com.example.comicversev1.presentation.history;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.ConcatAdapter;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.comicversev1.R;
import com.example.comicversev1.databinding.FragmentHistoryBinding;
import com.example.comicversev1.presentation.home.ShelfAdapter;
import com.example.comicversev1.presentation.shared.adapter.ShelfSectionAdapter;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class HistoryFragment extends Fragment {

    private FragmentHistoryBinding binding;
    private HistoryViewModel viewModel;

    private ShelfAdapter comicAdapter;
    private ShelfAdapter novelAdapter;

    public HistoryFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHistoryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(HistoryViewModel.class);

        setupToolbar();
        setupRecyclerView();
        setupPullToRefresh();
        setupBottomNav();
        observeViewModel();
    }

    private void setupPullToRefresh() {
        binding.swipeRefresh.setColorSchemeResources(R.color.brand_primary, R.color.brand_secondary);
        binding.swipeRefresh.setProgressBackgroundColorSchemeResource(R.color.bg_dark_surface_elevated);
        binding.swipeRefresh.setOnRefreshListener(() -> viewModel.refresh());
    }

    private void setupToolbar() {
        binding.toolbar.setNavigationIcon(androidx.appcompat.R.drawable.abc_ic_ab_back_material);
        binding.toolbar.setNavigationOnClickListener(v -> NavHostFragment.findNavController(this).navigateUp());
        
        binding.toolbar.inflateMenu(R.menu.menu_history);
        binding.toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_clear_history) {
                showClearHistoryConfirmDialog();
                return true;
            }
            return false;
        });
    }

    private void showClearHistoryConfirmDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Xoá lịch sử")
                .setMessage("Bạn có chắc chắn muốn xoá toàn bộ lịch sử đọc?")
                .setPositiveButton("Xoá", (dialog, which) -> {
                    viewModel.clearAllHistory();
                })
                .setNegativeButton("Huỷ", null)
                .show();
    }

    private void setupBottomNav() {
        // Màn hình này có thể đi từ Home -> History nên ta không highlight tab nào, 
        // hoặc để mặc định như cũ.
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.menu_home) {
                NavHostFragment.findNavController(this).navigate(R.id.homeFragment);
                return true;
            }
            if (item.getItemId() == R.id.menu_novel) {
                NavHostFragment.findNavController(this).navigate(R.id.novelFragment);
                return true;
            }
            if (item.getItemId() == R.id.menu_favorite) {
                NavHostFragment.findNavController(this).navigate(R.id.favoriteFragment);
                return true;
            }
            if (item.getItemId() == R.id.menu_more) {
                NavHostFragment.findNavController(this).navigate(R.id.profileFragment);
                return true;
            }
            return false;
        });
    }

    private void setupRecyclerView() {
        comicAdapter = new ShelfAdapter(item -> {
            NavHostFragment.findNavController(this).navigate(
                com.example.comicversev1.NavGraphDirections.actionGlobalComicDetail(item.getSlug(), 0)
            );
        });

        novelAdapter = new ShelfAdapter(item -> {
            NavHostFragment.findNavController(this).navigate(
                com.example.comicversev1.NavGraphDirections.actionGlobalNovelDetail(0, item.getSlug())
            );
        });

        ShelfSectionAdapter comicSection = new ShelfSectionAdapter(
                "Lịch Sử Truyện Tranh", comicAdapter,
                new androidx.recyclerview.widget.LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
        );

        ShelfSectionAdapter novelSection = new ShelfSectionAdapter(
                "Lịch Sử Truyện Chữ", novelAdapter,
                new androidx.recyclerview.widget.LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
        );

        ConcatAdapter concatAdapter = new ConcatAdapter(comicSection, novelSection);
        binding.recyclerHistory.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerHistory.setAdapter(concatAdapter);
    }

    private void observeViewModel() {
        viewModel.comicHistory().observe(getViewLifecycleOwner(), history -> {
            comicAdapter.submitList(history);
            checkEmptyState();
        });

        viewModel.novelHistory().observe(getViewLifecycleOwner(), history -> {
            novelAdapter.submitList(history);
            checkEmptyState();
        });

        viewModel.refreshing().observe(getViewLifecycleOwner(), refreshing ->
                binding.swipeRefresh.setRefreshing(Boolean.TRUE.equals(refreshing)));
    }

    private void checkEmptyState() {
        boolean noComics = viewModel.comicHistory().getValue() == null || viewModel.comicHistory().getValue().isEmpty();
        boolean noNovels = viewModel.novelHistory().getValue() == null || viewModel.novelHistory().getValue().isEmpty();

        if (noComics && noNovels) {
            binding.tvEmpty.setVisibility(View.VISIBLE);
            binding.recyclerHistory.setVisibility(View.GONE);
            binding.toolbar.getMenu().findItem(R.id.action_clear_history).setVisible(false);
        } else {
            binding.tvEmpty.setVisibility(View.GONE);
            binding.recyclerHistory.setVisibility(View.VISIBLE);
            binding.toolbar.getMenu().findItem(R.id.action_clear_history).setVisible(true);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
