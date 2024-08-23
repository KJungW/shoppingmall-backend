package com.project.shoppingmall.controller_manage.report_retrieve;

import com.project.shoppingmall.controller_manage.report_retrieve.output.OutputRetrieveProductReportByType;
import com.project.shoppingmall.controller_manage.report_retrieve.output.OutputRetrieveProductReportsByProductSeller;
import com.project.shoppingmall.controller_manage.report_retrieve.output.OutputRetrieveReviewReportByType;
import com.project.shoppingmall.controller_manage.report_retrieve.output.OutputRetrieveReviewReportsByReviewWriter;
import com.project.shoppingmall.entity.report.ProductReport;
import com.project.shoppingmall.entity.report.ReviewReport;
import com.project.shoppingmall.service_manage.report.ReportRetrieveManagerService;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Slice;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ReportRetrieveController {
  private final ReportRetrieveManagerService reportRetrieveManagerService;

  @GetMapping("/product-type/product-report")
  @PreAuthorize("hasAnyRole('ROLE_ROOT_MANAGER', 'ROLE_COMMON_MANAGER')")
  public OutputRetrieveProductReportByType retrieveUnprocessedProductReport(
      @PositiveOrZero @RequestParam("sliceNumber") Integer sliceNumber,
      @Positive @RequestParam("sliceSize") Integer sliceSize,
      @RequestParam("productTypeId") Long productTypeId) {
    Slice<ProductReport> sliceResult =
        reportRetrieveManagerService.findUnprocessedProductReport(
            productTypeId, sliceNumber, sliceSize);
    return new OutputRetrieveProductReportByType(sliceResult);
  }

  @GetMapping("/product-type/review-report")
  @PreAuthorize("hasAnyRole('ROLE_ROOT_MANAGER', 'ROLE_COMMON_MANAGER')")
  public OutputRetrieveReviewReportByType retrieveUnprocessedReviewReport(
      @PositiveOrZero @RequestParam("sliceNumber") Integer sliceNumber,
      @Positive @RequestParam("sliceSize") Integer sliceSize,
      @RequestParam("productTypeId") Long productTypeId) {
    Slice<ReviewReport> sliceResult =
        reportRetrieveManagerService.findUnprocessedReviewReportReport(
            productTypeId, sliceNumber, sliceSize);
    return new OutputRetrieveReviewReportByType(sliceResult);
  }

  @GetMapping("/product/seller/reports")
  @PreAuthorize("hasAnyRole('ROLE_ROOT_MANAGER', 'ROLE_COMMON_MANAGER')")
  public OutputRetrieveProductReportsByProductSeller retrieveProductReportsByProductSeller(
      @PositiveOrZero @RequestParam("sliceNumber") Integer sliceNumber,
      @Positive @RequestParam("sliceSize") Integer sliceSize,
      @RequestParam("productSellerId") Long productSellerId) {
    Slice<ProductReport> sliceResult =
        reportRetrieveManagerService.findProductReportsByProductSeller(
            productSellerId, sliceNumber, sliceSize);
    return new OutputRetrieveProductReportsByProductSeller(sliceResult);
  }

  @GetMapping("/review/writer/reports")
  @PreAuthorize("hasAnyRole('ROLE_ROOT_MANAGER', 'ROLE_COMMON_MANAGER')")
  public OutputRetrieveReviewReportsByReviewWriter retrieveReviewReportsByReviewWriter(
      @PositiveOrZero @RequestParam("sliceNumber") Integer sliceNumber,
      @Positive @RequestParam("sliceSize") Integer sliceSize,
      @RequestParam("reviewWriterId") Long reviewWriterId) {
    Slice<ReviewReport> sliceResult =
        reportRetrieveManagerService.findReviewReportsByReviewWriter(
            reviewWriterId, sliceNumber, sliceSize);
    return new OutputRetrieveReviewReportsByReviewWriter(sliceResult);
  }
}
