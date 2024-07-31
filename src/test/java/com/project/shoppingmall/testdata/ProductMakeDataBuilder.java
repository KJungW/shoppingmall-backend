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
    InputProductOption givenSingleOption = new InputProductOption("singleOption", -1000);
    Integer givenPrice = 10000;
    Integer givenDiscountAmount = 500;
    Double givenDiscountRate = 10.5;
    ArrayList<MultipartFile> givenProductImageList = new ArrayList<>();
    List<InputProductOption> givenMulitpleOptions = new ArrayList<>();
    List<InputBlockData> givenBlockDataList = new ArrayList<>();
    List<MultipartFile> givenBlockImages = new ArrayList<>();
    MockMultipartFile givenProductImage1 =
        new MockMultipartFile(
            "file",
            "1.png",
            "image/png",
            Files.readAllBytes(Paths.get("src/test/resources/static/product_image/1.png")));
    MockMultipartFile givenProductImage2 =
        new MockMultipartFile(
            "file",
            "2.png",
            "image/png",
            Files.readAllBytes(Paths.get("src/test/resources/static/product_image/2.png")));
    MockMultipartFile givenProductImage3 =
        new MockMultipartFile(
            "file",
            "3.png",
            "image/png",
            Files.readAllBytes(Paths.get("src/test/resources/static/product_image/3.png")));
    givenProductImageList.add(givenProductImage1);
    givenProductImageList.add(givenProductImage2);
    givenProductImageList.add(givenProductImage3);
    InputProductOption multiOption1 = new InputProductOption("multiOption1", 1000);
    InputProductOption multiOption2 = new InputProductOption("multiOption2", 1000);
    InputProductOption multiOption3 = new InputProductOption("multiOption3", 1000);
    givenMulitpleOptions.add(multiOption1);
    givenMulitpleOptions.add(multiOption2);
    givenMulitpleOptions.add(multiOption3);
    InputBlockData inputBlockData1 = new InputBlockData(1L, BlockType.IMAGE_TYPE, "image1.png");
    InputBlockData inputBlockData2 = new InputBlockData(2L, BlockType.TEXT_TYPE, "test text");
    InputBlockData inputBlockData3 = new InputBlockData(3L, BlockType.IMAGE_TYPE, "image2.png");
    InputBlockData inputBlockData4 = new InputBlockData(4L, BlockType.IMAGE_TYPE, "image3.png");
    givenBlockDataList.add(inputBlockData1);
    givenBlockDataList.add(inputBlockData2);
    givenBlockDataList.add(inputBlockData3);
    givenBlockDataList.add(inputBlockData4);
    MockMultipartFile givenBlockData1 =
        new MockMultipartFile(
            "file",
            "image1.png",
            "image/png",
            Files.readAllBytes(Paths.get("src/test/resources/static/product_image/image1.png")));
    MockMultipartFile givenBlockData2 =
        new MockMultipartFile(
            "file",
            "image2.png",
            "image/png",
            Files.readAllBytes(Paths.get("src/test/resources/static/product_image/image2.png")));
    MockMultipartFile givenBlockData3 =
        new MockMultipartFile(
            "file",
            "image3.png",
            "image/png",
            Files.readAllBytes(Paths.get("src/test/resources/static/product_image/image3.png")));
    givenBlockImages.add(givenBlockData1);
    givenBlockImages.add(givenBlockData2);
    givenBlockImages.add(givenBlockData3);

    return ProductMakeData.builder()
        .productTypeId(givenProductTypeId)
        .name(givenName)
        .singleOption(givenSingleOption)
        .price(givenPrice)
        .discountAmount(givenDiscountAmount)
        .discountRate(givenDiscountRate)
        .productImages(givenProductImageList)
        .multiOptions(givenMulitpleOptions)
        .blockDataList(givenBlockDataList)
        .blockImages(givenBlockImages);
  }
}
