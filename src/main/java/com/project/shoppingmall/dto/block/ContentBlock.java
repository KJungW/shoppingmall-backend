package com.project.shoppingmall.dto.block;

import com.project.shoppingmall.type.BlockType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ContentBlock {
  private Long index;
  private BlockType blockType;
}
