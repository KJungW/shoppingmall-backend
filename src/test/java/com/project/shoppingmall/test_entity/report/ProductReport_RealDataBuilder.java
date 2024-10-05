package com.project.shoppingmall.test_entity.report;

import com.project.shoppingmall.entity.Member;
import com.project.shoppingmall.entity.Product;
import com.project.shoppingmall.entity.report.ProductReport;
import com.project.shoppingmall.test_entity.member.MemberBuilder;
import com.project.shoppingmall.test_entity.product.ProductBuilder;
import com.project.shoppingmall.type.ReportResultType;
import org.springframework.test.util.ReflectionTestUtils;

public class ProductReport_RealDataBuilder {
  public static ProductReport.ProductReportBuilder fullData() {
    return ProductReport.builder()
        .reporter(MemberBuilder.makeMember(401230L))
        .title("test title")
        .description("test description")
        .product(ProductBuilder.makeProduct(5120320L));
  }

  public static ProductReport make(Member reporter, Product product, ReportResultType state) {
    ProductReport report =
        ProductReport_RealDataBuilder.fullData().product(product).reporter(reporter).build();
    ReflectionTestUtils.setField(report, "reportResult", state);
    if (state.equals(ReportResultType.WAITING_PROCESSED))
      ReflectionTestUtils.setField(report, "isProcessedComplete", false);
    else ReflectionTestUtils.setField(report, "isProcessedComplete", true);
    return report;
  }
}
