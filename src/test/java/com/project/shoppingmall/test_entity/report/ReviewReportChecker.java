package com.project.shoppingmall.test_entity.report;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.project.shoppingmall.entity.Member;
import com.project.shoppingmall.entity.Review;
import com.project.shoppingmall.entity.report.ReviewReport;
import com.project.shoppingmall.type.ReportResultType;

public class ReviewReportChecker {
  public static void check(
      Member reporter,
      String title,
      String description,
      Boolean isProcessedComplete,
      ReportResultType reportResult,
      Review review,
      ReviewReport target) {
    assertEquals(reporter.getId(), target.getReporter().getId());
    assertEquals(title, target.getTitle());
    assertEquals(description, target.getDescription());
    assertEquals(isProcessedComplete, target.getIsProcessedComplete());
    assertEquals(reportResult, target.getReportResult());
    assertEquals(review.getId(), target.getReview().getId());
  }
}
