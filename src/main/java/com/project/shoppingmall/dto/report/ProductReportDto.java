package com.project.shoppingmall.dto.report;

import com.project.shoppingmall.entity.report.ProductReport;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ProductReportDto {
  private Long reportId;
  private LocalDateTime reportCreatedDate;
  private Long reporterId;
  private String reporterName;
  private String title;
  private String description;
  private Boolean isProcessedComplete;
  private Long productId;
  private String productName;
  private Long sellerId;
  private String sellerName;

  public ProductReportDto(ProductReport report) {
    this.reportId = report.getId();
    this.reportCreatedDate = report.getCreateDate();
    this.reporterId = report.getReporter().getId();
    this.reporterName = report.getReporter().getNickName();
    this.title = report.getTitle();
    this.description = report.getDescription();
    this.isProcessedComplete = report.isProcessedComplete();
    this.productId = report.getProduct().getId();
    this.productName = report.getProduct().getName();
    this.sellerId = report.getProduct().getSeller().getId();
    this.sellerName = report.getProduct().getSeller().getNickName();
  }
}
