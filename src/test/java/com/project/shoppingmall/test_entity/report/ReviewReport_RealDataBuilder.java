package com.project.shoppingmall.test_entity.report;

import com.project.shoppingmall.entity.Member;
import com.project.shoppingmall.entity.Review;
import com.project.shoppingmall.entity.report.ReviewReport;
import com.project.shoppingmall.test_entity.member.MemberBuilder;
import com.project.shoppingmall.test_entity.review.ReviewBuilder;
import com.project.shoppingmall.type.ReportResultType;
import org.springframework.test.util.ReflectionTestUtils;

public class ReviewReport_RealDataBuilder {
  public static ReviewReport.ReviewReportBuilder fullData() {
    return ReviewReport.builder()
        .reporter(MemberBuilder.makeMember(401230L))
        .title("test title")
        .description("test description")
        .review(ReviewBuilder.makeReview(500213L));
  }

  public static ReviewReport make(Member reporter, Review review, ReportResultType state) {
    ReviewReport report = fullData().review(review).reporter(reporter).build();
    ReflectionTestUtils.setField(report, "reportResult", state);
    if (state.equals(ReportResultType.WAITING_PROCESSED))
      ReflectionTestUtils.setField(report, "isProcessedComplete", false);
    else ReflectionTestUtils.setField(report, "isProcessedComplete", true);
    return report;
  }
}
