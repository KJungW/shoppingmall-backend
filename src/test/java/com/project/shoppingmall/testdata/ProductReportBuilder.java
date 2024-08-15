package com.project.shoppingmall.testdata;

import com.project.shoppingmall.entity.Member;
import com.project.shoppingmall.entity.Product;
import com.project.shoppingmall.entity.report.ProductReport;
import java.io.IOException;

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
}
