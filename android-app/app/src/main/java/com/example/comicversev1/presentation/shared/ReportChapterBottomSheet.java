package com.example.comicversev1.presentation.shared;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.comicversev1.R;
import com.example.comicversev1.data.model.ChapterReportRequest;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class ReportChapterBottomSheet extends BottomSheetDialogFragment {

    private EditText etReportReason;
    private Spinner spReportType;
    private View btnSubmitReport;

    private OnReportSubmitListener listener;
    private String selectedTypeCode;

    public interface OnReportSubmitListener {
        void onSubmit(ChapterReportRequest request);
    }

    public void setOnReportSubmitListener(OnReportSubmitListener listener) {
        this.listener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NORMAL, com.google.android.material.R.style.Theme_Design_BottomSheetDialog);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        dialog.setOnShowListener(d -> {
            BottomSheetDialog bottomSheetDialog = (BottomSheetDialog) d;
            View bottomSheet = bottomSheetDialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
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
        spReportType = view.findViewById(R.id.spReportType);
        btnSubmitReport = view.findViewById(R.id.btnSubmitReport);

        setupReportTypes();
        updateSubmitState();

        btnSubmitReport.setOnClickListener(v -> submitReport());
    }

    private void setupReportTypes() {
        ReportTypeOption[] options = new ReportTypeOption[]{
                new ReportTypeOption("Chọn loại lỗi", null),
                new ReportTypeOption("Lỗi tải ảnh", "IMAGE_NOT_LOADING"),
                new ReportTypeOption("Sai nội dung chương", "WRONG_CONTENT"),
                new ReportTypeOption("Lỗi chính tả", "TYPO_ERROR"),
                new ReportTypeOption("Trùng chương", "DUPLICATE_CHAPTER"),
                new ReportTypeOption("Khác", "OTHER")
        };

        ArrayAdapter<ReportTypeOption> adapter = new ArrayAdapter<>(
                requireContext(),
                R.layout.item_report_type_spinner,
                options
        );
        adapter.setDropDownViewResource(R.layout.item_report_type_spinner_dropdown);
        spReportType.setAdapter(adapter);
        spReportType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View itemView, int position, long id) {
                selectedTypeCode = options[position].code;
                updateSubmitState();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedTypeCode = null;
                updateSubmitState();
            }
        });
    }

    private void submitReport() {
        if (selectedTypeCode == null) {
            Toast.makeText(requireContext(), "Chọn loại lỗi trước khi gửi", Toast.LENGTH_SHORT).show();
            return;
        }

        String reason = etReportReason.getText().toString().trim();
        if (listener != null) {
            listener.onSubmit(new ChapterReportRequest(selectedTypeCode, reason));
        }
        dismiss();
    }

    private void updateSubmitState() {
        boolean canSubmit = selectedTypeCode != null;
        btnSubmitReport.setEnabled(canSubmit);
        btnSubmitReport.setAlpha(canSubmit ? 1f : 0.55f);
    }

    private static class ReportTypeOption {
        final String label;
        final String code;

        ReportTypeOption(String label, String code) {
            this.label = label;
            this.code = code;
        }

        @NonNull
        @Override
        public String toString() {
            return label;
        }
    }
}
