package com.project.shoppingmall.controller.product_retrieve.output;

import com.project.shoppingmall.dto.SliceResult;
import com.project.shoppingmall.dto.product.ProductHeaderDto;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OutputGetRandomProducts {
  private int currentSliceNumber;
  private int sliceSize;
  private boolean isFirst;
  private boolean isLast;
  private boolean hasNext;
  private boolean hasPrevious;
  private List<ProductHeaderDto> productList;

  public OutputGetRandomProducts(SliceResult<ProductHeaderDto> sliceResult) {
    this.currentSliceNumber = sliceResult.getCurrentSliceNumber();
    this.sliceSize = sliceResult.getSliceSize();
    this.isFirst = sliceResult.isFirst();
    this.isLast = sliceResult.isLast();
    this.hasNext = sliceResult.isHasNext();
    this.hasPrevious = sliceResult.isHasPrevious();
    this.productList = sliceResult.getContentList();
  }
}
