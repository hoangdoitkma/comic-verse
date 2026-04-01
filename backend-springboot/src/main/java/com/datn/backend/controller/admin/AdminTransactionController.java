package com.datn.backend.controller.admin;

import com.datn.backend.dto.response.ApiResponse;
import com.datn.backend.dto.response.TransactionResponse;
import com.datn.backend.service.AdminTransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/transactions")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminTransactionController {

    private final AdminTransactionService adminTransactionService;

    @GetMapping
    public ApiResponse<Page<TransactionResponse>> getTransactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<TransactionResponse> transactions = adminTransactionService.getTransactions(page, size);
        return ApiResponse.success(transactions);
    }
}
