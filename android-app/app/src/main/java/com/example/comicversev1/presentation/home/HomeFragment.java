package com.example.comicversev1.presentation.home;

import android.content.Context;
import android.content.SharedPreferences;
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
import com.example.comicversev1.data.repository.NotificationRepository;
import com.example.comicversev1.databinding.FragmentHomeBinding;
import com.example.comicversev1.presentation.shared.adapter.AdSearchSectionAdapter;
import com.example.comicversev1.utils.Constants;
import com.bumptech.glide.Glide;

import com.example.comicversev1.presentation.shared.adapter.HeroSectionAdapter;
import com.example.comicversev1.presentation.shared.adapter.QuickActionSectionAdapter;
import com.example.comicversev1.presentation.shared.adapter.ShelfSectionAdapter;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

@AndroidEntryPoint
public class HomeFragment extends Fragment {

    @Inject
    NotificationRepository notificationRepository;

    private FragmentHomeBinding binding;
    private HomeViewModel viewModel;
    private final CompositeDisposable disposables = new CompositeDisposable();

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
        quickActionAdapter.setOnItemClickListener(action -> {
            try {
                if ("vip".equals(action.getId())) {
                    androidx.navigation.Navigation.findNavController(requireView()).navigate(R.id.action_global_vipCenter);
                } else if ("history".equals(action.getId())) {
                    androidx.navigation.Navigation.findNavController(requireView()).navigate(R.id.action_global_historyFragment);
                }
            } catch (Exception e) {
                android.widget.Toast.makeText(requireContext(), "Không thể mở: " + e.getMessage(), android.widget.Toast.LENGTH_LONG).show();
            }
        });
        recentAdapter = new RecentAdapter();
        recentAdapter.setListener(item -> {
            NavHostFragment.findNavController(this).navigate(HomeFragmentDirections.actionHomeToDetail(item.getSlug(), 0));
        });

        recommendAdapter = new ShelfAdapter(item -> {
            NavHostFragment.findNavController(this).navigate(HomeFragmentDirections.actionHomeToDetail(item.getSlug(), 0));
        });
        
        newUpdateAdapter = new ShelfAdapter(item -> {
            NavHostFragment.findNavController(this).navigate(HomeFragmentDirections.actionHomeToDetail(item.getSlug(), 0));
        });
        
        hotAdapter = new HotAdapter();
        hotAdapter.setListener(item -> {
            NavHostFragment.findNavController(this).navigate(HomeFragmentDirections.actionHomeToDetail(item.getSlug(), 0));
        });

        completedAdapter = new LargeCardAdapter(true);
        completedAdapter.setListener(item -> {
            NavHostFragment.findNavController(this).navigate(HomeFragmentDirections.actionHomeToDetail(item.getSlug(), 0));
        });

        newComicsAdapter = new LargeCardAdapter(false);
        newComicsAdapter.setListener(item -> {
            NavHostFragment.findNavController(this).navigate(HomeFragmentDirections.actionHomeToDetail(item.getSlug(), 0));
        });

        // Initialize wrapper section adapters
        AdSearchSectionAdapter adSearchSection = new AdSearchSectionAdapter(query -> navigateToSearch(query, "COMIC"));
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

    private void setupPullToRefresh() {
        binding.swipeRefresh.setColorSchemeResources(R.color.brand_primary, R.color.brand_secondary);
        binding.swipeRefresh.setProgressBackgroundColorSchemeResource(R.color.bg_dark_surface_elevated);
        binding.swipeRefresh.setOnRefreshListener(() -> {
            viewModel.refresh();
            fetchUnreadCount();
        });
    }

    private void navigateToSearch(String query, String type) {
        Bundle args = new Bundle();
        args.putString("initialQuery", query);
        args.putString("contentType", type);
        NavHostFragment.findNavController(this).navigate(R.id.discoverFragment, args);
    }

    private void observeState() {
        viewModel.getUiState().observe(getViewLifecycleOwner(), state -> {
            binding.swipeRefresh.setRefreshing(false);
            renderUserAvatar();
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
            if (cards != null) {
                recentAdapter.submitList(cards);
                if (binding.recyclerMain.getAdapter() != null) {
                    binding.recyclerMain.getAdapter().notifyDataSetChanged();
                }
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
            if (item.getItemId() == R.id.menu_favorite) {
                NavHostFragment.findNavController(this).navigate(R.id.favoriteFragment);
                return true;
            }
            if (item.getItemId() == R.id.menu_more) {
                NavHostFragment.findNavController(this).navigate(R.id.profileFragment);
                return true;
            }
            return true;
        });
    }

    private void setupNotificationBell() {
        // Bell click → navigate to notification screen
        binding.btnBell.setOnClickListener(v -> {
            NavHostFragment.findNavController(this).navigate(R.id.action_global_notification);
        });

        // Fetch unread count
        fetchUnreadCount();
    }

    private void setupAvatarAction() {
        binding.imageAvatar.setOnClickListener(v -> {
            SharedPreferences prefs = requireActivity().getSharedPreferences(Constants.PREF_AUTH, Context.MODE_PRIVATE);
            String token = prefs.getString(Constants.KEY_ACCESS_TOKEN, "");
            if (token == null || token.isEmpty()) {
                NavHostFragment.findNavController(this).navigate(R.id.loginFragment);
            } else {
                NavHostFragment.findNavController(this).navigate(R.id.profileDetailFragment);
            }
        });
    }

    private void renderUserAvatar() {
        SharedPreferences prefs = requireActivity().getSharedPreferences(Constants.PREF_AUTH, Context.MODE_PRIVATE);
        String avatarUrl = prefs.getString(Constants.KEY_AVATAR_URL, "");
        if (avatarUrl != null && !avatarUrl.trim().isEmpty()) {
            Glide.with(this)
                    .load(avatarUrl)
                    .placeholder(R.mipmap.ic_launcher)
                    .error(R.mipmap.ic_launcher)
                    .into(binding.imageAvatar);
        } else {
            binding.imageAvatar.setImageResource(R.mipmap.ic_launcher);
        }
    }

    private void fetchUnreadCount() {
        android.content.SharedPreferences prefs = requireActivity().getSharedPreferences(
                com.example.comicversev1.utils.Constants.PREF_AUTH, android.content.Context.MODE_PRIVATE);
        String token = prefs.getString(com.example.comicversev1.utils.Constants.KEY_ACCESS_TOKEN, "");
        
        // Only fetch if user is logged in
        if (token.isEmpty()) {
            binding.textBadge.setVisibility(View.GONE);
            return;
        }

        disposables.add(
                notificationRepository.getUnreadCount()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(this::updateBadge, error -> {
                            // Silently ignore — badge just won't show
                            binding.textBadge.setVisibility(View.GONE);
                        })
        );
    }

    private void updateBadge(long count) {
        if (count > 0) {
            binding.textBadge.setVisibility(View.VISIBLE);
            binding.textBadge.setText(count > 99 ? "99+" : String.valueOf(count));
        } else {
            binding.textBadge.setVisibility(View.GONE);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(HomeViewModel.class);
        setupRecyclerView();
        setupPullToRefresh();
        setupBottomNav();
        setupNotificationBell();
        setupAvatarAction();
        renderUserAvatar();
        observeState();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (binding != null) {
            renderUserAvatar();
        }
    }

    @Override
    public void onDestroyView() {
        disposables.clear();
        binding = null;
        super.onDestroyView();
    }
}
