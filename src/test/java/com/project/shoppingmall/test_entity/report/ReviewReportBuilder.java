package com.project.shoppingmall.test_entity.report;

import com.project.shoppingmall.entity.report.ReviewReport;
import com.project.shoppingmall.test_entity.member.MemberBuilder;
import com.project.shoppingmall.test_entity.review.ReviewBuilder;
import com.project.shoppingmall.type.ReportResultType;
import java.time.LocalDateTime;
import org.springframework.test.util.ReflectionTestUtils;

public class ReviewReportBuilder {
  public static ReviewReport.ReviewReportBuilder fullData() {
    return ReviewReport.builder()
        .reporter(MemberBuilder.makeMember(401230L))
        .title("test title")
        .description("test description")
        .review(ReviewBuilder.makeReview(500213L));
  }

  public static ReviewReport make(long id, ReportResultType state, LocalDateTime createDate) {
    ReviewReport report = fullData().build();
    ReflectionTestUtils.setField(report, "id", id);
    ReflectionTestUtils.setField(report, "createDate", createDate);
    ReflectionTestUtils.setField(report, "reportResult", state);
    setIsProcessedComplete(state, report);
    return report;
  }

  public static ReviewReport make(long id, ReportResultType state) {
    ReviewReport report = fullData().build();
    ReflectionTestUtils.setField(report, "id", id);
    ReflectionTestUtils.setField(report, "reportResult", state);
    setIsProcessedComplete(state, report);
    return report;
  }

  private static void setIsProcessedComplete(ReportResultType state, ReviewReport target) {
    if (state.equals(ReportResultType.WAITING_PROCESSED))
      ReflectionTestUtils.setField(target, "isProcessedComplete", false);
    else ReflectionTestUtils.setField(target, "isProcessedComplete", true);
  }
}
