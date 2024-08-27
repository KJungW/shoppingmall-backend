package com.project.shoppingmall.dto.alarm;

import com.project.shoppingmall.entity.Alarm;
import com.project.shoppingmall.type.AlarmType;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AlarmDto {
  private Long alarmId;
  private Long listenerId;
  private AlarmType alarmType;
  private String content;
  private Long targetRefundId;
  private Long targetReviewId;
  private Long targetProductId;
  private Boolean isChecked;

  public AlarmDto(Alarm alarm) {
    this.alarmId = alarm.getId();
    this.listenerId = alarm.getListener().getId();
    ;
    this.alarmType = alarm.getAlarmType();
    this.content = alarm.getContent();
    if (alarm.getTargetRefund() != null) this.targetRefundId = alarm.getTargetRefund().getId();
    if (alarm.getTargetReview() != null) this.targetReviewId = alarm.getTargetReview().getId();
    if (alarm.getTargetProduct() != null) this.targetProductId = alarm.getTargetProduct().getId();
    this.isChecked = alarm.getIsChecked();
  }
}
