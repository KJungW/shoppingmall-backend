package com.project.shoppingmall.dto.purchase;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SalesRevenueInMonth {
  private Integer year;
  private Integer month;
  private Long revenuePrice;
  private Long refundPrice;
}
