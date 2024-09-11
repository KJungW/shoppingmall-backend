package com.project.shoppingmall.service.basket_item;

import com.project.shoppingmall.entity.BasketItem;
import com.project.shoppingmall.repository.BasketItemRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class BasketItemFindService {
  private final BasketItemRepository basketItemRepository;

  public List<BasketItem> findAllById(List<Long> basketIdList) {
    return basketItemRepository.findAllById(basketIdList);
  }

  public List<BasketItem> findAllByProduct(long productId) {
    return basketItemRepository.findAllByProduct(productId);
  }

  public List<BasketItem> findAllByMember(long memberId) {
    return basketItemRepository.findAllByMember(memberId);
  }
}
