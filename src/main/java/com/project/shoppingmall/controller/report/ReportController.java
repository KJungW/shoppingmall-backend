package com.project.shoppingmall.controller.report;

import com.project.shoppingmall.controller.report.input.InputProcessProductReport;
import com.project.shoppingmall.controller.report.input.InputProcessReviewReport;
import com.project.shoppingmall.controller.report.input.InputSaveReportAboutProduct;
import com.project.shoppingmall.controller.report.input.InputSaveReportAboutReview;
import com.project.shoppingmall.controller.report.output.OutputProcessProductReport;
import com.project.shoppingmall.controller.report.output.OutputProcessReviewReport;
import com.project.shoppingmall.dto.auth.AuthMemberDetail;
import com.project.shoppingmall.entity.report.ProductReport;
import com.project.shoppingmall.entity.report.ReviewReport;
import com.project.shoppingmall.service.report.ReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ReportController {
  private final ReportService reportService;

  @PostMapping("/report")
  @PreAuthorize("hasRole('ROLE_MEMBER')")
  public void saveReportAboutProduct(@Valid @RequestBody InputSaveReportAboutProduct input) {
    AuthMemberDetail userDetail =
        (AuthMemberDetail) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    reportService.saveProductReport(
        userDetail.getId(),
        input.getProductId(),
        input.getReportTitle(),
        input.getReportDescription());
  }

  @PostMapping("/review/report")
  @PreAuthorize("hasRole('ROLE_MEMBER')")
  public void saveReportAboutReview(@Valid @RequestBody InputSaveReportAboutReview input) {
    AuthMemberDetail userDetail =
        (AuthMemberDetail) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    reportService.saveReviewReport(
        userDetail.getId(),
        input.getReviewId(),
        input.getReportTitle(),
        input.getReportDescription());
  }

  @PutMapping("/product/report/state")
  @PreAuthorize("hasAnyRole('ROLE_ROOT_MANAGER', 'ROLE_COMMON_MANAGER')")
  public OutputProcessProductReport processProductReport(
      @Valid @RequestBody InputProcessProductReport input) {
    ProductReport productReport =
        reportService.processProductReport(input.getProductReportId(), input.getResultType());
    return new OutputProcessProductReport(productReport.getId());
  }

  @PutMapping("/review/report/state")
  @PreAuthorize("hasAnyRole('ROLE_ROOT_MANAGER', 'ROLE_COMMON_MANAGER')")
  public OutputProcessReviewReport processReviewReport(
      @Valid @RequestBody InputProcessReviewReport input) {
    ReviewReport reviewReport =
        reportService.processReviewReport(input.getReviewReportId(), input.getResultType());
    return new OutputProcessReviewReport(reviewReport.getId());
  }
}
