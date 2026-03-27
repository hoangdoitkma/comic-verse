package com.example.comicversev1.presentation.discover;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.comicversev1.databinding.FragmentDiscoverBinding;
import com.example.comicversev1.domain.entity.ComicEntity;

import dagger.hilt.android.AndroidEntryPoint;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;

@AndroidEntryPoint
public class DiscoverFragment extends Fragment implements DiscoverPagingAdapter.OnComicClickListener {

    private FragmentDiscoverBinding binding;
    private DiscoverViewModel viewModel;
    private DiscoverPagingAdapter adapter;
    private final CompositeDisposable disposables = new CompositeDisposable();

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
        observePaging();
    }

    private void setupList() {
        adapter = new DiscoverPagingAdapter(this);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerView.setAdapter(adapter);
    }

    private void observePaging() {
        viewModel.pagingData().observe(getViewLifecycleOwner(), pagingData -> {
            adapter.submitData(getViewLifecycleOwner().getLifecycle(), pagingData);
        });
    }

    @Override
    public void onComicClick(ComicEntity comic) {
        DiscoverFragmentDirections.ActionDiscoverToDetail action =
                DiscoverFragmentDirections.actionDiscoverToDetail(comic.getSlug(), comic.getId());
        NavHostFragment.findNavController(this).navigate(action);
    }
}
