package com.project.shoppingmall.test_dto;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.project.shoppingmall.dto.SliceResult;
import org.springframework.data.domain.Slice;

public class SliceResultManager {
  public static <R, T> void checkOnlySliceData(Slice<R> givenSlice, SliceResult<T> target) {
    assertEquals(givenSlice.getNumber(), target.getCurrentSliceNumber());
    assertEquals(givenSlice.getSize(), target.getSliceSize());
    assertEquals(givenSlice.isFirst(), target.isFirst());
    assertEquals(givenSlice.isLast(), target.isLast());
    assertEquals(givenSlice.hasNext(), target.isHasNext());
    assertEquals(givenSlice.hasPrevious(), target.isHasPrevious());
  }
}
