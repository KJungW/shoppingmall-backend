package com.project.shoppingmall.controller.purchase_retrieve.output;

import com.project.shoppingmall.dto.purchase.PurchaseDto;
import com.project.shoppingmall.entity.Purchase;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.domain.Slice;

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

  public OutputRetrievePurchasesByBuyer(Slice<Purchase> sliceResult) {
    this.currentSliceNumber = sliceResult.getNumber();
    this.sliceSize = sliceResult.getSize();
    this.isFirst = sliceResult.isFirst();
    this.isLast = sliceResult.isLast();
    this.hasNext = sliceResult.hasNext();
    this.hasPrevious = sliceResult.hasPrevious();
    this.purchaseList = sliceResult.getContent().stream().map(PurchaseDto::new).toList();
  }
}
