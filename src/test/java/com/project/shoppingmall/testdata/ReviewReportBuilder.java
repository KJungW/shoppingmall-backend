package com.project.shoppingmall.testdata;

import com.project.shoppingmall.entity.report.ReviewReport;
import java.io.IOException;

public class ReviewReportBuilder {
  public static ReviewReport.ReviewReportBuilder fullData() throws IOException {
    return ReviewReport.builder()
        .reporter(MemberBuilder.fullData().build())
        .title("test title")
        .description("test description")
        .review(ReviewBuilder.fullData().build());
  }
}
