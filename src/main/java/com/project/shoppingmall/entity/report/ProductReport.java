package com.project.shoppingmall.entity.report;

import com.project.shoppingmall.entity.Member;
import com.project.shoppingmall.entity.Product;
import com.project.shoppingmall.exception.ServerLogicError;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@DiscriminatorValue("PRODUCT_REPORT")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductReport extends Report {
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "PRODUCT_ID")
  private Product product;

  @Builder
  public ProductReport(Member reporter, String title, String description, Product product) {
    super(reporter, title, description);
    updateProduct(product);
  }

  private void updateProduct(Product product) {
    if (product == null) throw new ServerLogicError("ProductReport의 product에 빈값이 들어왔습니다.");
    this.product = product;
  }
}
