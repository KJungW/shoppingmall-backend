package com.project.shoppingmall.controller.report.input;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class InputSaveReportAboutProduct {
  @NotNull long productId;
  @NotEmpty String reportTitle;
  @NotEmpty String reportDescription;
}
