package com.datn.backend.repository;

import com.datn.backend.entity.VipPackage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VipPackageRepository extends JpaRepository<VipPackage, Integer> {
    boolean existsByName(String name);
}
