package com.project.shoppingmall.service.sales_statistics;

import com.project.shoppingmall.dto.purchase.SalesRevenueInMonth;
import com.project.shoppingmall.service.purchase_item.PurchaseItemService;
import com.project.shoppingmall.service.refund.RefundService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class SalesStatisticsService {
  private final PurchaseItemService purchaseItemService;
  private final RefundService refundService;

  public SalesRevenueInMonth getSalesRevenueInMonth(long sellerId, int year, int month) {
    Long revenuePrice =
        purchaseItemService.getSalesRevenuePriceInMonthBySeller(sellerId, year, month);
    Long refundPrice = refundService.getRefundPriceInMonthBySeller(sellerId, year, month);
    return new SalesRevenueInMonth(year, month, revenuePrice, refundPrice);
  }
}
