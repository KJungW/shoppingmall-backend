package com.project.shoppingmall.controller.product.input;

import com.project.shoppingmall.type.BlockType;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class InputBlockData {
  @NotNull private Long index;
  @NotNull private BlockType blockType;
  @NotEmpty private String content;
}
