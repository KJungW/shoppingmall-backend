package com.project.shoppingmall.testdata.product;

import com.project.shoppingmall.controller.product.input.InputBlockData;
import com.project.shoppingmall.controller.product.input.InputProductOption;
import com.project.shoppingmall.dto.product.ProductMakeData;
import com.project.shoppingmall.entity.ProductType;
import com.project.shoppingmall.type.BlockType;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

public class ProductMakeDataBuilder {
  public static final Integer PRODUCT_IMAGE_COUNT = 3;
  public static final Integer TEXT_BLOCK_COUNT = 1;
  public static final Integer IMAGE_BLOCK_COUNT = 3;

  public static ProductMakeData.ProductMakeDataBuilder fullData() {
    Long givenProductTypeId = 1L;
    String givenName = "testProduct";
    Integer givenPrice = 10000;
    Integer givenDiscountAmount = 500;
    Double givenDiscountRate = 10.5;
    ArrayList<MultipartFile> givenProductImageList =
        new ArrayList<>(
            List.of(
                makeMockMultipartFile("1.png", "src/test/resources/static/product_image/1.png"),
                makeMockMultipartFile("2.png", "src/test/resources/static/product_image/2.png"),
                makeMockMultipartFile("3.png", "src/test/resources/static/product_image/3.png")));
    ArrayList<InputProductOption> givenSingleOptions =
        new ArrayList<>(
            List.of(
                new InputProductOption("multiOption1", 1000),
                new InputProductOption("multiOption2", 2000),
                new InputProductOption("multiOption3", 3000)));
    ArrayList<InputProductOption> givenMultipleOptions =
        new ArrayList<>(
            List.of(
                new InputProductOption("multiOption1", 1000),
                new InputProductOption("multiOption2", 2000),
                new InputProductOption("multiOption3", 3000)));
    ArrayList<InputBlockData> givenBlockDataList =
        new ArrayList<>(
            List.of(
                new InputBlockData(1L, BlockType.IMAGE_TYPE, "image1.png"),
                new InputBlockData(2L, BlockType.TEXT_TYPE, "test text"),
                new InputBlockData(3L, BlockType.IMAGE_TYPE, "image2.png"),
                new InputBlockData(4L, BlockType.IMAGE_TYPE, "image3.png")));
    ArrayList<MultipartFile> givenBlockImages =
        new ArrayList<>(
            List.of(
                makeMockMultipartFile(
                    "image1.png", "src/test/resources/static/product_image/image1.png"),
                makeMockMultipartFile(
                    "image2.png", "src/test/resources/static/product_image/image2.png"),
                makeMockMultipartFile(
                    "image3.png", "src/test/resources/static/product_image/image3.png")));
    return ProductMakeData.builder()
        .productTypeId(givenProductTypeId)
        .name(givenName)
        .singleOptions(givenSingleOptions)
        .price(givenPrice)
        .discountAmount(givenDiscountAmount)
        .discountRate(givenDiscountRate)
        .productImages(givenProductImageList)
        .multiOptions(givenMultipleOptions)
        .blockDataList(givenBlockDataList)
        .blockImages(givenBlockImages);
  }

  public static ProductMakeData makeProduct(ProductType productType) {
    return fullData().productTypeId(productType.getId()).build();
  }

  private static MockMultipartFile makeMockMultipartFile(String originName, String path) {
    try {
      return new MockMultipartFile(
          "file", originName, "image/png", Files.readAllBytes(Paths.get(path)));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
