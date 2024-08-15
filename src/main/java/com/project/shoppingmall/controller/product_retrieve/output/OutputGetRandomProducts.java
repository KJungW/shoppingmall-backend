package com.project.shoppingmall.controller.product_retrieve.output;

import com.project.shoppingmall.dto.product.ProductHeaderDto;
import com.project.shoppingmall.entity.Product;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.domain.Slice;

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

  public OutputGetRandomProducts(Slice<Product> sliceResult) {
    this.currentSliceNumber = sliceResult.getNumber();
    this.sliceSize = sliceResult.getSize();
    this.isFirst = sliceResult.isFirst();
    this.isLast = sliceResult.isLast();
    this.hasNext = sliceResult.hasNext();
    this.hasPrevious = sliceResult.hasPrevious();
    this.productList = sliceResult.getContent().stream().map(ProductHeaderDto::new).toList();
  }
}
