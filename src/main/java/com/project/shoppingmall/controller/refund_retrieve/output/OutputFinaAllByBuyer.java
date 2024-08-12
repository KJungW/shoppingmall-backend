package com.project.shoppingmall.controller.refund_retrieve.output;

import com.project.shoppingmall.dto.refund.RefundPurchaseItemForBuyer;
import com.project.shoppingmall.entity.PurchaseItem;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.domain.Slice;

@Getter
@AllArgsConstructor
public class OutputFinaAllByBuyer {
  private int currentSliceNumber;
  private int sliceSize;
  private boolean isFirst;
  private boolean isLast;
  private boolean hasNext;
  private boolean hasPrevious;
  private List<RefundPurchaseItemForBuyer> refundedPurchaseItemList;

  public OutputFinaAllByBuyer(Slice<PurchaseItem> sliceResult) {
    this.currentSliceNumber = sliceResult.getNumber();
    this.sliceSize = sliceResult.getSize();
    this.isFirst = sliceResult.isFirst();
    this.isLast = sliceResult.isLast();
    this.hasNext = sliceResult.hasNext();
    this.hasPrevious = sliceResult.hasPrevious();
    this.refundedPurchaseItemList =
        sliceResult.getContent().stream().map(RefundPurchaseItemForBuyer::new).toList();
  }
}
