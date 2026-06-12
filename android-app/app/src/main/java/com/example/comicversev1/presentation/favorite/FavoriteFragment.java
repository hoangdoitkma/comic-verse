package com.example.comicversev1.presentation.favorite;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.ConcatAdapter;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.comicversev1.R;
import com.example.comicversev1.databinding.FragmentFavoriteBinding;
import com.example.comicversev1.presentation.home.ShelfAdapter;
import com.example.comicversev1.presentation.shared.adapter.ShelfSectionAdapter;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class FavoriteFragment extends Fragment {

    private FragmentFavoriteBinding binding;
    private FavoriteViewModel viewModel;

    private ShelfAdapter comicAdapter;
    private ShelfAdapter novelAdapter;

    public FavoriteFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentFavoriteBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(FavoriteViewModel.class);

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

    private void setupBottomNav() {
        binding.bottomNavigation.setSelectedItemId(R.id.menu_favorite);
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

        // Use a Wrap Content grid or horizontal list. For favorites, a horizontal list is okay for small sets, but grid is better.
        // Wait, ShelfSectionAdapter expects any LayoutManager! Let's use horizontal list for now since that's what ShelfAdapter usually has.
        // Wait, actually user prefers Grid for sections? Since it's a dedicated page, GridLayout is much better!
        // But ShelfSectionAdapter takes LayoutManager. We can pass GridLayoutManager!
        ShelfSectionAdapter comicSection = new ShelfSectionAdapter(
                "Truyện Tranh Yêu Thích", comicAdapter,
                new androidx.recyclerview.widget.LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
        );

        ShelfSectionAdapter novelSection = new ShelfSectionAdapter(
                "Truyện Chữ Yêu Thích", novelAdapter,
                new androidx.recyclerview.widget.LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
        );

        ConcatAdapter concatAdapter = new ConcatAdapter(comicSection, novelSection);
        binding.recyclerFavorite.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerFavorite.setAdapter(concatAdapter);
    }

    private void observeViewModel() {
        viewModel.comicFavorites().observe(getViewLifecycleOwner(), favorites -> {
            comicAdapter.submitList(favorites);
            checkEmptyState();
        });

        viewModel.novelFavorites().observe(getViewLifecycleOwner(), favorites -> {
            novelAdapter.submitList(favorites);
            checkEmptyState();
        });

        viewModel.refreshing().observe(getViewLifecycleOwner(), refreshing ->
                binding.swipeRefresh.setRefreshing(Boolean.TRUE.equals(refreshing)));
    }

    private void checkEmptyState() {
        boolean noComics = viewModel.comicFavorites().getValue() == null || viewModel.comicFavorites().getValue().isEmpty();
        boolean noNovels = viewModel.novelFavorites().getValue() == null || viewModel.novelFavorites().getValue().isEmpty();

        if (noComics && noNovels) {
            binding.tvEmpty.setVisibility(View.VISIBLE);
            binding.recyclerFavorite.setVisibility(View.GONE);
        } else {
            binding.tvEmpty.setVisibility(View.GONE);
            binding.recyclerFavorite.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
