package com.project.shoppingmall.testdata;

import com.project.shoppingmall.controller.product.input.InputBlockData;
import com.project.shoppingmall.controller.product.input.InputProductOption;
import com.project.shoppingmall.dto.product.ProductMakeData;
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

  public static ProductMakeData.ProductMakeDataBuilder fullData() throws IOException {
    Long givenProductTypeId = 1L;
    String givenName = "testProduct";
    Integer givenPrice = 10000;
    Integer givenDiscountAmount = 500;
    Double givenDiscountRate = 10.5;
    ArrayList<MultipartFile> givenProductImageList =
        new ArrayList<>() {
          {
            add(
                new MockMultipartFile(
                    "file",
                    "1.png",
                    "image/png",
                    Files.readAllBytes(
                        Paths.get("src/test/resources/static/product_image/1.png"))));
            add(
                new MockMultipartFile(
                    "file",
                    "2.png",
                    "image/png",
                    Files.readAllBytes(
                        Paths.get("src/test/resources/static/product_image/2.png"))));
            add(
                new MockMultipartFile(
                    "file",
                    "3.png",
                    "image/png",
                    Files.readAllBytes(
                        Paths.get("src/test/resources/static/product_image/3.png"))));
          }
        };
    List<InputProductOption> givenSigleOptions =
        new ArrayList<>() {
          {
            add(new InputProductOption("multiOption1", -1000));
            add(new InputProductOption("multiOption2", -2000));
            add(new InputProductOption("multiOption3", -3000));
          }
        };
    List<InputProductOption> givenMultipleOptions =
        new ArrayList<>() {
          {
            add(new InputProductOption("multiOption1", 1000));
            add(new InputProductOption("multiOption2", 2000));
            add(new InputProductOption("multiOption3", 3000));
          }
        };
    List<InputBlockData> givenBlockDataList =
        new ArrayList<>() {
          {
            add(new InputBlockData(1L, BlockType.IMAGE_TYPE, "image1.png"));
            add(new InputBlockData(2L, BlockType.TEXT_TYPE, "test text"));
            add(new InputBlockData(3L, BlockType.IMAGE_TYPE, "image2.png"));
            add(new InputBlockData(4L, BlockType.IMAGE_TYPE, "image3.png"));
          }
        };
    List<MultipartFile> givenBlockImages =
        new ArrayList<>() {
          {
            add(
                new MockMultipartFile(
                    "file",
                    "image1.png",
                    "image/png",
                    Files.readAllBytes(
                        Paths.get("src/test/resources/static/product_image/image1.png"))));
            add(
                new MockMultipartFile(
                    "file",
                    "image2.png",
                    "image/png",
                    Files.readAllBytes(
                        Paths.get("src/test/resources/static/product_image/image2.png"))));
            add(
                new MockMultipartFile(
                    "file",
                    "image3.png",
                    "image/png",
                    Files.readAllBytes(
                        Paths.get("src/test/resources/static/product_image/image3.png"))));
          }
        };

    return ProductMakeData.builder()
        .productTypeId(givenProductTypeId)
        .name(givenName)
        .singleOptions(givenSigleOptions)
        .price(givenPrice)
        .discountAmount(givenDiscountAmount)
        .discountRate(givenDiscountRate)
        .productImages(givenProductImageList)
        .multiOptions(givenMultipleOptions)
        .blockDataList(givenBlockDataList)
        .blockImages(givenBlockImages);
  }
}
