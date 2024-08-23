package com.project.shoppingmall.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.project.shoppingmall.dto.block.ImageBlock;
import com.project.shoppingmall.dto.file.FileUploadResult;
import com.project.shoppingmall.dto.product.ProductMakeData;
import com.project.shoppingmall.dto.product.ProductOption;
import com.project.shoppingmall.entity.*;
import com.project.shoppingmall.exception.DataNotFound;
import com.project.shoppingmall.exception.WrongPriceAndDiscount;
import com.project.shoppingmall.repository.ProductRepository;
import com.project.shoppingmall.service.member.MemberService;
import com.project.shoppingmall.service.product.ProductService;
import com.project.shoppingmall.service.product_type.ProductTypeService;
import com.project.shoppingmall.service.s3.S3Service;
import com.project.shoppingmall.testdata.*;
import com.project.shoppingmall.type.BlockType;
import com.project.shoppingmall.type.ProductSaleType;
import com.project.shoppingmall.util.JsonUtil;
import com.project.shoppingmall.util.PriceCalculateUtil;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;
import org.springframework.test.util.ReflectionTestUtils;

class ProductServiceTest {
  private ProductService productService;
  private MemberService memberService;
  private ProductTypeService productTypeService;
  private ProductRepository productRepository;
  private S3Service s3Service;
  private static MockedStatic<JsonUtil> jsonUtil;

  @BeforeEach
  public void beforeEach() {
    memberService = mock(MemberService.class);
    productTypeService = mock(ProductTypeService.class);
    productRepository = mock(ProductRepository.class);
    s3Service = mock(S3Service.class);
    jsonUtil = mockStatic(JsonUtil.class);
    productService =
        new ProductService(memberService, productTypeService, productRepository, s3Service);
  }

  @AfterEach
  public void afterEach() {
    jsonUtil.close();
  }

  @Test
  @DisplayName("save() : 정상흐름")
  public void save_ok() throws IOException {
    // given
    Long givenMemberId = 1L;
    Member givenMember = MemberBuilder.fullData().build();
    ReflectionTestUtils.setField(givenMember, "id", givenMemberId);

    Long givenProductTypeId = 2L;
    ProductType givenProductType = new ProductType("test$detail");
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

    jsonUtil.verify(
        () -> JsonUtil.convertObjectToJson(any()),
        times(expectedTextBlockCount + expectedImageBlockCount));
    verify(s3Service, times(expectedImageBlockCount + expectedProductImgCount))
        .uploadFile(any(), any());

    // - Product.singleOption 검증
    List<String> expectedSingleOptionNames =
        givenProductMakeData.getSingleOptions().stream().map(ProductOption::getOptionName).toList();
    List<String> resultSingleOptionNames =
        savedProduct.getSingleOptions().stream().map(ProductSingleOption::getOptionName).toList();
    assertArrayEquals(expectedSingleOptionNames.toArray(), resultSingleOptionNames.toArray());
    List<Integer> expectedSingleOptionPriceChange =
        givenProductMakeData.getSingleOptions().stream()
            .map(ProductOption::getPriceChangeAmount)
            .toList();
    List<Integer> resultSingleOptionPriceChange =
        savedProduct.getSingleOptions().stream()
            .map(ProductSingleOption::getPriceChangeAmount)
            .toList();
    assertArrayEquals(
        expectedSingleOptionPriceChange.toArray(), resultSingleOptionPriceChange.toArray());

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

    // - Product.finalPrice 검증
    int expectedFinalPrice =
        PriceCalculateUtil.calculatePrice(
            givenProductMakeData.getPrice(),
            givenProductMakeData.getDiscountAmount(),
            givenProductMakeData.getDiscountRate());
    assertEquals(expectedFinalPrice, savedProduct.getFinalPrice());
  }

  @Test
  @DisplayName("save() : 잘못된 가격과 할인")
  public void save_wrongPriceAndDiscount() throws IOException {
    // given
    Long givenMemberId = 1L;
    Member givenMember = MemberBuilder.fullData().build();
    ReflectionTestUtils.setField(givenMember, "id", givenMemberId);

    Long givenProductTypeId = 2L;
    ProductType givenProductType = new ProductType("test$detail");
    ReflectionTestUtils.setField(givenProductType, "id", givenProductTypeId);

    FileUploadResult givenFileUpload = new FileUploadResult("severuri/test", "download/test");

    ProductMakeData givenProductMakeData =
        ProductMakeDataBuilder.fullData()
            .productTypeId(givenProductTypeId)
            .price(10000)
            .discountAmount(5000)
            .discountRate(50d)
            .build();

    when(memberService.findById(any())).thenReturn(Optional.of(givenMember));
    when(productTypeService.findById(any())).thenReturn(Optional.of(givenProductType));
    when(s3Service.uploadFile(any(), any())).thenReturn(givenFileUpload);

    // when
    assertThrows(
        WrongPriceAndDiscount.class,
        () -> productService.save(givenMemberId, givenProductMakeData));
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

    ProductType givenProductType = new ProductType("test$detail");
    ReflectionTestUtils.setField(givenProductType, "id", 5L);
    when(productTypeService.findById(any())).thenReturn(Optional.of(givenProductType));

    FileUploadResult givenFileUpload = new FileUploadResult("severuri/test", "download/test");
    when(s3Service.uploadFile(any(), any())).thenReturn(givenFileUpload);

    jsonUtil
        .when(() -> JsonUtil.convertJsonToObject(any(), any()))
        .thenReturn(new ImageBlock(1L, "test/serverUri", "test/downUri"));

    // when
    productService.update(givenMemberId, givenProductId, givenProductMakeData);

    // then
    verify(s3Service, times(ProductBuilder.PRODUCT_IMAGE_COUNT + ProductBuilder.IMAGE_BLOCK_COUNT))
        .deleteFile(any());
    jsonUtil.verify(
        () -> JsonUtil.convertJsonToObject(any(), any()), times(ProductBuilder.IMAGE_BLOCK_COUNT));
    jsonUtil.verify(
        () -> JsonUtil.convertObjectToJson(any()),
        times(ProductMakeDataBuilder.TEXT_BLOCK_COUNT + ProductMakeDataBuilder.IMAGE_BLOCK_COUNT));
    verify(
            s3Service,
            times(
                ProductMakeDataBuilder.IMAGE_BLOCK_COUNT
                    + ProductMakeDataBuilder.PRODUCT_IMAGE_COUNT))
        .uploadFile(any(), any());

    int expectedFinalPrice =
        PriceCalculateUtil.calculatePrice(
            givenProductMakeData.getPrice(),
            givenProductMakeData.getDiscountAmount(),
            givenProductMakeData.getDiscountRate());
    assertEquals(expectedFinalPrice, givenProduct.getFinalPrice());
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

    ProductType givenProductType = new ProductType("test$detail");
    ReflectionTestUtils.setField(givenProductType, "id", 5L);
    when(productTypeService.findById(any())).thenReturn(Optional.of(givenProductType));

    // when
    assertThrows(
        DataNotFound.class,
        () -> productService.update(givenMemberId, givenProductId, givenProductMakeData));

    // then
    verify(s3Service, times(0)).deleteFile(any());
    jsonUtil.verify(() -> JsonUtil.convertJsonToObject(any(), any()), times(0));
    jsonUtil.verify(() -> JsonUtil.convertObjectToJson(any()), times(0));
    verify(s3Service, times(0)).uploadFile(any(), any());
  }

  @Test
  @DisplayName("update() : 잘못된 가격과 할인으로 수정을 시도")
  public void update_wrongPriceAndDiscount() throws IOException {
    // given
    Long givenMemberId = 10L;
    Long givenProductId = 13L;
    ProductMakeData givenProductMakeData =
        ProductMakeDataBuilder.fullData()
            .price(10000)
            .discountAmount(5000)
            .discountRate(50d)
            .build();

    Product givenProduct = ProductBuilder.fullData().build();
    ReflectionTestUtils.setField(givenProduct, "id", givenProductId);
    ReflectionTestUtils.setField(givenProduct.getSeller(), "id", givenMemberId);
    when(productRepository.findById(any())).thenReturn(Optional.of(givenProduct));

    ProductType givenProductType = new ProductType("test$detail");
    ReflectionTestUtils.setField(givenProductType, "id", 5L);
    when(productTypeService.findById(any())).thenReturn(Optional.of(givenProductType));

    FileUploadResult givenFileUpload = new FileUploadResult("severuri/test", "download/test");
    when(s3Service.uploadFile(any(), any())).thenReturn(givenFileUpload);

    jsonUtil
        .when(() -> JsonUtil.convertJsonToObject(any(), any()))
        .thenReturn(new ImageBlock(1L, "test/serverUri", "test/downUri"));

    // when
    assertThrows(
        WrongPriceAndDiscount.class,
        () -> productService.update(givenMemberId, givenProductId, givenProductMakeData));
  }

  @Test
  @DisplayName("findByIdWithAll() : 정상흐름")
  public void findByIdWithAll_ok() throws IOException {
    // given
    Long givenProductId = 30L;
    Product givenProduct = mock(Product.class);
    when(productRepository.findByIdWithAll(any())).thenReturn(Optional.of(givenProduct));
    when(givenProduct.getProductImages()).thenReturn(new ArrayList<>());
    when(givenProduct.getSingleOptions()).thenReturn(new ArrayList<>());
    when(givenProduct.getMultipleOptions()).thenReturn(new ArrayList<>());

    // when
    Optional<Product> result = productService.findByIdWithAll(givenProductId);

    // then
    assertTrue(result.isPresent());
    verify(givenProduct, times(1)).getProductImages();
    verify(givenProduct, times(1)).getSingleOptions();
    verify(givenProduct, times(1)).getMultipleOptions();
  }

  @Test
  @DisplayName("changeProductToOnSale() : 정상흐름")
  public void changeProductToOnSale_ok() throws IOException {
    // given
    long givenMemberId = 10L;
    long givenProductId = 32L;

    Product givenProduct = ProductBuilder.fullData().build();
    ReflectionTestUtils.setField(givenProduct, "id", givenProductId);
    ReflectionTestUtils.setField(givenProduct, "saleState", ProductSaleType.DISCONTINUED);
    ReflectionTestUtils.setField(givenProduct.getSeller(), "id", givenMemberId);
    when(productService.findByIdWithSeller(anyLong())).thenReturn(Optional.of(givenProduct));

    // when
    Product product = productService.changeProductToOnSale(givenMemberId, givenProductId);

    // then
    assertEquals(givenProductId, product.getId());
    assertEquals(givenMemberId, product.getSeller().getId());
    assertEquals(ProductSaleType.ON_SALE, givenProduct.getSaleState());
  }

  @Test
  @DisplayName("changeProductToDiscontinued() : 정상흐름")
  public void changeProductToDiscontinued_ok() throws IOException {
    // given
    long givenMemberId = 10L;
    long givenProductId = 32L;

    Product givenProduct = ProductBuilder.fullData().build();
    ReflectionTestUtils.setField(givenProduct, "id", givenProductId);
    ReflectionTestUtils.setField(givenProduct, "saleState", ProductSaleType.ON_SALE);
    ReflectionTestUtils.setField(givenProduct.getSeller(), "id", givenMemberId);
    when(productService.findByIdWithSeller(anyLong())).thenReturn(Optional.of(givenProduct));

    // when
    Product product = productService.changeProductToDiscontinued(givenMemberId, givenProductId);

    // then
    assertEquals(givenProductId, product.getId());
    assertEquals(givenMemberId, product.getSeller().getId());
    assertEquals(ProductSaleType.DISCONTINUED, givenProduct.getSaleState());
  }
}
