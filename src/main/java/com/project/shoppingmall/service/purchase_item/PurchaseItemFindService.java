package com.project.shoppingmall.service.purchase_item;

import com.project.shoppingmall.entity.PurchaseItem;
import com.project.shoppingmall.repository.PurchaseItemRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PurchaseItemFindService {
  private final PurchaseItemRepository purchaseItemRepository;

  public Optional<PurchaseItem> findByIdWithPurchaseAndRefund(Long purchaseItemId) {
    return purchaseItemRepository.findByIdWithPurchaseAndRefund(purchaseItemId);
  }

  public Optional<PurchaseItem> findById(long purchaseItemId) {
    return purchaseItemRepository.findById(purchaseItemId);
  }

  public Optional<PurchaseItem> findByReviewId(long reviewId) {
    return purchaseItemRepository.findByReviewId(reviewId);
  }

  public List<PurchaseItem> findLatestByProduct(long productId, int queryPurchaseItemCount) {
    PageRequest pageRequest =
        PageRequest.of(0, queryPurchaseItemCount, Sort.by(Sort.Direction.DESC, "createDate"));
    return purchaseItemRepository.findLatestByProduct(productId, pageRequest).getContent();
  }
}
