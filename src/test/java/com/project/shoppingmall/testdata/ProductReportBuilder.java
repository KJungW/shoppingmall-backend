package com.project.shoppingmall.testdata;

import com.project.shoppingmall.entity.Member;
import com.project.shoppingmall.entity.Product;
import com.project.shoppingmall.entity.report.ProductReport;
import com.project.shoppingmall.type.ReportResultType;
import java.io.IOException;
import org.springframework.test.util.ReflectionTestUtils;

public class ProductReportBuilder {
  public static ProductReport.ProductReportBuilder fullData() throws IOException {
    Member givenMember = MemberBuilder.fullData().build();
    Product givenProduct = ProductBuilder.fullData().build();
    return ProductReport.builder()
        .reporter(givenMember)
        .title("Test Title")
        .description("Test Description")
        .product(givenProduct);
  }

  public static ProductReport makeNoProcessedProductReport(Member reporter, Product product)
      throws IOException {
    ProductReport report =
        ProductReportBuilder.fullData().product(product).reporter(reporter).build();
    ReflectionTestUtils.setField(report, "isProcessedComplete", false);
    return report;
  }

  public static ProductReport makeProcessedProductReportTestData(Member reporter, Product product)
      throws IOException {
    ProductReport report =
        ProductReportBuilder.fullData().product(product).reporter(reporter).build();
    ReflectionTestUtils.setField(report, "isProcessedComplete", true);
    ReflectionTestUtils.setField(report, "reportResult", ReportResultType.NO_ACTION);
    return report;
  }
}
