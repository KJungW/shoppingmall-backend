package com.project.shoppingmall.controller.alarm_retrive.output;

import com.project.shoppingmall.dto.SliceResult;
import com.project.shoppingmall.dto.alarm.AlarmDto;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OutputRetrieveAllByMember {
  private int currentSliceNumber;
  private int sliceSize;
  private boolean isFirst;
  private boolean isLast;
  private boolean hasNext;
  private boolean hasPrevious;
  private List<AlarmDto> alarmList;

  public OutputRetrieveAllByMember(SliceResult<AlarmDto> sliceResult) {
    this.currentSliceNumber = sliceResult.getCurrentSliceNumber();
    this.sliceSize = sliceResult.getSliceSize();
    this.isFirst = sliceResult.isFirst();
    this.isLast = sliceResult.isLast();
    this.hasNext = sliceResult.isHasNext();
    this.hasPrevious = sliceResult.isHasPrevious();
    this.alarmList = sliceResult.getContentList();
  }
}
