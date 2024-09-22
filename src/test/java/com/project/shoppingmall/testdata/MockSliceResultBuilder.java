package com.project.shoppingmall.testdata;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import org.springframework.data.domain.Slice;

public class MockSliceResultBuilder {

  public static <T> Slice<T> setSlice(int givenSliceNum, int givenSliceSize, List<T> contents) {
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
}
