package com.project.shoppingmall.test_entity.report;

import static org.junit.jupiter.api.Assertions.*;

import com.project.shoppingmall.entity.Member;
import com.project.shoppingmall.entity.Product;
import com.project.shoppingmall.entity.report.ProductReport;
import com.project.shoppingmall.type.ReportResultType;

public class ProductReportChecker {
  public static void check(
      Member reporter,
      String title,
      String description,
      Boolean isProcessedComplete,
      ReportResultType reportResult,
      Product product,
      ProductReport target) {
    assertEquals(reporter.getId(), target.getReporter().getId());
    assertEquals(title, target.getTitle());
    assertEquals(description, target.getDescription());
    assertEquals(isProcessedComplete, target.getIsProcessedComplete());
    assertEquals(reportResult, target.getReportResult());
    assertEquals(product.getId(), target.getProduct().getId());
  }
}
