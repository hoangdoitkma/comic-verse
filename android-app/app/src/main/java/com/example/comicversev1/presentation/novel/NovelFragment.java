package com.example.comicversev1.presentation.novel;

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
import com.example.comicversev1.databinding.FragmentNovelBinding;
import com.example.comicversev1.utils.Constants;
import com.bumptech.glide.Glide;
import com.example.comicversev1.presentation.home.HomeHeroAdapter;
import com.example.comicversev1.presentation.home.HomeQuickActionAdapter;
import com.example.comicversev1.presentation.home.HotAdapter;
import com.example.comicversev1.presentation.home.LargeCardAdapter;
import com.example.comicversev1.presentation.home.RecentAdapter;
import com.example.comicversev1.presentation.home.ShelfAdapter;
import com.example.comicversev1.presentation.shared.adapter.AdSearchSectionAdapter;
import com.example.comicversev1.presentation.shared.adapter.ContinueReadingSectionAdapter;
import com.example.comicversev1.presentation.shared.adapter.HeroSectionAdapter;
import com.example.comicversev1.presentation.shared.adapter.QuickActionSectionAdapter;
import com.example.comicversev1.presentation.shared.adapter.ShelfSectionAdapter;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

@AndroidEntryPoint
public class NovelFragment extends Fragment {

    @Inject
    NotificationRepository notificationRepository;

    private FragmentNovelBinding binding;
    private NovelViewModel viewModel;
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

    // Section wrappers
    private ContinueReadingSectionAdapter continueSection;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentNovelBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    private void setupRecyclerView() {
        // Initialize child adapters
        heroAdapter = new HomeHeroAdapter();
        quickActionAdapter = new HomeQuickActionAdapter();
        quickActionAdapter.setOnItemClickListener(action -> {
            if ("vip".equals(action.getId())) {
                NavHostFragment.findNavController(this).navigate(R.id.action_global_vipCenter);
            }
        });
        
        recentAdapter = new RecentAdapter();
        recentAdapter.setListener(item -> {
            NavHostFragment.findNavController(this).navigate(
                com.example.comicversev1.NavGraphDirections.actionGlobalNovelDetail(0, item.getSlug())
            );
        });

        recommendAdapter = new ShelfAdapter(item -> {
            NavHostFragment.findNavController(this).navigate(
                com.example.comicversev1.NavGraphDirections.actionGlobalNovelDetail(0, item.getSlug())
            );
        });
        
        newUpdateAdapter = new ShelfAdapter(item -> {
            NavHostFragment.findNavController(this).navigate(
                com.example.comicversev1.NavGraphDirections.actionGlobalNovelDetail(0, item.getSlug())
            );
        });
        
        hotAdapter = new HotAdapter();
        hotAdapter.setListener(item -> {
            NavHostFragment.findNavController(this).navigate(
                com.example.comicversev1.NavGraphDirections.actionGlobalNovelDetail(0, item.getSlug())
            );
        });

        completedAdapter = new LargeCardAdapter(true);
        completedAdapter.setListener(item -> {
            NavHostFragment.findNavController(this).navigate(
                com.example.comicversev1.NavGraphDirections.actionGlobalNovelDetail(0, item.getSlug())
            );
        });

        newComicsAdapter = new LargeCardAdapter(false);
        newComicsAdapter.setListener(item -> {
            NavHostFragment.findNavController(this).navigate(
                com.example.comicversev1.NavGraphDirections.actionGlobalNovelDetail(0, item.getSlug())
            );
        });

        // Initialize wrapper section adapters
        AdSearchSectionAdapter adSearchSection = new AdSearchSectionAdapter(query -> navigateToSearch(query, "NOVEL"));
        HeroSectionAdapter heroSection = new HeroSectionAdapter(heroAdapter);
        QuickActionSectionAdapter quickActionSection = new QuickActionSectionAdapter(quickActionAdapter);
        continueSection = new ContinueReadingSectionAdapter();

        ShelfSectionAdapter recentSection = new ShelfSectionAdapter("Tiểu thuyết vừa đọc", recentAdapter, new LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false));
        ShelfSectionAdapter recommendSection = new ShelfSectionAdapter("Gợi ý tiểu thuyết", recommendAdapter, new LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false));
        ShelfSectionAdapter newUpdateSection = new ShelfSectionAdapter("Mới cập nhật", newUpdateAdapter, new LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false));
        ShelfSectionAdapter hotSection = new ShelfSectionAdapter("Tiểu thuyết HOT nhất", hotAdapter, new GridLayoutManager(requireContext(), 3));
        ShelfSectionAdapter completedSection = new ShelfSectionAdapter("Đã hoàn thành", completedAdapter, new LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false));
        ShelfSectionAdapter newComicsSection = new ShelfSectionAdapter("Tiểu thuyết mới", newComicsAdapter, new LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false));

        // Create ConcatAdapter
        ConcatAdapter concatAdapter = new ConcatAdapter(
                adSearchSection,
                heroSection,
                quickActionSection,
                continueSection,
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
            
            continueSection.setContinueData(state.getContinueSubtitle(), state.getContinueProgress());
            
            recommendAdapter.submitList(state.getRecommendations());
            newUpdateAdapter.submitList(state.getNewUpdates());
            hotAdapter.submitList(state.getHotComics());
            completedAdapter.submitList(state.getCompleted());
            newComicsAdapter.submitList(state.getNewComics());
            
            // To force update the ConcatAdapter visibility checks
            binding.recyclerMain.getAdapter().notifyDataSetChanged();
        });

        // Observe local reading history (from Room DB offline directly)
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
        binding.bottomNavigation.setSelectedItemId(R.id.menu_novel);
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.menu_home) {
                NavHostFragment.findNavController(this).navigate(R.id.homeFragment);
                return true;
            }
            if (item.getItemId() == R.id.menu_novel) {
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

    private void setupNotificationBell() {
        binding.btnBell.setOnClickListener(v -> {
            NavHostFragment.findNavController(this).navigate(R.id.action_global_notification);
        });
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

        if (token.isEmpty()) {
            binding.textBadge.setVisibility(View.GONE);
            return;
        }

        disposables.add(
                notificationRepository.getUnreadCount()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(count -> {
                            if (count > 0) {
                                binding.textBadge.setVisibility(View.VISIBLE);
                                binding.textBadge.setText(count > 99 ? "99+" : String.valueOf(count));
                            } else {
                                binding.textBadge.setVisibility(View.GONE);
                            }
                        }, error -> binding.textBadge.setVisibility(View.GONE))
        );
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(NovelViewModel.class);
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
