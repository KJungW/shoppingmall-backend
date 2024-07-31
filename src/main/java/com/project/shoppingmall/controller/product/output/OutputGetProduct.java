package com.project.shoppingmall.controller.product.output;

import com.project.shoppingmall.dto.block.ImageBlock;
import com.project.shoppingmall.dto.block.TextBlock;
import com.project.shoppingmall.entity.Product;
import com.project.shoppingmall.entity.ProductContent;
import com.project.shoppingmall.entity.ProductImage;
import com.project.shoppingmall.exception.InvalidEnumType;
import com.project.shoppingmall.type.BlockType;
import com.project.shoppingmall.util.JsonUtil;
import java.awt.*;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OutputGetProduct {
  private Long productId;
  private Long sellerId;
  private Long ProductTypeId;
  private List<String> productImageDownloadUrlList;
  private List<OutputBlockData> blockDataList;
  private List<OutputProductOption> singleOptions;
  private List<OutputProductOption> multipleOptions;
  private String name;
  private Integer price;
  private Integer discountAmount;
  private Double discountRate;
  private Boolean isBan;
  private Double scoreAvg;

  public OutputGetProduct(Product product) {
    this.productId = product.getId();
    this.sellerId = product.getSeller().getId();
    this.ProductTypeId = product.getProductType().getId();
    this.productImageDownloadUrlList =
        product.getProductImages().stream().map(ProductImage::getDownLoadUrl).toList();
    this.blockDataList = makeOutputBlockDataList(product.getContents());
    this.singleOptions = product.getSingleOptions().stream().map(OutputProductOption::new).toList();
    this.multipleOptions =
        product.getMultipleOptions().stream().map(OutputProductOption::new).toList();
    this.name = product.getName();
    this.price = product.getPrice();
    this.discountAmount = product.getDiscountAmount();
    this.discountRate = product.getDiscountRate();
    this.isBan = product.getIsBan();
    this.scoreAvg = product.getScoreAvg();
  }

  private List<OutputBlockData> makeOutputBlockDataList(List<ProductContent> productContents) {
    return productContents.stream()
        .map(
            content -> {
              if (content.getType() == BlockType.TEXT_TYPE) {
                TextBlock textBlock =
                    JsonUtil.convertJsonToObject(content.getContent(), TextBlock.class);
                return new OutputBlockData(textBlock);
              } else if (content.getType() == BlockType.IMAGE_TYPE) {
                ImageBlock imageBlock =
                    JsonUtil.convertJsonToObject(content.getContent(), ImageBlock.class);
                return new OutputBlockData(imageBlock);
              } else {
                throw new InvalidEnumType("유효하지 않은 블록타입 입니다.");
              }
            })
        .toList();
  }
}
