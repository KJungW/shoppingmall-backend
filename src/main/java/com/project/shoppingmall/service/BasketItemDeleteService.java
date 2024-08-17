package com.project.shoppingmall.service;

import com.project.shoppingmall.entity.BasketItem;
import com.project.shoppingmall.exception.ServerLogicError;
import com.project.shoppingmall.repository.BasketItemRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class BasketItemDeleteService {
  private final BasketItemRepository basketItemRepository;

  public void deleteBasketItem(BasketItem basketItem) {
    if (basketItem == null) throw new ServerLogicError("비어있는 BasketItem을 제거하려고 시도하고 있습니다.");
    basketItemRepository.delete(basketItem);
  }

  public void deleteBasketItemList(List<BasketItem> basketItems) {
    basketItemRepository.deleteAllInBatch(basketItems);
  }
}
