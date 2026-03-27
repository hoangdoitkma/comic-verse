package com.datn.backend.repository;

import com.datn.backend.entity.Transaction;
import com.datn.backend.entity.enums.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Integer> {

    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.status = :status AND t.createdAt >= :startDate AND t.createdAt < :endDate")
    BigDecimal sumAmountByStatusAndDateRange(@Param("status") TransactionStatus status, 
                                             @Param("startDate") LocalDateTime startDate, 
                                             @Param("endDate") LocalDateTime endDate);

    List<Transaction> findTop5ByStatusOrderByCreatedAtDesc(TransactionStatus status);

    @Query(value = "SELECT YEAR(t.created_at) as year, MONTH(t.created_at) as month, SUM(t.amount) as revenue " +
                   "FROM transactions t " +
                   "WHERE t.status = :status AND t.created_at >= :startDate " +
                   "GROUP BY YEAR(t.created_at), MONTH(t.created_at) " +
                   "ORDER BY year ASC, month ASC", nativeQuery = true)
    List<Object[]> getMonthlyRevenue(@Param("status") String status, @Param("startDate") LocalDateTime startDate);

    // KPI: Total all-time revenue
    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.status = :status")
    BigDecimal sumAmountByStatus(@Param("status") TransactionStatus status);

    // KPI: Count VIP transactions this month
    long countByStatusAndCreatedAtBetween(TransactionStatus status, LocalDateTime startDate, LocalDateTime endDate);

    // KPI: Count distinct users with active VIP (successful transactions)
    @Query("SELECT COUNT(DISTINCT t.user.id) FROM Transaction t WHERE t.status = :status")
    long countDistinctUserByStatus(@Param("status") TransactionStatus status);
}
