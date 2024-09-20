package com.project.shoppingmall.controller.product.input;

import com.project.shoppingmall.type.BlockType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.hibernate.validator.constraints.Length;

@Getter
@AllArgsConstructor
public class InputBlockData {
  @NotNull private Long index;
  @NotNull private BlockType blockType;

  @NotBlank
  @Length(min = 1, max = 500)
  private String content;
}
