package com.project.shoppingmall.dto.block;

import com.project.shoppingmall.type.BlockType;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ContentBlock {
  private Long index;
  private BlockType blockType;
}
