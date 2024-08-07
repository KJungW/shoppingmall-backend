package com.project.shoppingmall.entity;

import com.project.shoppingmall.entity.value.DeliveryInfo;
import com.project.shoppingmall.exception.ServerLogicError;
import com.project.shoppingmall.type.PurchaseStateType;
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
public class Purchase extends BaseEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "BUYER_ID")
  private Member buyer;

  @OneToMany(mappedBy = "purchase", cascade = CascadeType.ALL, orphanRemoval = true)
  List<PurchaseItem> purchaseItems = new ArrayList<>();

  @Column(unique = true)
  private String purchaseUid;

  private String paymentUid;

  @Enumerated(value = EnumType.STRING)
  private PurchaseStateType state;

  private String purchaseTitle;
  @Embedded private DeliveryInfo deliveryInfo;
  private int totalPrice;

  @Builder
  public Purchase(
      Member buyer,
      List<PurchaseItem> purchaseItems,
      String purchaseUid,
      String purchaseTitle,
      DeliveryInfo deliveryInfo,
      int totalPrice) {
    if (buyer == null
        || purchaseItems.isEmpty()
        || purchaseUid.isEmpty()
        || purchaseTitle.isEmpty()
        || deliveryInfo == null
        || totalPrice <= 0) {
      throw new ServerLogicError("Purchase를 빌더로 생성할때 필수값을 넣어주지 않았습니다.");
    }
    this.buyer = buyer;
    updatePurchaseItems(purchaseItems);
    this.purchaseUid = purchaseUid;
    this.state = PurchaseStateType.READY;
    this.purchaseTitle = purchaseTitle;
    this.deliveryInfo = deliveryInfo;
    this.totalPrice = totalPrice;
  }

  private void updatePurchaseItems(List<PurchaseItem> purchaseItems) {
    if (purchaseItems == null || purchaseItems.isEmpty()) return;
    this.purchaseItems.clear();
    for (PurchaseItem item : purchaseItems) {
      item.registerPurchase(this);
      this.purchaseItems.add(item);
    }
  }

  public void registerPaymentUid(String paymentUid) {
    this.paymentUid = paymentUid;
  }

  public void convertStateToComplete(String paymentUid) {
    this.paymentUid = paymentUid;
    this.state = PurchaseStateType.COMPLETE;
  }

  public void convertStateToFail(String paymentUid) {
    this.paymentUid = paymentUid;
    this.state = PurchaseStateType.FAIL;
  }

  public void convertStateToDetectPriceTampering(String paymentUid) {
    this.paymentUid = paymentUid;
    this.state = PurchaseStateType.DETECT_PRICE_TAMPERING;
  }
}
