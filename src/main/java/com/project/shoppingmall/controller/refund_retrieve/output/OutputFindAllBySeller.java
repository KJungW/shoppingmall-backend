package com.project.shoppingmall.controller.refund_retrieve.output;

import com.project.shoppingmall.dto.SliceResult;
import com.project.shoppingmall.dto.refund.RefundPurchaseItemForSeller;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OutputFindAllBySeller {
  private int currentSliceNumber;
  private int sliceSize;
  private boolean isFirst;
  private boolean isLast;
  private boolean hasNext;
  private boolean hasPrevious;
  private List<RefundPurchaseItemForSeller> refundedPurchaseItemList;

  public OutputFindAllBySeller(SliceResult<RefundPurchaseItemForSeller> sliceResult) {
    this.currentSliceNumber = sliceResult.getCurrentSliceNumber();
    this.sliceSize = sliceResult.getSliceSize();
    this.isFirst = sliceResult.isFirst();
    this.isLast = sliceResult.isLast();
    this.hasNext = sliceResult.isHasNext();
    this.hasPrevious = sliceResult.isHasPrevious();
    this.refundedPurchaseItemList = sliceResult.getContentList();
  }
}
