package com.project.shoppingmall.controller.refund_retrieve.output;

import com.project.shoppingmall.dto.refund.RefundDto;
import com.project.shoppingmall.entity.Refund;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.domain.Slice;

@Getter
@AllArgsConstructor
public class OutputFindAllAboutPurchaseItem {
  private int currentSliceNumber;
  private int sliceSize;
  private boolean isFirst;
  private boolean isLast;
  private boolean hasNext;
  private boolean hasPrevious;
  private List<RefundDto> refundList;

  public OutputFindAllAboutPurchaseItem(Slice<Refund> sliceResult) {
    this.currentSliceNumber = sliceResult.getNumber();
    this.sliceSize = sliceResult.getSize();
    this.isFirst = sliceResult.isFirst();
    this.isLast = sliceResult.isLast();
    this.hasNext = sliceResult.hasNext();
    this.hasPrevious = sliceResult.hasPrevious();
    this.refundList = sliceResult.getContent().stream().map(RefundDto::new).toList();
  }
}
