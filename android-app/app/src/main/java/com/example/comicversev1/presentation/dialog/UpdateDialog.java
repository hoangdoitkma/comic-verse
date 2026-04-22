package com.example.comicversev1.presentation.dialog;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.comicversev1.R;
import com.example.comicversev1.data.model.AppUpdateInfo;

public class UpdateDialog extends DialogFragment {

    private AppUpdateInfo appUpdateInfo;
    private OnUpdateClickListener listener;

    public interface OnUpdateClickListener {
        void onUpdateClicked();
    }

    public static UpdateDialog newInstance(AppUpdateInfo info, OnUpdateClickListener listener) {
        UpdateDialog dialog = new UpdateDialog();
        dialog.appUpdateInfo = info;
        dialog.listener = listener;
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_app_update, container, false);
        
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        
        // Không cho đóng nếu là Force Update
        setCancelable(appUpdateInfo == null || !appUpdateInfo.isForceUpdate());

        TextView tvTitle = view.findViewById(R.id.tvUpdateTitle);
        TextView tvSize = view.findViewById(R.id.tvUpdateSize);
        Button btnUpdate = view.findViewById(R.id.btnUpdate);
        Button btnCancel = view.findViewById(R.id.btnCancel);

        if (appUpdateInfo != null) {
            tvTitle.setText("Phiên bản " + appUpdateInfo.getVersionName());
            if (appUpdateInfo.getApkSize() > 0) {
                tvSize.setText(String.format("Dung lượng: %.2f MB", appUpdateInfo.getApkSize() / (1024.0 * 1024.0)));
            } else {
                tvSize.setText("Dung lượng: Không rõ");
            }

            if (appUpdateInfo.isForceUpdate()) {
                btnCancel.setVisibility(View.GONE);
            }
        }

        btnUpdate.setOnClickListener(v -> {
            if (listener != null) {
                listener.onUpdateClicked();
            }
            if (appUpdateInfo != null && !appUpdateInfo.isForceUpdate()) {
                dismiss();
            } else {
                tvTitle.setText("Đang tải xuống...");
                btnUpdate.setEnabled(false);
                btnUpdate.setText("Vui lòng đợi...");
            }
        });

        btnCancel.setOnClickListener(v -> dismiss());

        return view;
    }
}
