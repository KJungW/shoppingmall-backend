package com.project.shoppingmall.controller.report.input;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.hibernate.validator.constraints.Length;

@Getter
@AllArgsConstructor
public class InputSaveReportAboutReview {
  @NotNull Long reviewId;

  @NotBlank
  @Length(min = 1, max = 100)
  String reportTitle;

  @NotBlank
  @Length(min = 1, max = 500)
  String reportDescription;
}
