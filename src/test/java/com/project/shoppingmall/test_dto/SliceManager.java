package com.project.shoppingmall.test_dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;

public class SliceManager {

  public static <T> Slice<T> setMockSlice(int givenSliceNum, int givenSliceSize, List<T> contents) {
    Slice<T> mockSlice = mock(Slice.class);
    when(mockSlice.getNumber()).thenReturn(givenSliceNum);
    when(mockSlice.getSize()).thenReturn(givenSliceSize);
    when(mockSlice.isFirst()).thenReturn(false);
    when(mockSlice.isLast()).thenReturn(false);
    when(mockSlice.hasNext()).thenReturn(true);
    when(mockSlice.hasPrevious()).thenReturn(true);
    when(mockSlice.getContent()).thenReturn(contents);
    return mockSlice;
  }

  public static <T> void checkOnlyPageData(
      PageRequest pageRequest,
      boolean isFirst,
      boolean isLast,
      boolean hasNext,
      boolean hasPrevious,
      Slice<T> target) {
    assertEquals(pageRequest.getPageNumber(), target.getNumber());
    assertEquals(pageRequest.getPageSize(), target.getSize());
    assertEquals(isFirst, target.isFirst());
    assertEquals(isLast, target.isLast());
    assertEquals(hasNext, target.hasNext());
    assertEquals(hasPrevious, target.hasPrevious());
  }

  public static <T> void checkContentSize(int contentSize, Slice<T> target) {
    assertEquals(contentSize, target.getContent().size());
  }
}
