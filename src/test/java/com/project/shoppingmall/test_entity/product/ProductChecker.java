package com.project.shoppingmall.test_entity.product;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.project.shoppingmall.dto.block.ContentBlock;
import com.project.shoppingmall.dto.file.FileUploadResult;
import com.project.shoppingmall.dto.product.ProductMakeData;
import com.project.shoppingmall.dto.product.ProductOption;
import com.project.shoppingmall.entity.*;
import com.project.shoppingmall.type.BlockType;
import com.project.shoppingmall.util.PriceCalculateUtil;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public class ProductChecker {
  public static void checkProduct(
      Member givenMember,
      ProductMakeData givenMakeData,
      FileUploadResult givenFileUploadResult,
      Product targetProduct) {
    assertEquals(givenMember.getId(), targetProduct.getSeller().getId());
    assertEquals(givenMakeData.getProductTypeId(), targetProduct.getProductType().getId());
    checkProductImage(
        givenFileUploadResult, givenMakeData.getProductImages(), targetProduct.getProductImages());
    checkProductBlock(givenMakeData.getContentBlocks(), targetProduct.getContents());
    checkProductSingleOption(givenMakeData.getSingleOptions(), targetProduct.getSingleOptions());
    checkProductMultiOptions(givenMakeData.getMultiOptions(), targetProduct.getMultipleOptions());
    assertEquals(givenMakeData.getName(), targetProduct.getName());
    assertEquals(givenMakeData.getPrice(), targetProduct.getPrice());
    assertEquals(givenMakeData.getDiscountAmount(), targetProduct.getDiscountAmount());
    assertEquals(givenMakeData.getDiscountRate(), targetProduct.getDiscountRate());
    assertEquals(false, targetProduct.getIsBan());
    assertEquals(0d, targetProduct.getScoreAvg());
    checkProductFinalPrice(givenMakeData, targetProduct.getFinalPrice());
  }

  public static void checkProductImage(
      FileUploadResult givenFileUploadResult,
      List<MultipartFile> givenProductImages,
      List<ProductImage> targetProductImages) {
    assertEquals(givenProductImages.size(), targetProductImages.size());
    targetProductImages.forEach(
        image -> {
          assertEquals(givenFileUploadResult.getFileServerUri(), image.getImageUri());
          assertEquals(givenFileUploadResult.getDownLoadUrl(), image.getDownLoadUrl());
        });
  }

  public static void checkProductBlock(
      List<ContentBlock> givenContentBlocks, List<ProductContent> targetProductBlocks) {
    int expectedTextBlockCount =
        givenContentBlocks.stream()
            .filter(block -> block.getBlockType().equals(BlockType.TEXT_TYPE))
            .toList()
            .size();
    int expectedImageBlockCount =
        givenContentBlocks.stream()
            .filter(block -> block.getBlockType().equals(BlockType.IMAGE_TYPE))
            .toList()
            .size();
    assertEquals(expectedTextBlockCount + expectedImageBlockCount, targetProductBlocks.size());
  }

  public static void checkProductSingleOption(
      List<ProductOption> givenSingleOptions, List<ProductSingleOption> targetSingleOptions) {
    List<String> expectedSingleOptionNames =
        givenSingleOptions.stream().map(ProductOption::getOptionName).toList();
    List<String> resultSingleOptionNames =
        targetSingleOptions.stream().map(ProductSingleOption::getOptionName).toList();
    assertArrayEquals(expectedSingleOptionNames.toArray(), resultSingleOptionNames.toArray());

    List<Integer> expectedSingleOptionPriceChange =
        givenSingleOptions.stream().map(ProductOption::getPriceChangeAmount).toList();
    List<Integer> resultSingleOptionPriceChange =
        targetSingleOptions.stream().map(ProductSingleOption::getPriceChangeAmount).toList();
    assertArrayEquals(
        expectedSingleOptionPriceChange.toArray(), resultSingleOptionPriceChange.toArray());
  }

  public static void checkProductMultiOptions(
      List<ProductOption> givenMultiOptions, List<ProductMultipleOption> targetMultiOptions) {
    List<String> expectedOptionNames =
        givenMultiOptions.stream().map(ProductOption::getOptionName).toList();
    List<String> resultOptionNames =
        targetMultiOptions.stream().map(ProductMultipleOption::getOptionName).toList();
    assertArrayEquals(expectedOptionNames.toArray(), resultOptionNames.toArray());

    List<Integer> expectedOptionPriceChange =
        givenMultiOptions.stream().map(ProductOption::getPriceChangeAmount).toList();
    List<Integer> resultOptionPriceChange =
        targetMultiOptions.stream().map(ProductMultipleOption::getPriceChangeAmount).toList();
    assertArrayEquals(expectedOptionPriceChange.toArray(), resultOptionPriceChange.toArray());
  }

  public static void checkProductFinalPrice(ProductMakeData givenMakeData, int targetPrice) {
    int expectedFinalPrice =
        PriceCalculateUtil.calculatePrice(
            givenMakeData.getPrice(),
            givenMakeData.getDiscountAmount(),
            givenMakeData.getDiscountRate());
    assertEquals(expectedFinalPrice, targetPrice);
  }
}
