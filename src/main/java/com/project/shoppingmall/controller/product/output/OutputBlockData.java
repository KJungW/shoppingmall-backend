package com.project.shoppingmall.controller.product.output;

import com.project.shoppingmall.dto.block.ImageBlock;
import com.project.shoppingmall.dto.block.TextBlock;
import com.project.shoppingmall.type.BlockType;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OutputBlockData {
  private Long index;
  private BlockType blockType;
  private String contentInTextBlock;
  private String imgDownloadUrlInImageBlock;

  public OutputBlockData(ImageBlock block) {
    this.index = block.getIndex();
    this.blockType = block.getBlockType();
    this.contentInTextBlock = "";
    this.imgDownloadUrlInImageBlock = block.getDownloadUrl();
  }

  public OutputBlockData(TextBlock block) {
    this.index = block.getIndex();
    this.blockType = block.getBlockType();
    this.contentInTextBlock = block.getContent();
    this.imgDownloadUrlInImageBlock = "";
  }
}
