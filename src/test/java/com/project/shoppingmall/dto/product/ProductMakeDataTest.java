package com.project.shoppingmall.dto.product;

import static org.junit.jupiter.api.Assertions.*;

import com.project.shoppingmall.controller.product.input.InputBlockData;
import com.project.shoppingmall.controller.product.input.InputProductOption;
import com.project.shoppingmall.dto.block.ImageBlockBeforeImageSave;
import com.project.shoppingmall.dto.block.TextBlock;
import com.project.shoppingmall.exception.NotMatchBlockAndImage;
import com.project.shoppingmall.type.BlockType;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

class ProductMakeDataTest {
  private Long givenProductTypeId;
  private String givenName;
  private InputProductOption givenSingleOption;
  private Integer givenPrice;
  private Integer givenDiscountAmount;
  private Double givenDiscountRate;
  private ArrayList<MultipartFile> givenProductImageList = new ArrayList<>();
  private List<InputProductOption> givenMulitpleOptions = new ArrayList<>();
  private List<InputBlockData> givenBlockDataList = new ArrayList<>();
  private List<MultipartFile> givenBlockImages = new ArrayList<>();

  @BeforeEach
  public void beforeEach() throws IOException {
    givenProductTypeId = 1L;
    givenName = "testProduct";
    givenSingleOption = new InputProductOption("singleOption", -1000);
    givenPrice = 10000;
    givenDiscountAmount = 500;
    givenDiscountRate = 10.5;

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
  }

  @Test
  @DisplayName("ProductMakeData 생성 : 정상흐름")
  public void productMakeData_ok() throws IOException {
    // when
    ProductMakeData result =
        ProductMakeData.builder()
            .productTypeId(givenProductTypeId)
            .name(givenName)
            .singleOption(givenSingleOption)
            .price(givenPrice)
            .discountAmount(givenDiscountAmount)
            .discountRate(givenDiscountRate)
            .productImages(givenProductImageList)
            .multiOptions(givenMulitpleOptions)
            .blockDataList(givenBlockDataList)
            .blockImages(givenBlockImages)
            .build();

    // then
    assertEquals(givenProductTypeId, result.getProductTypeId());
    assertEquals(givenName, result.getName());
    assertEquals(givenSingleOption.getOptionName(), result.getSingleOption().getOptionName());
    assertEquals(
        givenSingleOption.getPriceChangeAmount(), result.getSingleOption().getPriceChangeAmount());
    assertEquals(givenPrice, result.getPrice());
    assertEquals(givenDiscountAmount, result.getDiscountAmount());
    assertEquals(
        givenProductImageList.get(0).getOriginalFilename(),
        result.getProductImages().get(0).getOriginalFilename());
    assertEquals(
        givenProductImageList.get(1).getOriginalFilename(),
        result.getProductImages().get(1).getOriginalFilename());
    assertEquals(
        givenProductImageList.get(2).getOriginalFilename(),
        result.getProductImages().get(2).getOriginalFilename());
    assertEquals(
        givenMulitpleOptions.get(0).getOptionName(),
        result.getMultiOptions().get(0).getOptionName());
    assertEquals(
        givenMulitpleOptions.get(1).getOptionName(),
        result.getMultiOptions().get(1).getOptionName());
    assertEquals(
        givenMulitpleOptions.get(2).getOptionName(),
        result.getMultiOptions().get(2).getOptionName());
    assertEquals(
        givenBlockImages.get(0).getOriginalFilename(),
        ((ImageBlockBeforeImageSave) result.getContentBlocks().get(0))
            .getImage()
            .getOriginalFilename());
    assertEquals(
        givenBlockDataList.get(1).getContent(),
        ((TextBlock) result.getContentBlocks().get(1)).getContent());
    assertEquals(
        givenBlockImages.get(1).getOriginalFilename(),
        ((ImageBlockBeforeImageSave) result.getContentBlocks().get(2))
            .getImage()
            .getOriginalFilename());
    assertEquals(
        givenBlockImages.get(2).getOriginalFilename(),
        ((ImageBlockBeforeImageSave) result.getContentBlocks().get(3))
            .getImage()
            .getOriginalFilename());
    assertEquals(givenDiscountRate, result.getDiscountRate());
  }

  @Test
  @DisplayName("ProductMakeData 생성 : 빌더옵션  productImages의 순서가 뒤바껴서 제공됨")
  public void productMakeData_productImages의NoOrder() throws IOException {
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
    List<MultipartFile> noOrderProductImage = new ArrayList<>();
    noOrderProductImage.add(givenProductImage3);
    noOrderProductImage.add(givenProductImage2);
    noOrderProductImage.add(givenProductImage1);

    // when
    ProductMakeData result =
        ProductMakeData.builder()
            .productTypeId(givenProductTypeId)
            .name(givenName)
            .singleOption(givenSingleOption)
            .price(givenPrice)
            .discountAmount(givenDiscountAmount)
            .discountRate(givenDiscountRate)
            .productImages(noOrderProductImage)
            .multiOptions(givenMulitpleOptions)
            .blockDataList(givenBlockDataList)
            .blockImages(givenBlockImages)
            .build();

    // then
    assertEquals(
        givenProductImageList.get(0).getOriginalFilename(),
        result.getProductImages().get(0).getOriginalFilename());
    assertEquals(
        givenProductImageList.get(1).getOriginalFilename(),
        result.getProductImages().get(1).getOriginalFilename());
    assertEquals(
        givenProductImageList.get(2).getOriginalFilename(),
        result.getProductImages().get(2).getOriginalFilename());
  }

  @Test
  @DisplayName("ProductMakeData 생성 : 빌더옵션 multiOption이 null일 경우")
  public void productMakeData_multiOptionNull() {
    // when
    ProductMakeData result =
        ProductMakeData.builder()
            .productTypeId(givenProductTypeId)
            .name(givenName)
            .singleOption(givenSingleOption)
            .price(givenPrice)
            .discountAmount(givenDiscountAmount)
            .discountRate(givenDiscountRate)
            .productImages(givenProductImageList)
            .multiOptions(null)
            .blockDataList(givenBlockDataList)
            .blockImages(givenBlockImages)
            .build();

    // then
    assertEquals(0, result.getMultiOptions().size());
  }

  @Test
  @DisplayName("ProductMakeData 생성 : 빌더옵션 productImages이 null일 경우")
  public void productMakeData_productImagesNull() {
    // when
    ProductMakeData result =
        ProductMakeData.builder()
            .productTypeId(givenProductTypeId)
            .name(givenName)
            .singleOption(givenSingleOption)
            .price(givenPrice)
            .discountAmount(givenDiscountAmount)
            .discountRate(givenDiscountRate)
            .productImages(null)
            .multiOptions(givenMulitpleOptions)
            .blockDataList(givenBlockDataList)
            .blockImages(givenBlockImages)
            .build();

    // then
    assertEquals(0, result.getProductImages().size());
  }

  @Test
  @DisplayName("ProductMakeData 생성 : 빌더옵션 blockDataList null일 경우")
  public void productMakeData_blockDataListNull() {
    // when then
    assertThrows(
        NotMatchBlockAndImage.class,
        () -> {
          ProductMakeData.builder()
              .productTypeId(givenProductTypeId)
              .name(givenName)
              .singleOption(givenSingleOption)
              .price(givenPrice)
              .discountAmount(givenDiscountAmount)
              .discountRate(givenDiscountRate)
              .productImages(givenProductImageList)
              .multiOptions(givenMulitpleOptions)
              .blockDataList(null)
              .blockImages(givenBlockImages)
              .build();
        });
  }

  @Test
  @DisplayName("ProductMakeData 생성 : 빌더옵션 blockImages null일 경우")
  public void productMakeData_blockImagesNull() {
    // when then
    assertThrows(
        NotMatchBlockAndImage.class,
        () -> {
          ProductMakeData.builder()
              .productTypeId(givenProductTypeId)
              .name(givenName)
              .singleOption(givenSingleOption)
              .price(givenPrice)
              .discountAmount(givenDiscountAmount)
              .discountRate(givenDiscountRate)
              .productImages(givenProductImageList)
              .multiOptions(givenMulitpleOptions)
              .blockDataList(givenBlockDataList)
              .blockImages(null)
              .build();
        });
  }

  @Test
  @DisplayName("ProductMakeData 생성 : 빌더옵션 blockDataList와 blockImages가 null일 경우")
  public void productMakeData_blockDataListAndBlockImagesNull() {
    // when
    ProductMakeData result =
        ProductMakeData.builder()
            .productTypeId(givenProductTypeId)
            .name(givenName)
            .singleOption(givenSingleOption)
            .price(givenPrice)
            .discountAmount(givenDiscountAmount)
            .discountRate(givenDiscountRate)
            .productImages(givenProductImageList)
            .multiOptions(givenMulitpleOptions)
            .blockDataList(null)
            .blockImages(null)
            .build();

    // then
    assertEquals(0, result.getContentBlocks().size());
  }

  @Test
  @DisplayName("ProductMakeData 생성 : 빌더옵션 blockDataList와 blockImages가 매칭되지 않을 경우")
  public void productMakeData_blockNotMatch() throws IOException {
    // given
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
    List<MultipartFile> wrongBlockImages = new ArrayList<>();
    wrongBlockImages.add(givenBlockData3);
    wrongBlockImages.add(givenBlockData2);
    wrongBlockImages.add(givenBlockData3);

    // when then
    assertThrows(
        NotMatchBlockAndImage.class,
        () -> {
          ProductMakeData.builder()
              .productTypeId(givenProductTypeId)
              .name(givenName)
              .singleOption(givenSingleOption)
              .price(givenPrice)
              .discountAmount(givenDiscountAmount)
              .discountRate(givenDiscountRate)
              .productImages(givenProductImageList)
              .multiOptions(givenMulitpleOptions)
              .blockDataList(givenBlockDataList)
              .blockImages(wrongBlockImages)
              .build();
        });
  }
}
