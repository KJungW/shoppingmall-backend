package com.project.shoppingmall.testdata;

import com.project.shoppingmall.entity.Member;
import com.project.shoppingmall.entity.Review;
import com.project.shoppingmall.entity.report.ReviewReport;
import com.project.shoppingmall.type.ReportResultType;
import java.io.IOException;
import org.springframework.test.util.ReflectionTestUtils;

public class ReviewReportBuilder {
  public static ReviewReport.ReviewReportBuilder fullData() throws IOException {
    return ReviewReport.builder()
        .reporter(MemberBuilder.fullData().build())
        .title("test title")
        .description("test description")
        .review(ReviewBuilder.fullData().build());
  }

  public static ReviewReport makeNoProcessedReviewReport(Member reporter, Review review)
      throws IOException {
    ReviewReport report = ReviewReportBuilder.fullData().review(review).reporter(reporter).build();
    ReflectionTestUtils.setField(report, "isProcessedComplete", false);
    return report;
  }

  public static ReviewReport makeProcessedReviewReport(Member reporter, Review review)
      throws IOException {
    ReviewReport report = ReviewReportBuilder.fullData().review(review).reporter(reporter).build();
    ReflectionTestUtils.setField(report, "isProcessedComplete", true);
    ReflectionTestUtils.setField(report, "reportResult", ReportResultType.NO_ACTION);
    return report;
  }
}
