package com.project.shoppingmall.controller.purchase_retrieve.output;

import com.project.shoppingmall.dto.SliceResult;
import com.project.shoppingmall.dto.purchase.PurchaseDto;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OutputRetrievePurchasesByBuyer {
  private int currentSliceNumber;
  private int sliceSize;
  private boolean isFirst;
  private boolean isLast;
  private boolean hasNext;
  private boolean hasPrevious;
  private List<PurchaseDto> purchaseList;

  public OutputRetrievePurchasesByBuyer(SliceResult<PurchaseDto> sliceResult) {
    this.currentSliceNumber = sliceResult.getCurrentSliceNumber();
    this.sliceSize = sliceResult.getSliceSize();
    this.isFirst = sliceResult.isFirst();
    this.isLast = sliceResult.isLast();
    this.hasNext = sliceResult.isHasNext();
    this.hasPrevious = sliceResult.isHasPrevious();
    this.purchaseList = sliceResult.getContentList();
  }
}
