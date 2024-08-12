package com.project.shoppingmall.dto.refund;

import com.project.shoppingmall.entity.Refund;
import com.project.shoppingmall.type.RefundStateType;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RefundDto {
  private long refundId;
  private int refundPrice;
  private String requestTitle;
  private String requestContent;
  private String responseContent;
  private RefundStateType refundState;
  private long purchaseItemId;

  public RefundDto(Refund refund) {
    this.refundId = refund.getId();
    this.refundPrice = refund.getRefundPrice();
    this.requestTitle = refund.getRequestTitle();
    this.requestContent = refund.getRequestContent();
    this.responseContent = refund.getResponseContent();
    this.refundState = refund.getState();
    this.purchaseItemId = refund.getPurchaseItem().getId();
  }
}
