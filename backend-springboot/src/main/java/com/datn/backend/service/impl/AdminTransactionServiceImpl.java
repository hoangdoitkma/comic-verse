package com.datn.backend.service.impl;

import com.datn.backend.dto.response.TransactionResponse;
import com.datn.backend.entity.Transaction;
import com.datn.backend.repository.TransactionRepository;
import com.datn.backend.service.AdminTransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminTransactionServiceImpl implements AdminTransactionService {

    private final TransactionRepository transactionRepository;

    @Override
    public Page<TransactionResponse> getTransactions(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Transaction> transactions = transactionRepository.findAll(pageable);
        return transactions.map(this::mapToTransactionResponse);
    }

    private TransactionResponse mapToTransactionResponse(Transaction transaction) {
        return TransactionResponse.builder()
                .id(transaction.getId())
                .orderCode(transaction.getOrderCode())
                .paymentLinkId(transaction.getPaymentLinkId())
                .amount(transaction.getAmount())
                .paymentMethod(transaction.getPaymentMethod() != null ? transaction.getPaymentMethod().name() : null)
                .status(transaction.getStatus() != null ? transaction.getStatus().name() : null)
                .createdAt(transaction.getCreatedAt())
                .userId(transaction.getUser() != null ? transaction.getUser().getId() : null)
                .userEmail(transaction.getUser() != null ? transaction.getUser().getEmail() : null)
                .userDisplayName(transaction.getUser() != null ? transaction.getUser().getDisplayName() : null)
                .packageId(transaction.getVipPackage() != null ? transaction.getVipPackage().getId() : null)
                .packageName(transaction.getVipPackage() != null ? transaction.getVipPackage().getName() : null)
                .build();
    }
}
