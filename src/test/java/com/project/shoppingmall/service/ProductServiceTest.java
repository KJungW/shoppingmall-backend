package com.project.shoppingmall.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.project.shoppingmall.dto.block.ImageBlock;
import com.project.shoppingmall.dto.file.FileUploadResult;
import com.project.shoppingmall.dto.product.ProductMakeData;
import com.project.shoppingmall.dto.product.ProductOption;
import com.project.shoppingmall.entity.*;
import com.project.shoppingmall.exception.DataNotFound;
import com.project.shoppingmall.repository.ProductRepository;
import com.project.shoppingmall.testdata.MemberBuilder;
import com.project.shoppingmall.testdata.ProductBuilder;
import com.project.shoppingmall.testdata.ProductMakeDataBuilder;
import com.project.shoppingmall.type.BlockType;
import com.project.shoppingmall.util.JsonUtil;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class ProductServiceTest {
  private ProductService productService;
  private MemberService memberService;
  private ProductTypeService productTypeService;
  private ProductRepository productRepository;
  private S3Service s3Service;
  private JsonUtil jsonUtil;

  @BeforeEach
  public void beforeEach() {
    memberService = mock(MemberService.class);
    productTypeService = mock(ProductTypeService.class);
    productRepository = mock(ProductRepository.class);
    s3Service = mock(S3Service.class);
    jsonUtil = mock(JsonUtil.class);
    productService =
        new ProductService(
            memberService, productTypeService, productRepository, s3Service, jsonUtil);
  }

  @Test
  @DisplayName("save() : 정상흐름")
  public void save_ok() throws IOException {
    // given
    Long givenMemberId = 1L;
    Member givenMember = MemberBuilder.fullData().build();
    ReflectionTestUtils.setField(givenMember, "id", givenMemberId);

    Long givenProductTypeId = 2L;
    ProductType givenProductType = new ProductType("test/detail");
    ReflectionTestUtils.setField(givenProductType, "id", givenProductTypeId);

    FileUploadResult givenFileUpload = new FileUploadResult("severuri/test", "download/test");

    ProductMakeData givenProductMakeData =
        ProductMakeDataBuilder.fullData().productTypeId(givenProductTypeId).build();

    when(memberService.findById(any())).thenReturn(Optional.of(givenMember));
    when(productTypeService.findById(any())).thenReturn(Optional.of(givenProductType));
    when(s3Service.uploadFile(any(), any())).thenReturn(givenFileUpload);

    // when
    Product savedProduct = productService.save(givenMemberId, givenProductMakeData);

    // then
    // - Product.seller 검증
    assertEquals(1L, savedProduct.getSeller().getId());

    // - Product.productType 검증
    assertEquals(givenProductMakeData.getProductTypeId(), savedProduct.getProductType().getId());

    // - Product.productImages 검증
    int expectedProductImgCount = givenProductMakeData.getProductImages().size();
    for (ProductImage image : savedProduct.getProductImages()) {
      assertEquals(givenFileUpload.getFileServerUri(), image.getImageUri());
      assertEquals(givenFileUpload.getDownLoadUrl(), image.getDownLoadUrl());
    }

    // - Product.contents 검증
    int expectedTextBlockCount =
        givenProductMakeData.getContentBlocks().stream()
            .filter(block -> block.getBlockType().equals(BlockType.TEXT_TYPE))
            .toList()
            .size();
    int expectedImageBlockCount =
        givenProductMakeData.getContentBlocks().stream()
            .filter(block -> block.getBlockType().equals(BlockType.IMAGE_TYPE))
            .toList()
            .size();
    assertEquals(
        expectedTextBlockCount + expectedImageBlockCount, savedProduct.getContents().size());
    verify(jsonUtil, times(expectedTextBlockCount + expectedImageBlockCount))
        .convertObjectToJson(any());
    verify(s3Service, times(expectedImageBlockCount + expectedProductImgCount))
        .uploadFile(any(), any());

    // - Product.singleOption 검증
    assertEquals(
        givenProductMakeData.getSingleOption().getOptionName(),
        savedProduct.getSingleOption().getOptionName());
    assertEquals(
        givenProductMakeData.getSingleOption().getPriceChangeAmount(),
        savedProduct.getSingleOption().getPriceChangeAmount());

    // - Product.multipleOptions 검증
    List<String> expectedOptionNames =
        givenProductMakeData.getMultiOptions().stream().map(ProductOption::getOptionName).toList();
    List<String> resultOptionNames =
        savedProduct.getMultipleOptions().stream()
            .map(ProductMultipleOption::getOptionName)
            .toList();
    assertArrayEquals(expectedOptionNames.toArray(), resultOptionNames.toArray());
    List<Integer> expectedOptionPriceChange =
        givenProductMakeData.getMultiOptions().stream()
            .map(ProductOption::getPriceChangeAmount)
            .toList();
    List<Integer> resultOptionPriceChange =
        savedProduct.getMultipleOptions().stream()
            .map(ProductMultipleOption::getPriceChangeAmount)
            .toList();
    assertArrayEquals(expectedOptionPriceChange.toArray(), resultOptionPriceChange.toArray());

    // - Product.name 검증
    assertEquals(givenProductMakeData.getName(), savedProduct.getName());

    // - Product.price 검증
    assertEquals(givenProductMakeData.getPrice(), savedProduct.getPrice());

    // - Product.discountAmount 검증
    assertEquals(givenProductMakeData.getDiscountAmount(), savedProduct.getDiscountAmount());

    // - Product.discountRate 검증
    assertEquals(givenProductMakeData.getDiscountRate(), savedProduct.getDiscountRate());

    // - Product.isBan 검증
    assertEquals(false, savedProduct.getIsBan());

    // - Product.scoreAvg 검증
    assertEquals(0d, savedProduct.getScoreAvg());
  }

  @Test
  @DisplayName("update() : 정상흐름")
  public void update_ok() throws IOException {
    // given
    Long givenMemberId = 10L;
    Long givenProductId = 13L;
    ProductMakeData givenProductMakeData = ProductMakeDataBuilder.fullData().build();

    Product givenProduct = ProductBuilder.fullData().build();
    ReflectionTestUtils.setField(givenProduct, "id", givenProductId);
    ReflectionTestUtils.setField(givenProduct.getSeller(), "id", givenMemberId);
    when(productRepository.findById(any())).thenReturn(Optional.of(givenProduct));

    ProductType givenProductType = new ProductType("test/detail");
    ReflectionTestUtils.setField(givenProductType, "id", 5L);
    when(productTypeService.findById(any())).thenReturn(Optional.of(givenProductType));

    FileUploadResult givenFileUpload = new FileUploadResult("severuri/test", "download/test");
    when(s3Service.uploadFile(any(), any())).thenReturn(givenFileUpload);

    when(jsonUtil.convertJsonToObject(any(), any()))
        .thenReturn(new ImageBlock(1L, "test/serverUri", "test/downUri"));

    // when
    productService.update(givenMemberId, givenProductId, givenProductMakeData);

    // then
    verify(s3Service, times(ProductBuilder.PRODUCT_IMAGE_COUNT + ProductBuilder.ImageBlockCount))
        .deleteFile(any());
    verify(jsonUtil, times(ProductBuilder.ImageBlockCount)).convertJsonToObject(any(), any());
    verify(
            jsonUtil,
            times(ProductMakeDataBuilder.TextBlockCount + ProductMakeDataBuilder.ImageBlockCount))
        .convertObjectToJson(any());
    verify(
            s3Service,
            times(
                ProductMakeDataBuilder.ImageBlockCount
                    + ProductMakeDataBuilder.PRODUCT_IMAGE_COUNT))
        .uploadFile(any(), any());
  }

  @Test
  @DisplayName("update() : 다른 회원의 제품을 수정하려고 시도")
  public void update_otherMemberProduct() throws IOException {
    // given
    Long givenMemberId = 10L;
    Long givenProductId = 13L;
    ProductMakeData givenProductMakeData = ProductMakeDataBuilder.fullData().build();

    Product givenProduct = ProductBuilder.fullData().build();
    Long givenProductSellerId = 30L;
    ReflectionTestUtils.setField(givenProduct, "id", givenProductId);
    ReflectionTestUtils.setField(givenProduct.getSeller(), "id", givenProductSellerId);
    when(productRepository.findById(any())).thenReturn(Optional.of(givenProduct));

    ProductType givenProductType = new ProductType("test/detail");
    ReflectionTestUtils.setField(givenProductType, "id", 5L);
    when(productTypeService.findById(any())).thenReturn(Optional.of(givenProductType));

    // when
    assertThrows(
        DataNotFound.class,
        () -> productService.update(givenMemberId, givenProductId, givenProductMakeData));

    // then
    verify(s3Service, times(0)).deleteFile(any());
    verify(jsonUtil, times(0)).convertJsonToObject(any(), any());
    verify(jsonUtil, times(0)).convertObjectToJson(any());
    verify(s3Service, times(0)).uploadFile(any(), any());
  }
}
