package com.project.shoppingmall.controller.review.input;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;

@Getter
@AllArgsConstructor
public class InputUpdateReview {
  @NotNull private Long reviewId;

  @NotNull
  @Range(min = 0, max = 5)
  private Integer score;

  @NotBlank
  @Length(min = 1, max = 100)
  private String title;

  private String description;
}
