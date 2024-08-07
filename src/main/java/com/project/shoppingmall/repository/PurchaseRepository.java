package com.project.shoppingmall.repository;

import com.project.shoppingmall.entity.Purchase;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PurchaseRepository extends JpaRepository<Purchase, Long> {}
