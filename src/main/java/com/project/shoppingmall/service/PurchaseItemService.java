package com.project.shoppingmall.service;

import com.project.shoppingmall.entity.PurchaseItem;
import com.project.shoppingmall.entity.Refund;
import com.project.shoppingmall.repository.PurchaseItemRepository;
import com.project.shoppingmall.type.RefundStateType;
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
public class PurchaseItemService {
  private final PurchaseItemRepository purchaseItemRepository;

  public Optional<PurchaseItem> findByIdWithPurchaseAndRefund(Long purchaseItemId) {
    return purchaseItemRepository.findByIdWithPurchaseAndRefund(purchaseItemId);
  }

  public List<Refund> selectRefundByState(PurchaseItem purchaseItem, RefundStateType state) {
    return purchaseItem.getRefunds().stream()
        .filter(refund -> refund.getState().equals(state))
        .toList();
  }

  public boolean refundIsPossible(PurchaseItem purchaseItem) {
    return purchaseItem.getRefunds().stream()
        .filter(
            refund ->
                refund.getState().equals(RefundStateType.COMPLETE)
                    || refund.getState().equals(RefundStateType.ACCEPT)
                    || refund.getState().equals(RefundStateType.REQUEST))
        .toList()
        .isEmpty();
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
