package com.project.shoppingmall.dto.product;

import com.project.shoppingmall.controller.product.input.InputBlockData;
import com.project.shoppingmall.controller.product.input.InputProductOption;
import com.project.shoppingmall.dto.block.ContentBlock;
import com.project.shoppingmall.dto.block.ImageBlockBeforeImageSave;
import com.project.shoppingmall.dto.block.TextBlock;
import com.project.shoppingmall.exception.InvalidEnumType;
import com.project.shoppingmall.exception.NotMatchBlockAndImage;
import com.project.shoppingmall.type.BlockType;
import com.project.shoppingmall.util.FileUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;

@Getter
public class ProductMakeData {
  private Long productTypeId;
  private String name;
  private ProductOption singleOption;
  private Integer price;
  private Integer discountAmount;
  private Double discountRate;

  private List<MultipartFile> productImages;
  private List<ProductOption> multiOptions;
  private List<ContentBlock> contentBlocks;

  @Builder
  public ProductMakeData(
      Long productTypeId,
      String name,
      InputProductOption singleOption,
      Integer price,
      Integer discountAmount,
      Double discountRate,
      List<MultipartFile> productImages,
      List<InputProductOption> multiOptions,
      List<InputBlockData> blockDataList,
      List<MultipartFile> blockImages) {
    this.productTypeId = productTypeId;
    this.name = name;
    this.price = price;
    this.discountAmount = discountAmount;
    this.discountRate = discountRate;
    initSingleProductOption(singleOption);
    initMultipleProductOption(multiOptions);
    initProductImages(productImages);
    initContentBlocks(blockDataList, blockImages);
  }

  private void initSingleProductOption(InputProductOption singleOption) {
    this.singleOption =
        new ProductOption(singleOption.getOptionName(), singleOption.getPriceChangeAmount());
  }

  private void initMultipleProductOption(List<InputProductOption> multiOptions) {
    if (multiOptions == null) {
      this.multiOptions = new ArrayList<>();
      return;
    }
    this.multiOptions =
        multiOptions.stream()
            .map(input -> new ProductOption(input.getOptionName(), input.getPriceChangeAmount()))
            .collect(Collectors.toList());
  }

  private void initProductImages(List<MultipartFile> productImages) {
    if (productImages == null) {
      this.productImages = new ArrayList<>();
      return;
    }

    FileUtils.sortMultiPartFilesByName(productImages);
    this.productImages = productImages;
  }

  private void initContentBlocks(
      List<InputBlockData> blockDataList, List<MultipartFile> blockImages) {
    if (blockDataList == null && blockImages == null) {
      this.contentBlocks = new ArrayList<>();
      return;
    }

    if (blockDataList == null || blockImages == null) {
      throw new NotMatchBlockAndImage("블록과 이미지가 매치되지 않습니다.");
    }

    ArrayList<ContentBlock> resultBlockList = new ArrayList<>();
    for (InputBlockData blockData : blockDataList) {
      if (blockData.getBlockType().equals(BlockType.TEXT_TYPE)) {
        resultBlockList.add(new TextBlock(blockData.getIndex(), blockData.getContent()));
      } else if (blockData.getBlockType().equals(BlockType.IMAGE_TYPE)) {
        MultipartFile findImage =
            blockImages.stream()
                .filter(blockImg -> blockData.getContent().equals(blockImg.getOriginalFilename()))
                .findFirst()
                .orElseThrow(() -> new NotMatchBlockAndImage("블록과 이미지가 매치되지 않습니다."));
        ImageBlockBeforeImageSave imageBlock =
            new ImageBlockBeforeImageSave(blockData.getIndex(), findImage);
        resultBlockList.add(imageBlock);
      } else {
        throw new InvalidEnumType("유효하지 않은 블록 타입입니다.");
      }
    }
    this.contentBlocks = resultBlockList;
  }
}
