package com.project.shoppingmall.controller.report.input;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class InputSaveReportAboutReview {
  @NotNull Long reviewId;
  @NotBlank String reportTitle;
  @NotBlank String reportDescription;
}
