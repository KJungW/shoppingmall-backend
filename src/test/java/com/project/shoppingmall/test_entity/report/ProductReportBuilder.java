package com.project.shoppingmall.test_entity.report;

import com.project.shoppingmall.entity.report.ProductReport;
import com.project.shoppingmall.test_entity.member.MemberBuilder;
import com.project.shoppingmall.test_entity.product.ProductBuilder;
import com.project.shoppingmall.type.ReportResultType;
import java.time.LocalDateTime;
import org.springframework.test.util.ReflectionTestUtils;

public class ProductReportBuilder {
  public static ProductReport.ProductReportBuilder fullData() {
    return ProductReport.builder()
        .reporter(MemberBuilder.makeMember(401230L))
        .title("test title")
        .description("test description")
        .product(ProductBuilder.makeProduct(5120320L));
  }

  public static ProductReport make(long id, ReportResultType state, LocalDateTime createDate) {
    ProductReport report = fullData().build();
    ReflectionTestUtils.setField(report, "id", id);
    ReflectionTestUtils.setField(report, "createDate", createDate);
    ReflectionTestUtils.setField(report, "reportResult", state);
    setIsProcessedComplete(state, report);
    return report;
  }

  public static ProductReport make(long id, ReportResultType state) {
    ProductReport report = fullData().build();
    ReflectionTestUtils.setField(report, "id", id);
    ReflectionTestUtils.setField(report, "reportResult", state);
    setIsProcessedComplete(state, report);
    return report;
  }

  private static void setIsProcessedComplete(ReportResultType state, ProductReport target) {
    if (state.equals(ReportResultType.WAITING_PROCESSED))
      ReflectionTestUtils.setField(target, "isProcessedComplete", false);
    else ReflectionTestUtils.setField(target, "isProcessedComplete", true);
  }
}
