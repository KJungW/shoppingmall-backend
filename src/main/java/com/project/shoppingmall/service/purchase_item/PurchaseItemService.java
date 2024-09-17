package com.project.shoppingmall.service.purchase_item;

import com.project.shoppingmall.entity.PurchaseItem;
import com.project.shoppingmall.entity.Refund;
import com.project.shoppingmall.repository.PurchaseItemRepository;
import com.project.shoppingmall.type.RefundStateType;
import com.project.shoppingmall.util.DateUtil;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PurchaseItemService {
  private final PurchaseItemRepository purchaseItemRepository;

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

  public Long getSalesRevenuePriceInMonthBySeller(long sellerId, int year, int month) {
    LocalDateTime startDate = DateUtil.makeStartDateInMonth(year, month);
    LocalDateTime endDate = DateUtil.makeStartDateInNextMonth(year, month);
    return purchaseItemRepository.findSalesRevenuePriceInPeriodBySeller(
        sellerId, startDate, endDate);
  }
}
