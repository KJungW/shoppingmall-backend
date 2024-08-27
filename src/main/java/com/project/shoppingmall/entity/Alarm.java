package com.project.shoppingmall.entity;

import com.project.shoppingmall.exception.ServerLogicError;
import com.project.shoppingmall.final_value.AlarmContentTemplate;
import com.project.shoppingmall.type.AlarmType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Alarm extends BaseEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  private Member listener;

  @Enumerated(EnumType.STRING)
  private AlarmType alarmType;

  private String content;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "TARGET_REFUND_ID")
  private Refund targetRefund;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "TARGET_REVIEW_ID")
  private Review targetReview;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "TARGET_PRODUCT_ID")
  private Product targetProduct;

  private Boolean isChecked;

  @Builder
  public Alarm(
      Member listener,
      AlarmType alarmType,
      Refund targetRefund,
      Review targetReview,
      Product targetProduct) {
    if (listener == null) throw new ServerLogicError("Alarm의 listener필드에 비어있는 값이 입력되었습니다.");
    if (alarmType == null) throw new ServerLogicError("Alarm의 alarmType필드에 비어있는 값이 입력되었습니다.");
    this.listener = listener;
    this.alarmType = alarmType;
    this.targetRefund = targetRefund;
    this.targetReview = targetReview;
    this.targetProduct = targetProduct;
    this.isChecked = false;

    if (validateDataForAlarmType()) {
      makeContent();
    } else {
      throw new ServerLogicError("현재 Alarm의 데이터가 올바르지 않습니다. 알람타입에 맞게 데이터를 세팅해주세요");
    }
  }

  public boolean validateDataForAlarmType() {
    switch (alarmType) {
      case MEMBER_BAN -> {
        if (targetRefund == null && targetReview == null && targetProduct == null) return true;
      }
      case REVIEW_BAN -> {
        if (targetRefund == null && targetReview != null && targetProduct == null) return true;
      }
      case PRODUCT_BAN, TYPE_DELETE, TYPE_UPDATE -> {
        if (targetRefund == null && targetReview == null && targetProduct != null) return true;
      }
      case REFUND_REQUEST -> {
        if (targetRefund != null && targetReview == null && targetProduct == null) return true;
      }
      default -> throw new ServerLogicError("아직 처리과정이 세팅되지 않은 AlarmType이 존재합니다.");
    }
    return false;
  }

  private void makeContent() {
    switch (alarmType) {
      case MEMBER_BAN -> content =
          AlarmContentTemplate.makeMemberBanAlarmContent(listener.getIsBan());
      case REVIEW_BAN -> content =
          AlarmContentTemplate.makeReviewBanAlarmContent(
              targetReview.getIsBan(), targetReview.getTitle());
      case PRODUCT_BAN -> content =
          AlarmContentTemplate.makeProductBanAlarmContent(
              targetProduct.getIsBan(), targetProduct.getName());
      case TYPE_DELETE -> content =
          AlarmContentTemplate.makeTypeDeleteAlarmContent(targetProduct.getName());
      case TYPE_UPDATE -> content =
          AlarmContentTemplate.makeTypeUpdateAlarmContent(targetProduct.getName());
      case REFUND_REQUEST -> content = AlarmContentTemplate.makeRefundRequestAlarmContent();
      default -> throw new ServerLogicError("아직 처리과정이 세팅되지 않은 AlarmType이 존재합니다.");
    }
  }

  public void processAlarmChecking() {
    isChecked = true;
  }
}
