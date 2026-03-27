package com.example.comicversev1.presentation.home;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.comicversev1.databinding.ItemHomeHeroBinding;
import com.example.comicversev1.domain.entity.HomeContent;

import java.util.ArrayList;
import java.util.List;

public class HomeHeroAdapter extends RecyclerView.Adapter<HomeHeroAdapter.HeroViewHolder> {

    private final List<HomeContent.Hero> items = new ArrayList<>();

    public void submitList(List<HomeContent.Hero> heroes) {
        items.clear();
        if (heroes != null) {
            items.addAll(heroes);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public HeroViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemHomeHeroBinding binding = ItemHomeHeroBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new HeroViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull HeroViewHolder holder, int position) {
        HomeContent.Hero hero = items.get(position);
        holder.binding.textHeroTitle.setText(hero.getTitle());
        holder.binding.textHeroSubtitle.setText(hero.getDescription());
        holder.binding.buttonHeroCta.setText(hero.getCta());
        Glide.with(holder.binding.getRoot().getContext())
                .load(hero.getImageUrl())
                .centerCrop()
                .into(holder.binding.imageHeroPoster);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class HeroViewHolder extends RecyclerView.ViewHolder {
        final ItemHomeHeroBinding binding;

        HeroViewHolder(ItemHomeHeroBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
