package com.project.shoppingmall.repository;

import com.project.shoppingmall.entity.BasketItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BasketItemRepository extends JpaRepository<BasketItem, Long> {}
