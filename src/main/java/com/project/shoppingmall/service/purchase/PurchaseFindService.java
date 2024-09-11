package com.project.shoppingmall.service.purchase;

import com.project.shoppingmall.entity.Purchase;
import com.project.shoppingmall.repository.PurchaseRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PurchaseFindService {
  private final PurchaseRepository purchaseRepository;

  public Optional<Purchase> findByPurchaseUid(String purchaseUid) {
    return purchaseRepository.findByPurchaseUid(purchaseUid);
  }

  public List<Purchase> findAllByBuyer(long buyerId) {
    return purchaseRepository.findAllByBuyer(buyerId);
  }
}
