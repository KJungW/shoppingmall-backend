package com.project.shoppingmall.dto.block;

import com.project.shoppingmall.type.BlockType;
import lombok.Getter;

@Getter
public class ImageBlock extends ContentBlock {
  private String imageUri;
  private String downloadUrl;

  public ImageBlock(Long index, String imageUri, String downloadUrl) {
    super(index, BlockType.IMAGE_TYPE);
    this.imageUri = imageUri;
    this.downloadUrl = downloadUrl;
  }
}
