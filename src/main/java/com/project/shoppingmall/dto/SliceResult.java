package com.project.shoppingmall.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.domain.Slice;

@Getter
@AllArgsConstructor
public class SliceResult<T> {
  private int currentSliceNumber;
  private int sliceSize;
  private boolean isFirst;
  private boolean isLast;
  private boolean hasNext;
  private boolean hasPrevious;
  private List<T> contentList;

  public SliceResult(Slice slice, List<T> contentList) {
    this.currentSliceNumber = slice.getNumber();
    this.sliceSize = slice.getSize();
    this.isFirst = slice.isFirst();
    this.isLast = slice.isLast();
    this.hasNext = slice.hasNext();
    this.hasPrevious = slice.hasPrevious();
    this.contentList = contentList;
  }
}
