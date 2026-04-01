package com.datn.backend.service;

import com.datn.backend.dto.response.TransactionResponse;
import org.springframework.data.domain.Page;

public interface AdminTransactionService {
    Page<TransactionResponse> getTransactions(int page, int size);
}
