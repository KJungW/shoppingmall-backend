package com.project.shoppingmall.dto.block;

import com.project.shoppingmall.type.BlockType;
import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;

@Getter
public class ImageBlockBeforeImageSave extends ContentBlock {
  private MultipartFile image;

  public ImageBlockBeforeImageSave(Long index, MultipartFile image) {
    super(index, BlockType.IMAGE_TYPE);
    this.image = image;
  }
}
