package com.project.shoppingmall.entity;

import com.project.shoppingmall.dto.purchase.ProductDataForPurchase;
import com.project.shoppingmall.exception.ServerLogicError;
import com.project.shoppingmall.type.RefundStateTypeForPurchaseItem;
import jakarta.persistence.*;
import java.time.LocalDateTime;
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

  private Long productId;
  private Long sellerId;

  @Column(columnDefinition = "JSON")
  private String productData;

  private Integer finalPrice;

  private Boolean isRefund;

  @OneToMany(mappedBy = "purchaseItem", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Refund> refunds = new ArrayList<>();

  private RefundStateTypeForPurchaseItem finalRefundState;
  private LocalDateTime finalRefundCreatedDate;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "REVIEW_ID")
  private Review review;

  @Builder
  public PurchaseItem(ProductDataForPurchase productData, Integer finalPrice) {
    registerProductData(productData);
    registerFinalPrice(finalPrice);
    this.isRefund = false;
    this.refunds = new ArrayList<>();
    this.finalRefundState = RefundStateTypeForPurchaseItem.NONE;
    this.finalRefundCreatedDate = null;
    this.review = null;
  }

  private void registerProductData(ProductDataForPurchase productData) {
    if (productData == null)
      throw new ServerLogicError("PurchaseItem의 비어있는 ProductDataForPurchase가 입력되었습니다.");
    this.productId = productData.getProductId();
    this.sellerId = productData.getSellerId();
    this.productData = productData.makeJson();
  }

  private void registerFinalPrice(Integer finalPrice) {
    if (finalPrice == null || finalPrice <= 0)
      throw new ServerLogicError("PurchaseItem에 적절하지 않은 finalPrice가 입력되었습니다.");
    this.finalPrice = finalPrice;
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
    this.finalRefundCreatedDate = LocalDateTime.now();
    this.finalRefundState = RefundStateTypeForPurchaseItem.REQUEST;
  }

  public void processFinalRefundAccept() {
    this.finalRefundState = RefundStateTypeForPurchaseItem.ACCEPT;
  }

  public void processFinalRefundReject() {
    this.finalRefundState = RefundStateTypeForPurchaseItem.REJECTED;
  }

  public void processFinalRefundComplete() {
    this.isRefund = true;
    this.finalRefundState = RefundStateTypeForPurchaseItem.COMPLETE;
  }

  public boolean writeReviewPossible() {
    return review == null;
  }

  public void registerReview(Review review) {
    if (review == null) throw new ServerLogicError("null인 Review를 PurchaseItem에 등록하려고 시도하고 있습니다.");
    this.review = review;
  }

  public void deleteReview() {
    this.review = null;
  }
}
