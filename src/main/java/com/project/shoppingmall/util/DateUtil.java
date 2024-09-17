package com.project.shoppingmall.util;

import java.time.LocalDateTime;

public class DateUtil {
  public static LocalDateTime makeStartDateInMonth(int year, int month) {
    return LocalDateTime.of(year, month, 1, 0, 0);
  }

  public static LocalDateTime makeStartDateInNextMonth(int year, int month) {
    if (month + 1 > 12) return LocalDateTime.of(year + 1, 1, 1, 0, 0);
    else return LocalDateTime.of(year, month + 1, 1, 0, 0);
  }
}
