package com.project.shoppingmall.controller_manage.report;

import com.project.shoppingmall.controller_manage.report.input.InputProcessProductReport;
import com.project.shoppingmall.controller_manage.report.input.InputProcessReviewReport;
import com.project.shoppingmall.controller_manage.report.output.OutputProcessProductReport;
import com.project.shoppingmall.controller_manage.report.output.OutputProcessReviewReport;
import com.project.shoppingmall.entity.report.ProductReport;
import com.project.shoppingmall.entity.report.ReviewReport;
import com.project.shoppingmall.service_manage.report.ReportProcessManagerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ReportManageController {
  private final ReportProcessManagerService reportProcessManagerService;

  @PutMapping("/product/report/state")
  @PreAuthorize("hasAnyRole('ROLE_ROOT_MANAGER', 'ROLE_COMMON_MANAGER')")
  public OutputProcessProductReport processProductReport(
      @Valid @RequestBody InputProcessProductReport input) {
    ProductReport productReport =
        reportProcessManagerService.processProductReport(
            input.getProductReportId(), input.getResultType());
    return new OutputProcessProductReport(productReport.getId());
  }

  @PutMapping("/review/report/state")
  @PreAuthorize("hasAnyRole('ROLE_ROOT_MANAGER', 'ROLE_COMMON_MANAGER')")
  public OutputProcessReviewReport processReviewReport(
      @Valid @RequestBody InputProcessReviewReport input) {
    ReviewReport reviewReport =
        reportProcessManagerService.processReviewReport(
            input.getReviewReportId(), input.getResultType());
    return new OutputProcessReviewReport(reviewReport.getId());
  }
}
