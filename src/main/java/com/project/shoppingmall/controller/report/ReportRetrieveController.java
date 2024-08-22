package com.project.shoppingmall.controller.report;

import com.project.shoppingmall.controller.report.output.OutputRetrieveProductReportByType;
import com.project.shoppingmall.controller.report.output.OutputRetrieveProductReportsByProductSeller;
import com.project.shoppingmall.controller.report.output.OutputRetrieveReviewReportByType;
import com.project.shoppingmall.controller.report.output.OutputRetrieveReviewReportsByReviewWriter;
import com.project.shoppingmall.entity.report.ProductReport;
import com.project.shoppingmall.entity.report.ReviewReport;
import com.project.shoppingmall.service.report.ReportRetrieveService;
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
  private final ReportRetrieveService reportRetrieveService;

  @GetMapping("/product-type/product-report")
  @PreAuthorize("hasAnyRole('ROLE_ROOT_MANAGER', 'ROLE_COMMON_MANAGER')")
  public OutputRetrieveProductReportByType retrieveUnprocessedProductReport(
      @PositiveOrZero @RequestParam("sliceNumber") Integer sliceNumber,
      @Positive @RequestParam("sliceSize") Integer sliceSize,
      @RequestParam("productTypeId") Long productTypeId) {
    Slice<ProductReport> sliceResult =
        reportRetrieveService.findUnprocessedProductReport(productTypeId, sliceNumber, sliceSize);
    return new OutputRetrieveProductReportByType(sliceResult);
  }

  @GetMapping("/product-type/review-report")
  @PreAuthorize("hasAnyRole('ROLE_ROOT_MANAGER', 'ROLE_COMMON_MANAGER')")
  public OutputRetrieveReviewReportByType retrieveUnprocessedReviewReport(
      @PositiveOrZero @RequestParam("sliceNumber") Integer sliceNumber,
      @Positive @RequestParam("sliceSize") Integer sliceSize,
      @RequestParam("productTypeId") Long productTypeId) {
    Slice<ReviewReport> sliceResult =
        reportRetrieveService.findUnprocessedReviewReportReport(
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
        reportRetrieveService.findProductReportsByProductSeller(
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
        reportRetrieveService.findReviewReportsByReviewWriter(
            reviewWriterId, sliceNumber, sliceSize);
    return new OutputRetrieveReviewReportsByReviewWriter(sliceResult);
  }
}
