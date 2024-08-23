package com.project.shoppingmall.controller_manage.report.input;

import com.project.shoppingmall.type.ReportResultTypeForApi;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class InputProcessProductReport {
  @NotNull private Long productReportId;
  @NotNull private ReportResultTypeForApi resultType;
}
