package com.project.shoppingmall.entity;

import com.project.shoppingmall.exception.ServerLogicError;
import com.project.shoppingmall.type.RefundStateType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Refund extends BaseEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "PURCHASE_ITEM_ID")
  private PurchaseItem purchaseItem;

  private Integer refundPrice;
  private String requestTitle;
  private String requestContent;
  private String responseContent;

  @Enumerated(EnumType.STRING)
  private RefundStateType state;

  @Builder
  public Refund(int refundPrice, String requestTitle, String requestContent) {
    if (requestTitle.isEmpty() || requestContent.isEmpty()) {
      throw new ServerLogicError("Refund를 Builder를 통해 생성할때, 필수값을 넣어주지 않았습니다.");
    }
    this.refundPrice = refundPrice;
    this.requestTitle = requestTitle;
    this.requestContent = requestContent;
    this.responseContent = "";
    this.state = RefundStateType.REQUEST;
  }

  public void registerPurchaseItem(PurchaseItem purchaseItem) {
    this.purchaseItem = purchaseItem;
  }

  public void acceptRefund(String requestContent) {
    state = RefundStateType.ACCEPT;
    this.responseContent = requestContent;
  }

  public void completeRefund() {
    this.state = RefundStateType.COMPLETE;
    this.purchaseItem.completeRefund();
  }
}
