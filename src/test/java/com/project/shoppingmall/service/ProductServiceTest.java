package com.project.shoppingmall.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.project.shoppingmall.dto.block.ContentBlock;
import com.project.shoppingmall.dto.block.ImageBlock;
import com.project.shoppingmall.dto.file.FileUploadResult;
import com.project.shoppingmall.dto.product.ProductMakeData;
import com.project.shoppingmall.dto.product.ProductOption;
import com.project.shoppingmall.entity.*;
import com.project.shoppingmall.exception.CannotSaveProductBecauseMemberBan;
import com.project.shoppingmall.exception.DataNotFound;
import com.project.shoppingmall.exception.MemberAccountIsNotRegistered;
import com.project.shoppingmall.exception.WrongPriceAndDiscount;
import com.project.shoppingmall.repository.ProductRepository;
import com.project.shoppingmall.service.member.MemberFindService;
import com.project.shoppingmall.service.product.ProductFindService;
import com.project.shoppingmall.service.product.ProductService;
import com.project.shoppingmall.service.product_type.ProductTypeService;
import com.project.shoppingmall.service.s3.S3Service;
import com.project.shoppingmall.testdata.member.MemberBuilder;
import com.project.shoppingmall.testdata.product.ProductBuilder;
import com.project.shoppingmall.testdata.product.ProductMakeDataBuilder;
import com.project.shoppingmall.testdata.product_type.ProductTypeBuilder;
import com.project.shoppingmall.type.BlockType;
import com.project.shoppingmall.type.LoginType;
import com.project.shoppingmall.type.ProductSaleType;
import com.project.shoppingmall.util.JsonUtil;
import com.project.shoppingmall.util.PriceCalculateUtil;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

class ProductServiceTest {
  private ProductService productService;
  private MemberFindService mockMemberFindService;
  private ProductTypeService productTypeService;
  private ProductRepository productRepository;
  private ProductFindService productFindService;
  private S3Service s3Service;
  private static MockedStatic<JsonUtil> jsonUtil;

  @BeforeEach
  public void beforeEach() {
    mockMemberFindService = mock(MemberFindService.class);
    productTypeService = mock(ProductTypeService.class);
    productRepository = mock(ProductRepository.class);
    productFindService = mock(ProductFindService.class);
    s3Service = mock(S3Service.class);
    jsonUtil = mockStatic(JsonUtil.class);
    productService =
        new ProductService(
            mockMemberFindService,
            productTypeService,
            productRepository,
            productFindService,
            s3Service);
  }

  @AfterEach
  public void afterEach() {
    jsonUtil.close();
  }

  @Test
  @DisplayName("save() : 정상흐름")
  public void save_ok() {
    // given
    Long inputMemberId = 1L;
    Member givenMember =
        MemberBuilder.makeMemberWithAccountNumber(
            inputMemberId, LoginType.NAVER, "123124-512412-123");
    ProductType givenProductType = ProductTypeBuilder.makeProductType(2L, "test$detail");
    FileUploadResult givenFileUploadResult = new FileUploadResult("severuri/test", "download/test");
    ProductMakeData inputMakeData = ProductMakeDataBuilder.makeProduct(givenProductType);

    when(mockMemberFindService.findById(any())).thenReturn(Optional.of(givenMember));
    when(productTypeService.findById(any())).thenReturn(Optional.of(givenProductType));
    when(s3Service.uploadFile(any(), any())).thenReturn(givenFileUploadResult);

    // when
    Product savedProduct = productService.save(inputMemberId, inputMakeData);

    // then
    check_s3Service_uploadFile(inputMakeData);
    checkProduct(givenMember, inputMakeData, givenFileUploadResult, savedProduct);
  }

  @Test
  @DisplayName("save() : 잘못된 가격과 할인")
  public void save_wrongPriceAndDiscount() {
    // given
    Long inputMemberId = 1L;
    Member givenMember =
        MemberBuilder.makeMemberWithAccountNumber(
            inputMemberId, LoginType.NAVER, "123124-512412-123");
    ProductType givenProductType = ProductTypeBuilder.makeProductType(2L, "test$detail");
    FileUploadResult givenFileUploadResult = new FileUploadResult("severuri/test", "download/test");
    ProductMakeData inputMakeData = ProductMakeDataBuilder.makeProduct(givenProductType);

    ReflectionTestUtils.setField(inputMakeData, "price", 10000);
    ReflectionTestUtils.setField(inputMakeData, "discountAmount", 5000);
    ReflectionTestUtils.setField(inputMakeData, "discountRate", 50d);

    when(mockMemberFindService.findById(any())).thenReturn(Optional.of(givenMember));
    when(productTypeService.findById(any())).thenReturn(Optional.of(givenProductType));
    when(s3Service.uploadFile(any(), any())).thenReturn(givenFileUploadResult);

    // when
    assertThrows(
        WrongPriceAndDiscount.class, () -> productService.save(inputMemberId, inputMakeData));
  }

  @Test
  @DisplayName("save() : 벤상태의 회원이 제품을 등록하려 시도함")
  public void save_bannedMember() {
    // given
    Long inputMemberId = 1L;
    Member givenMember =
        MemberBuilder.makeMemberWithAccountNumber(
            inputMemberId, LoginType.NAVER, "123124-512412-123");
    ProductType givenProductType = ProductTypeBuilder.makeProductType(2L, "test$detail");
    FileUploadResult givenFileUploadResult = new FileUploadResult("severuri/test", "download/test");
    ProductMakeData inputMakeData = ProductMakeDataBuilder.makeProduct(givenProductType);

    ReflectionTestUtils.setField(givenMember, "isBan", true);

    when(mockMemberFindService.findById(any())).thenReturn(Optional.of(givenMember));
    when(productTypeService.findById(any())).thenReturn(Optional.of(givenProductType));
    when(s3Service.uploadFile(any(), any())).thenReturn(givenFileUploadResult);

    // when
    assertThrows(
        CannotSaveProductBecauseMemberBan.class,
        () -> productService.save(inputMemberId, inputMakeData));
  }

  @Test
  @DisplayName("save() : 계좌를 등록하지 않은 회원이 제품을 등록하려고 시도")
  public void save_notRegisterAccount() {
    // given
    Long inputMemberId = 1L;
    Member givenMember = MemberBuilder.makeMember(inputMemberId, LoginType.NAVER);
    ProductType givenProductType = ProductTypeBuilder.makeProductType(2L, "test$detail");
    FileUploadResult givenFileUploadResult = new FileUploadResult("severuri/test", "download/test");
    ProductMakeData inputMakeData = ProductMakeDataBuilder.makeProduct(givenProductType);

    when(mockMemberFindService.findById(any())).thenReturn(Optional.of(givenMember));
    when(productTypeService.findById(any())).thenReturn(Optional.of(givenProductType));
    when(s3Service.uploadFile(any(), any())).thenReturn(givenFileUploadResult);

    // when
    assertThrows(
        MemberAccountIsNotRegistered.class,
        () -> productService.save(inputMemberId, inputMakeData));
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
    when(productFindService.findById(any())).thenReturn(Optional.of(givenProduct));

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
    //    verify(s3Service, times(ProductBuilder.PRODUCT_IMAGE_COUNT +
    // ProductBuilder.IMAGE_BLOCK_COUNT))
    //        .deleteFile(any());
    //    jsonUtil.verify(
    //        () -> JsonUtil.convertJsonToObject(any(), any()),
    // times(ProductBuilder.IMAGE_BLOCK_COUNT));
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
    when(productFindService.findById(any())).thenReturn(Optional.of(givenProduct));

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
    when(productFindService.findById(any())).thenReturn(Optional.of(givenProduct));

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
  @DisplayName("changeProductToOnSale() : 정상흐름")
  public void changeProductToOnSale_ok() throws IOException {
    // given
    long givenMemberId = 10L;
    long givenProductId = 32L;

    Product givenProduct = ProductBuilder.fullData().build();
    ReflectionTestUtils.setField(givenProduct, "id", givenProductId);
    ReflectionTestUtils.setField(givenProduct, "saleState", ProductSaleType.DISCONTINUED);
    ReflectionTestUtils.setField(givenProduct.getSeller(), "id", givenMemberId);
    when(productFindService.findByIdWithSeller(anyLong())).thenReturn(Optional.of(givenProduct));

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
    when(productFindService.findByIdWithSeller(anyLong())).thenReturn(Optional.of(givenProduct));

    // when
    Product product = productService.changeProductToDiscontinued(givenMemberId, givenProductId);

    // then
    assertEquals(givenProductId, product.getId());
    assertEquals(givenMemberId, product.getSeller().getId());
    assertEquals(ProductSaleType.DISCONTINUED, givenProduct.getSaleState());
  }

  public void checkProduct(
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

  private void checkProductImage(
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

  private void checkProductBlock(
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

  private void checkProductSingleOption(
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

  private void checkProductMultiOptions(
      List<ProductOption> givenMultiOptions, List<ProductMultipleOption> targetMultiOptions) {
    // - Product.multipleOptions 검증
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

  private void checkProductFinalPrice(ProductMakeData givenMakeData, int targetPrice) {
    int expectedFinalPrice =
        PriceCalculateUtil.calculatePrice(
            givenMakeData.getPrice(),
            givenMakeData.getDiscountAmount(),
            givenMakeData.getDiscountRate());
    assertEquals(expectedFinalPrice, targetPrice);
  }

  private void check_s3Service_uploadFile(ProductMakeData givenMakeData) {
    int givenImageBlockCount =
        givenMakeData.getContentBlocks().stream()
            .filter(block -> block.getBlockType().equals(BlockType.IMAGE_TYPE))
            .toList()
            .size();
    int expectedTimes = givenMakeData.getProductImages().size() + givenImageBlockCount;
    verify(s3Service, times(expectedTimes)).uploadFile(any(), any());
  }
}
