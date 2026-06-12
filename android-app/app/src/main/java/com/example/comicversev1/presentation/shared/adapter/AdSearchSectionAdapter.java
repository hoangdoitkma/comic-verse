package com.example.comicversev1.presentation.shared.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.comicversev1.R;
import com.google.android.material.textfield.TextInputEditText;

public class AdSearchSectionAdapter extends RecyclerView.Adapter<AdSearchSectionAdapter.ViewHolder> {

    public interface OnSearchSubmitListener {
        void onSearchSubmit(String query);
    }

    private final OnSearchSubmitListener listener;

    public AdSearchSectionAdapter() {
        this(null);
    }

    public AdSearchSectionAdapter(OnSearchSubmitListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_landing_ad_search, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(listener);
    }

    @Override
    public int getItemCount() {
        return 1; // Always 1 section
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextInputEditText inputSearch;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            inputSearch = itemView.findViewById(R.id.inputSearch);
        }

        void bind(OnSearchSubmitListener listener) {
            if (inputSearch == null || listener == null) return;
            inputSearch.setFocusable(false);
            inputSearch.setCursorVisible(false);
            inputSearch.setKeyListener(null);
            inputSearch.setOnClickListener(v -> listener.onSearchSubmit(""));
            itemView.setOnClickListener(v -> listener.onSearchSubmit(""));
            inputSearch.setOnEditorActionListener((v, actionId, event) -> {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    listener.onSearchSubmit(inputSearch.getText() != null ? inputSearch.getText().toString() : "");
                    return true;
                }
                return false;
            });
        }
    }
}
