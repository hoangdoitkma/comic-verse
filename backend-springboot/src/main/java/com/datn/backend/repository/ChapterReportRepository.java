package com.datn.backend.repository;

import com.datn.backend.entity.ChapterReport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChapterReportRepository extends JpaRepository<ChapterReport, Integer> {

    @Query(value = "SELECT cr FROM ChapterReport cr JOIN cr.chapter c JOIN c.comic co WHERE co.createdBy.id = :uploaderId",
           countQuery = "SELECT count(cr) FROM ChapterReport cr JOIN cr.chapter c JOIN c.comic co WHERE co.createdBy.id = :uploaderId")
    Page<ChapterReport> findByUploaderId(@Param("uploaderId") Integer uploaderId, Pageable pageable);

    @Query(value = "SELECT cr FROM ChapterReport cr JOIN cr.chapter c JOIN c.comic co WHERE co.createdBy.id = :uploaderId AND cr.status = :status",
           countQuery = "SELECT count(cr) FROM ChapterReport cr JOIN cr.chapter c JOIN c.comic co WHERE co.createdBy.id = :uploaderId AND cr.status = :status")
    Page<ChapterReport> findByUploaderIdAndStatus(@Param("uploaderId") Integer uploaderId, @Param("status") com.datn.backend.entity.enums.ReportStatus status, Pageable pageable);

    Page<ChapterReport> findByStatus(com.datn.backend.entity.enums.ReportStatus status, Pageable pageable);
}
