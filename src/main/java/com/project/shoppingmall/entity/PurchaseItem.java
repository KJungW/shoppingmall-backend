package com.project.shoppingmall.entity;

import com.project.shoppingmall.exception.ServerLogicError;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PurchaseItem {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "PURCHASE_ID")
  private Purchase purchase;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "PRODUCT_ID")
  private Product product;

  @Column(columnDefinition = "JSON")
  private String productData;

  private Integer finalPrice;

  @Builder
  public PurchaseItem(Product product, String productData, Integer finalPrice) {
    if (product == null || productData.isEmpty() || finalPrice <= 0) {
      throw new ServerLogicError("PurchaseItem를 빌더로 생성할때 필수값을 넣어주지 않았습니다.");
    }
    this.product = product;
    this.productData = productData;
    this.finalPrice = finalPrice;
  }

  public void registerPurchase(Purchase purchase) {
    this.purchase = purchase;
  }
}
