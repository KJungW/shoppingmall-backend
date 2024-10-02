package com.project.shoppingmall.testdata.report;

import com.project.shoppingmall.entity.Member;
import com.project.shoppingmall.entity.Product;
import com.project.shoppingmall.entity.report.ProductReport;
import com.project.shoppingmall.testdata.member.MemberBuilder;
import com.project.shoppingmall.testdata.product.ProductBuilder;
import com.project.shoppingmall.type.ReportResultType;
import org.springframework.test.util.ReflectionTestUtils;

public class ProductReport_RealDataBuilder {
  public static ProductReport.ProductReportBuilder fullData() {
    Member givenMember = MemberBuilder.fullData().build();
    Product givenProduct = ProductBuilder.fullData().build();
    return ProductReport.builder()
        .reporter(givenMember)
        .title("Test Title")
        .description("Test Description")
        .product(givenProduct);
  }

  public static ProductReport makeProductReport(
      Member reporter, Product product, boolean isProcessedComplete) {
    ProductReport report =
        ProductReport_RealDataBuilder.fullData().product(product).reporter(reporter).build();
    ReflectionTestUtils.setField(report, "isProcessedComplete", isProcessedComplete);
    if (isProcessedComplete) {
      ReflectionTestUtils.setField(report, "reportResult", ReportResultType.NO_ACTION);
    }
    return report;
  }
}
