package com.example.comicversev1.presentation.shared;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.comicversev1.R;
import com.example.comicversev1.data.model.ChapterReportRequest;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.LinkedHashMap;
import java.util.Map;

public class ReportChapterBottomSheet extends BottomSheetDialogFragment {

    private EditText etReportReason;
    private View btnSubmitReport;

    private OnReportSubmitListener listener;

    // Currently selected type code
    private String selectedTypeCode = null;
    // Map from view ID → type code
    private final Map<Integer, String> typeMap = new LinkedHashMap<>();
    // All clickable type views
    private LinearLayout[] typeViews;

    public interface OnReportSubmitListener {
        void onSubmit(ChapterReportRequest request);
    }

    public void setOnReportSubmitListener(OnReportSubmitListener listener) {
        this.listener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Make background transparent so our custom bg_report_sheet is visible
        setStyle(STYLE_NORMAL, com.google.android.material.R.style.Theme_Design_BottomSheetDialog);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        dialog.setOnShowListener(d -> {
            BottomSheetDialog bsd = (BottomSheetDialog) d;
            View bottomSheet = bsd.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                bottomSheet.setBackgroundResource(android.R.color.transparent);
            }
        });
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_report_chapter_bottom_sheet, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etReportReason = view.findViewById(R.id.etReportReason);
        btnSubmitReport = view.findViewById(R.id.btnSubmitReport);

        // Build type mapping
        typeMap.put(R.id.reportTypeImage, "IMAGE_NOT_LOADING");
        typeMap.put(R.id.reportTypeWrongContent, "WRONG_CONTENT");
        typeMap.put(R.id.reportTypeTypo, "TYPO_ERROR");
        typeMap.put(R.id.reportTypeDuplicate, "DUPLICATE_CHAPTER");
        typeMap.put(R.id.reportTypeOther, "OTHER");

        // Find all type views
        typeViews = new LinearLayout[]{
                view.findViewById(R.id.reportTypeImage),
                view.findViewById(R.id.reportTypeWrongContent),
                view.findViewById(R.id.reportTypeTypo),
                view.findViewById(R.id.reportTypeDuplicate),
                view.findViewById(R.id.reportTypeOther),
        };

        // Set click listeners on each type card
        for (LinearLayout typeView : typeViews) {
            typeView.setOnClickListener(v -> selectType(v.getId()));
        }

        // Submit button
        btnSubmitReport.setOnClickListener(v -> {
            if (selectedTypeCode == null) {
                Toast.makeText(requireContext(), "Vui lòng chọn loại lỗi", Toast.LENGTH_SHORT).show();
                return;
            }

            String reason = etReportReason.getText().toString().trim();

            if (listener != null) {
                listener.onSubmit(new ChapterReportRequest(selectedTypeCode, reason));
            }
            dismiss();
        });
    }

    /**
     * Highlights the selected type card and deselects others.
     */
    private void selectType(int viewId) {
        selectedTypeCode = typeMap.get(viewId);

        for (LinearLayout typeView : typeViews) {
            boolean isSelected = typeView.getId() == viewId;
            typeView.setSelected(isSelected);

            // Animate a subtle scale to give tactile feedback
            typeView.animate()
                    .scaleX(isSelected ? 1.03f : 1.0f)
                    .scaleY(isSelected ? 1.03f : 1.0f)
                    .setDuration(150)
                    .start();
        }
    }
}
