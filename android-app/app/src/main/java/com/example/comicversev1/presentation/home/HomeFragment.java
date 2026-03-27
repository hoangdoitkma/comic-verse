package com.example.comicversev1.presentation.home;

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
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.comicversev1.R;
import com.example.comicversev1.databinding.FragmentHomeBinding;
import com.example.comicversev1.presentation.shared.adapter.AdSearchSectionAdapter;

import com.example.comicversev1.presentation.shared.adapter.HeroSectionAdapter;
import com.example.comicversev1.presentation.shared.adapter.QuickActionSectionAdapter;
import com.example.comicversev1.presentation.shared.adapter.ShelfSectionAdapter;
import com.example.comicversev1.domain.entity.HomeContent;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private HomeViewModel viewModel;

    // Child Adapters
    private HomeHeroAdapter heroAdapter;
    private HomeQuickActionAdapter quickActionAdapter;
    private RecentAdapter recentAdapter;
    private ShelfAdapter recommendAdapter;
    private ShelfAdapter newUpdateAdapter;
    private HotAdapter hotAdapter;
    private LargeCardAdapter completedAdapter;
    private LargeCardAdapter newComicsAdapter;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    private void setupRecyclerView() {
        // Initialize child adapters
        heroAdapter = new HomeHeroAdapter();
        quickActionAdapter = new HomeQuickActionAdapter();
        recentAdapter = new RecentAdapter();
        recommendAdapter = new ShelfAdapter(item -> {
            NavHostFragment.findNavController(this).navigate(HomeFragmentDirections.actionHomeToDetail(item.getSlug(), 0));
        });
        newUpdateAdapter = new ShelfAdapter(item -> {
            NavHostFragment.findNavController(this).navigate(HomeFragmentDirections.actionHomeToDetail(item.getSlug(), 0));
        });
        hotAdapter = new HotAdapter();
        completedAdapter = new LargeCardAdapter(true);
        newComicsAdapter = new LargeCardAdapter(false);

        // Initialize wrapper section adapters
        AdSearchSectionAdapter adSearchSection = new AdSearchSectionAdapter();
        HeroSectionAdapter heroSection = new HeroSectionAdapter(heroAdapter);
        QuickActionSectionAdapter quickActionSection = new QuickActionSectionAdapter(quickActionAdapter);


        ShelfSectionAdapter recentSection = new ShelfSectionAdapter("Bạn vừa đọc", recentAdapter, new LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false));
        ShelfSectionAdapter recommendSection = new ShelfSectionAdapter("Gợi ý truyện tranh", recommendAdapter, new LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false));
        ShelfSectionAdapter newUpdateSection = new ShelfSectionAdapter("Mới cập nhật", newUpdateAdapter, new LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false));
        ShelfSectionAdapter hotSection = new ShelfSectionAdapter("Truyện tranh HOT nhất", hotAdapter, new GridLayoutManager(requireContext(), 3));
        ShelfSectionAdapter completedSection = new ShelfSectionAdapter("Đã hoàn thành", completedAdapter, new LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false));
        ShelfSectionAdapter newComicsSection = new ShelfSectionAdapter("Truyện tranh mới", newComicsAdapter, new LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false));

        // Create ConcatAdapter
        ConcatAdapter concatAdapter = new ConcatAdapter(
                adSearchSection,
                heroSection,
                quickActionSection,
                recentSection,
                recommendSection,
                newUpdateSection,
                hotSection,
                completedSection,
                newComicsSection
        );

        binding.recyclerMain.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerMain.setAdapter(concatAdapter);
    }

    private void observeState() {
        viewModel.getUiState().observe(getViewLifecycleOwner(), state -> {
            binding.textGreetingTitle.setText(state.getGreetingTitle());
            binding.textGreetingSubtitle.setText(state.getGreetingSubtitle());
            
            heroAdapter.submitList(state.getHeroes());
            quickActionAdapter.submitList(state.getQuickActions());
            

            
            recentAdapter.submitList(state.getRecentlyRead());
            recommendAdapter.submitList(state.getRecommendations());
            newUpdateAdapter.submitList(state.getNewUpdates());
            hotAdapter.submitList(state.getHotComics());
            completedAdapter.submitList(state.getCompleted());
            newComicsAdapter.submitList(state.getNewComics());
            
            if (state.getErrorMessage() != null && !state.getErrorMessage().isEmpty()) {
                android.widget.Toast.makeText(requireContext(), state.getErrorMessage(), android.widget.Toast.LENGTH_LONG).show();
            }

            // To force update the ConcatAdapter visibility checks
            binding.recyclerMain.getAdapter().notifyDataSetChanged();
        });

        // Observe local reading history (from Room DB + API enrichment)
        viewModel.recentlyReadCards().observe(getViewLifecycleOwner(), cards -> {
            if (cards != null && !cards.isEmpty()) {
                recentAdapter.submitList(cards);
            }
        });
    }

    private void setupBottomNav() {
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.menu_home) {
                return true;
            }
            if (item.getItemId() == R.id.menu_novel) {
                NavHostFragment.findNavController(this).navigate(R.id.novelFragment);
                return true;
            }
            return true;
        });
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        setupRecyclerView();
        setupBottomNav();
        observeState();
    }
}
