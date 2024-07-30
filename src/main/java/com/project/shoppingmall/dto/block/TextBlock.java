package com.project.shoppingmall.dto.block;

import com.project.shoppingmall.type.BlockType;
import lombok.Getter;

@Getter
public class TextBlock extends ContentBlock {
  private String content;

  public TextBlock(Long index, String content) {
    super(index, BlockType.TEXT_TYPE);
    this.content = content;
  }
}
