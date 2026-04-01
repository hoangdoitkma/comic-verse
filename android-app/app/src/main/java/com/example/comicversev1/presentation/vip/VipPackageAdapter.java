package com.example.comicversev1.presentation.vip;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.comicversev1.R;

import java.util.List;

public class VipPackageAdapter extends RecyclerView.Adapter<VipPackageAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(com.example.comicversev1.data.model.VipPackageDTO vipPackage);
    }

    private List<com.example.comicversev1.data.model.VipPackageDTO> items;
    private final OnItemClickListener listener;

    public VipPackageAdapter(List<com.example.comicversev1.data.model.VipPackageDTO> items, OnItemClickListener listener) {
        this.items = items;
        this.listener = listener;
    }
    
    public void setItems(List<com.example.comicversev1.data.model.VipPackageDTO> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_vip_package, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        com.example.comicversev1.data.model.VipPackageDTO item = items.get(position);
        holder.textTitle.setText(item.name);
        holder.textPrice.setText(item.getFormattedPrice());
        if (item.durationMonth == 999) {
            holder.textPeriod.setText("");
        } else {
            holder.textPeriod.setText(item.getFormattedPeriod());
        }
        
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textTitle;
        TextView textPrice;
        TextView textPeriod;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textTitle = itemView.findViewById(R.id.text_title);
            textPrice = itemView.findViewById(R.id.text_price);
            textPeriod = itemView.findViewById(R.id.text_period);
        }
    }
}
