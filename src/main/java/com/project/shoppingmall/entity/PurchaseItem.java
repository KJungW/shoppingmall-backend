package com.project.shoppingmall.entity;

import com.project.shoppingmall.exception.ServerLogicError;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PurchaseItem extends BaseEntity {
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
  private boolean isRefund;

  @OneToMany(mappedBy = "purchaseItem", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Refund> refunds = new ArrayList<>();

  @Builder
  public PurchaseItem(Product product, String productData, Integer finalPrice) {
    if (product == null || productData.isEmpty() || finalPrice <= 0) {
      throw new ServerLogicError("PurchaseItem를 빌더로 생성할때 필수값을 넣어주지 않았습니다.");
    }
    this.product = product;
    this.productData = productData;
    this.finalPrice = finalPrice;
    this.isRefund = false;
  }

  public void registerPurchase(Purchase purchase) {
    this.purchase = purchase;
  }

  public void addRefund(Refund refund) {
    if (refund == null) {
      throw new ServerLogicError("구매아이템에 추가할 환불데이터가 null입니다.");
    }
    this.refunds.add(refund);
    refund.registerPurchaseItem(this);
  }
}
