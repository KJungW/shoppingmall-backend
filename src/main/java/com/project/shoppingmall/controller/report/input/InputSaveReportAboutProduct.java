package com.project.shoppingmall.controller.report.input;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.hibernate.validator.constraints.Length;

@Getter
@AllArgsConstructor
public class InputSaveReportAboutProduct {
  @NotNull private Long productId;

  @NotBlank
  @Length(min = 1, max = 100)
  private String reportTitle;

  @NotBlank
  @Length(min = 1, max = 500)
  private String reportDescription;
}
